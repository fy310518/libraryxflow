package com.fy.baselibrary.retrofit.request

import android.util.ArrayMap
import com.google.gson.reflect.TypeToken

/**
 * 离线
 */
interface HttpOffline {

    /**
     * 从数据库查询所有数据
     * @param typeOfT  返回类型
     * @param params   查询参数
     */
    suspend fun <T> queryAllData(typeOfT: TypeToken<T>, params: ArrayMap<String, Any> = ArrayMap<String, Any>()): T?

    /**
     * 保存网络数据到数据库
     */
    suspend fun <T> saveDataToDb(netResultData: T)


}