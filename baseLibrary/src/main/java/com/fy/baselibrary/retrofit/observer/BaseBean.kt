package com.fy.baselibrary.retrofit.observer

/**
 * 网络请求 返回数据 格式化接口
 */
interface BaseBean<T> {

    /**
     * 获取请求返回对象
     */
    open fun getResultData(): T?
    /**
     * 获取请求返回信息
     */
    open fun getResultMsg(): String
    /**
     * 获取请求返回状态码
     */
    open fun getResultCode(): Int

    /**
     * 判断请求是否成功
     * @return true/false
     */
    open fun isSuccess(): Boolean
}