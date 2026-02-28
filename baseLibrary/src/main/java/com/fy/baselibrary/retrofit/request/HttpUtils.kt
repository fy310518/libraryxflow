package com.fy.baselibrary.retrofit.request

import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.ArrayMap
import com.fy.baselibrary.application.ioc.ConfigUtils
import com.fy.baselibrary.retrofit.RequestUtils
import com.fy.baselibrary.retrofit.ServerException
import com.fy.baselibrary.retrofit.converter.file.FileRequestBodyConverter
import com.fy.baselibrary.retrofit.load.ApiService
import com.fy.baselibrary.retrofit.observer.BaseBean
import com.fy.baselibrary.retrofit.observer.IProgressDialog
import com.fy.baselibrary.retrofit.observer.TransmissionState
import com.fy.baselibrary.retrofit.request.HttpUtils.flowConverter
import com.fy.baselibrary.retrofit.request.HttpUtils.httpGet
import com.fy.baselibrary.retrofit.request.HttpUtils.postCompose
import com.fy.baselibrary.retrofit.request.HttpUtils.postForm
import com.fy.baselibrary.utils.AppUtils
import com.fy.baselibrary.utils.Constant
import com.fy.baselibrary.utils.FileUtils
import com.fy.baselibrary.utils.GsonUtils
import com.fy.baselibrary.utils.cache.SpfAgent
import com.fy.baselibrary.utils.media.UriUtils
import com.fy.baselibrary.utils.net.NetUtils
import com.fy.baselibrary.utils.notify.L
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile

enum class Method{
    GET, POST, POSTJSON
}

class Builder{
    var requestMethod: Method = Method.POSTJSON
    var apiUrl: String = ""
    var params: ArrayMap<String, Any> = ArrayMap<String, Any>()
    var headers: ArrayMap<String, Any> = ArrayMap<String, Any>()
    var offline: HttpOffline? = null

    fun setParams(params: ArrayMap<String, Any>, headers: ArrayMap<String, Any> = ArrayMap<String, Any>()) = apply {
        this.params = params
        this.headers = headers
    }

    fun setOfflineListener(offline: HttpOffline) = apply {
        this.offline = offline
    }

    fun <T> getFlow(typeOfT: TypeToken<T>, action: (suspend () -> BaseBean<Any>)? = null): Flow<T> {
        return offline?.let {
            flow {
                val result = if (NetUtils.isConnected()) {
                    getNetFlow(typeOfT, action)
                        .map { result ->
                            // 请求成功，缓存到数据库
                            offline?.saveDataToDb(result)

                            offline?.queryAllData(typeOfT, params) ?: result
                        }
                        .first()
                } else {
                    offline?.queryAllData(typeOfT, params) ?: run {
                        if (GsonUtils.isListType(typeOfT.type)) {
                            GsonUtils.fromJson("[]", typeOfT)
                        } else {
                            GsonUtils.fromJson("{}", typeOfT)
                        }
                    }
                }

                emit(result)
            }
                .flowOn(Dispatchers.IO)
        } ?: run {
            getNetFlow(typeOfT, action)
        }
    }

    /**
     * 通用请求 不满足 业务需求时，通过 action 回调函数，获取 自定义的 retrofit请求方法，以支持 更多场景
     * @param action 请求方法
     *       RequestUtils.create(ApiService::class.java)
     *                 .getCompose(apiUrl, headers, params)
     */
    fun <T> getNetFlow(typeOfT: TypeToken<T>, action: (suspend () -> BaseBean<Any>)? = null): Flow<T> {
        return action?.let {
            flow {
                emit(action())
            }.flowConverter(typeOfT)
        } ?: when (requestMethod) {
            Method.GET -> {
                httpGet(apiUrl, params, typeOfT, headers)
            }

            Method.POST -> {
                postForm(apiUrl, params, typeOfT, headers)
            }

            else -> {
                postCompose(apiUrl, params, typeOfT, headers)
            }
        }
    }
}


object HttpUtils {

    val CALL_BACK_LENGTH: Long = (1024 * 1024).toLong()

