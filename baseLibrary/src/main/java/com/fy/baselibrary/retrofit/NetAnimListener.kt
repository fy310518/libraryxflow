package com.fy.baselibrary.retrofit

/**
 * 网络请求动画监听
 */
interface NetAnimListener {
    /**
     * 定义 启动动画
     * 设置 currentType
     */
    open fun start(currentType: Int)

    /**
     * 停止动画
     */
    open fun stop()
}