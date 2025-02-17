package com.fy.baselibrary.retrofit.request

import com.fy.baselibrary.retrofit.observer.BaseBean
import com.google.gson.annotations.SerializedName

/**
 * desc api 返回数据 模板 bean（当 返回不匹配时候，自己 复制一下这个类 在 alternate 数组中添加 属性名）
 * @author fy
 * @time 2020/11/19  15:01
 */
data class BeanModule<T>(
    @SerializedName("message", alternate = ["msg", "errorMsg", "resultMessage"])
    val message: String,

    @SerializedName("code", alternate = ["errorCode", "resultCode"])
    val code: Int,

    @SerializedName("result", alternate = ["data", "resultData", "list"])
    val result: T
) : BaseBean<T> {

    override fun getResultCode() = code

    override fun getResultMsg() = message

    override fun getResultData() = result

    override fun isSuccess() = code == 0

}