    fun build() = Builder()
    fun build(apiUrl: String, requestMethod: Method = Method.POSTJSON) = Builder().apply {
        this.apiUrl = apiUrl
        this.requestMethod = requestMethod
    }

    fun <T, I : BaseBean<Any>> Flow<I>.flowConverter(typeOfT: TypeToken<T>): Flow<T> {
        return map { result ->
            if (result.isSuccess()) {
                val data = run {
                    val jsonData = GsonUtils.toJson(result.getResultData())
                    GsonUtils.fromJson(jsonData, typeOfT)
                }

                data
            } else {
                throw ServerException(result.getResultMsg(), result.getResultCode())
            }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * 拓展 Flow 函数，添加异常处理，进度条显示，进度条关闭
     */
    fun <T> Flow<T>.flowNext(progressDialog: IProgressDialog? = null, action: ((ex: Throwable) -> Unit)? = null): Flow<T> {
        return flowOn(Dispatchers.IO)
            .onStart {
                L.e("request", "请求开始--> ${Thread.currentThread().name}")
                progressDialog?.show()
            }.onCompletion { cause ->
                cause?.printStackTrace()

                L.e("request", "请求结束--> ${Thread.currentThread().name}")
                progressDialog?.close()
            }
            .catch { ex ->
                L.e("request", "请求异常--> ${Thread.currentThread().name}")
                action?.invoke(ex)
            }
    }

    /**
     * 获取get 请求 被观察者，最终返回 Flow
     *
     * 当 返回数据不匹配时候，
     * 1、复制 BeanModule 按需求 添加 属性名
     * 2、复制 ApiService 把 返回的 BeanModle 改成 自己的
     * 3、复制 HttpUtils（httpGet，postCompose，postForm）三个方法
     *
     * @param typeOfT 请求完成后 返回数据对象 [通用类型，集合，对象，都可以]
     * @param apiUrl 请求Url
     * @param params 请求体
     * @param headers 请求头
     */
    fun <T> httpGet(
        apiUrl: String = "",
        params: ArrayMap<String, Any> = ArrayMap<String, Any>(),
        typeOfT: TypeToken<T>,
        headers: ArrayMap<String, Any> = ArrayMap<String, Any>()
    ): Flow<T> {
        return flow {
            L.e("request", "请求执行--> ${Thread.currentThread().name}")

            val result = RequestUtils.create(ApiService::class.java)
                .getCompose(apiUrl, headers, params)

            emit(result)
        }
            .flowConverter(typeOfT)

//            .collect{ // 订阅
//            }
    }

    fun <T> postCompose(
        apiUrl: String = "",
        params: ArrayMap<String, Any> = ArrayMap<String, Any>(),
        typeOfT: TypeToken<T>,
        headers: ArrayMap<String, Any> = ArrayMap<String, Any>()
    ): Flow<T> {
        return flow {
            L.e("request", "请求执行--> ${Thread.currentThread().name}")

            val result = RequestUtils.create(ApiService::class.java)
                .postCompose(apiUrl, headers, params)

            emit(result)
        }
            .flowConverter(typeOfT)
    }

    fun <T> postForm(
        apiUrl: String = "",
        params: ArrayMap<String, Any> = ArrayMap<String, Any>(),
        typeOfT: TypeToken<T>,
        headers: ArrayMap<String, Any> = ArrayMap<String, Any>()
    ): Flow<T> {
        return flow {
            L.e("request", "请求执行--> ${Thread.currentThread().name}")

            val result = RequestUtils.create(ApiService::class.java)
                .postFormCompose(apiUrl, headers, params)

            emit(result)
        }
            .flowConverter(typeOfT)
    }

    /**
     * 上传文件
     * @param apiUrl 请求Url
     * @param files 文件
     * @param params 请求文本参数
     * @param typeOfT 响应数据对象
     * @param progressCallback 进度回调
     *
     * 文本参数 应用层 执行上传文件前的 请求参数配置 请严格 一一对应
     * params.put("uploadFile", "file"); //上传文件 key 值【一般是 "file" OR "files" 根据接口】
     * params["isFileKeyAES"] = false        //多文件上传时候的 文件key：是否使用 file1，file2
     * params["isTextParamJson"] = false     //是否使用 json 格式 传递文本参数
     * params["isFormSubmit"] = false           //是否表单提交
     */
    fun <T, F> uploadFile(
        apiUrl: String, files: ArrayList<F>,
        params: ArrayMap<String, Any> = ArrayMap<String, Any>(),
        typeOfT: TypeToken<T>,
        progressCallback: ((Float) -> Unit)? = null
    ): Flow<T> {

//        val channel = Channel<Float>()
//        GlobalScope.launch {
//            for (proress in channel) {
//                L.e("request", "进度--> ${proress} ${Thread.currentThread().name}")
//                progressCallback?.invoke(proress)
//            }
//        }
        return flow {
            L.e("request", "请求执行--> ${Thread.currentThread().name}")

            val item = files[0]
            if (item is String) params["filePathList"] = files
            else if (item is File) params["files"] = files
            else throw Exception("param exception")

//            params["ProgressChannel"] = channel

            val data = if(params["isFormSubmit"] == true){
                RequestUtils.create(ApiService::class.java)
                    .uploadFile(apiUrl, params)
            } else {
                val filesPart = FileRequestBodyConverter.filesToBody(files)
                RequestUtils.create(ApiService::class.java)
                    .putUploadFile(apiUrl, filesPart)
            }

//            for (proress in channel) {
//                L.e("request", "进度--> ${proress} ${Thread.currentThread().name}")
//                emit(proress)
//            }

            emit(data)
        }
            .flowConverter(typeOfT)
    }


    /**
     * 下载文件
     */
    fun downLoadFile(
        downUrl: String,
        targetPath: String,
        reNameFile: String,
        isReturnProcess: Boolean = false
    ): Flow<TransmissionState> {

        return flow {
            var filePath: String = targetPath
            filePath = if (!TextUtils.isEmpty(filePath)) {
                if (filePath.contains(AppUtils.getLocalPackageName())) { // 下载到 指定的私有目录
                    FileUtils.folderIsExists(filePath).path
                } else {
                    FileUtils.getSDCardDirectoryTpye(filePath) + ConfigUtils.getFilePath()
                }
            } else {
                FileUtils.folderIsExists(FileUtils.DOWN, 0).path
            }

            val tempFile = FileUtils.getTempFile(downUrl, filePath)

            var targetFile = FileUtils.getFile(downUrl, filePath)
            if (!TextUtils.isEmpty(reNameFile)) { // 找最终下载完成的 文件
                targetFile = File(filePath, reNameFile)
            }
            val downParam = if (targetFile.exists()) {
                targetFile.path
            } else {
                "bytes=" + tempFile.length() + "-"
            }

            val responseBody = RequestUtils.create(ApiService::class.java)
                .download(downParam, downUrl)

            val file = saveFile(responseBody, downUrl, filePath) {
                if (isReturnProcess) {
                    emit(TransmissionState.InProgress(it))
                }
            }

            emit(TransmissionState.Success(file))
        }.flowOn(Dispatchers.IO)
            .onStart {
                L.e("request", "请求开始--> ${Thread.currentThread().name}")
            }
            .onCompletion { cause ->
                L.e("request", "请求结束--> ${Thread.currentThread().name}")
            }

    }

    /**
     * 根据ResponseBody 写文件
     * @param responseBody
     * @param url
     * @param filePath     文件保存路径
     * @return
     */
    private inline fun saveFile(responseBody: ResponseBody, url: String, filePath: String, progressListener: (Float) -> Unit): File {
        var tempFile = FileUtils.getTempFile(url, filePath)

        val reName_MAP = ArrayMap<String, String>()


        var uri: Uri? = null
        if (!filePath.contains(AppUtils.getLocalPackageName())) { // 下载到 sd卡 Environment.DIRECTORY_DOWNLOADS 目录
            UriUtils.deleteFileUri("Downloads",
                MediaStore.Downloads.RELATIVE_PATH + "=? AND " + MediaStore.Downloads.DISPLAY_NAME + " =?",
                arrayOf(Environment.DIRECTORY_DOWNLOADS + "/" + ConfigUtils.getFilePath(), tempFile.name)
            )

            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.Downloads.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/" + ConfigUtils.getFilePath()
            )
            contentValues.put(MediaStore.Downloads.DISPLAY_NAME, tempFile.name)
            uri = UriUtils.createFileUri(contentValues, "Downloads")
        } else {
            tempFile = FileUtils.fileIsExists(tempFile.path)
        }


        var file: File = writeFileToDisk(responseBody, url, tempFile.absolutePath, uri, progressListener)
        try {
            val FileDownStatus = SpfAgent.init("").getInt(url + Constant.FileDownStatus)

            val renameSuccess: Boolean
            if (FileDownStatus == 4) {
                val contentValues = ContentValues()
                contentValues.put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/" + ConfigUtils.getFilePath()
                )

                val fileName = FileUtils.getFileName(url)
                var resultFile = File(filePath, fileName)
                if (reName_MAP.containsKey(url)) {
                    if (null != uri) {
                        contentValues.put(
                            MediaStore.Downloads.DISPLAY_NAME,
                            reName_MAP[url]
                        )
                        contentValues.put(
                            MediaStore.Downloads.DATA,
                            filePath + File.separator + reName_MAP[url]
                        )
                        renameSuccess = UriUtils.updateFileUri(uri, contentValues)
                        resultFile = File(filePath, reName_MAP[url])
                    } else {
                        resultFile = File(tempFile.parent, reName_MAP[url])
                        renameSuccess = tempFile.renameTo(resultFile)
                    }
                } else {
                    if (null != uri) {
                        contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        contentValues.put(
                            MediaStore.Downloads.DATA,
                            filePath + File.separator + fileName
                        )
                        renameSuccess = UriUtils.updateFileUri(uri, contentValues)
                    } else {
                        renameSuccess = FileUtils.reNameFile(url, tempFile.path)
                    }
                }

                return if (renameSuccess) {
                    resultFile
                } else {
                    tempFile
                }
            } else if (FileDownStatus == 3) { //取消下载则 删除下载内容
                FileUtils.deleteFileSafely(tempFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file
    }

    /**
     * 单线程 断点下载
     *
     * @param responseBody
     * @param filePath
     * @return
     * @throws IOException
     */
    private inline fun writeFileToDisk(responseBody: ResponseBody, url: String, filePath: String?, uri: Uri?, progressListener: (Float) -> Unit): File {
        val file = File(filePath)
        val totalByte = responseBody.contentLength()
        val fileTotalByte = file.length() + totalByte

        SpfAgent.init("").saveInt(url + Constant.FileDownStatus, 1).commit(false) //正在下载
        val buffer = ByteArray(1024 * 4)

        var `is`: InputStream? = null
        var randomAccessFile: RandomAccessFile? = null
        var out: OutputStream? = null

        try {
            `is` = responseBody.byteStream()

            if (null != uri) {
                val resolver = ConfigUtils.getAppCtx().contentResolver
                out = resolver.openOutputStream(uri)
            } else {
                val tempFileLen = file.length()
                randomAccessFile = RandomAccessFile(file, "rwd")
                randomAccessFile.seek(tempFileLen)
            }

            var downloadByte: Long = 0
            var lastTotal = 0L

            while (true) {
                val len = `is`.read(buffer)
                if (len == -1) { //下载完成
                    SpfAgent.init("").saveInt(url + Constant.FileDownStatus, 4).commit(false) //下载完成
                    break
                }

                val FileDownStatus = SpfAgent.init("").getInt(url + Constant.FileDownStatus)
                if (FileDownStatus == 2 || FileDownStatus == 3) break //暂停或者取消 停止下载


                if (null != randomAccessFile) {
                    randomAccessFile.write(buffer, 0, len)
                } else {
                    out?.write(buffer, 0, len)
                }

                downloadByte += len.toLong()
                lastTotal += len.toLong()

                if (lastTotal >= CALL_BACK_LENGTH || downloadByte >= fileTotalByte) { //避免每写4096字节，就回调一次，那未免太奢侈了，所以设定一个常量每1mb回调一次
                    progressListener((downloadByte * 100 / fileTotalByte).toFloat())
                    lastTotal = 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            FileUtils.closeIO(out, `is`, responseBody)
        }

        return file
    }


}