package com.fy.baselibrary.widget

import android.os.CountDownTimer
import com.fy.baselibrary.utils.notify.L

/**
 * 倒计时
 */
abstract class AppCountDownTimer(countDownTimer: Long, time: Long): CountDownTimer(countDownTimer, time) {
    val TAG = "AppCountDownTimer"

    private var isCancelled = false

    override fun onFinish() {
        if(!isCancelled){
            L.e(TAG, "CountDownTimer end")
            onFinished()
        } else {
            L.e(TAG, "CountDownTimer cancel，不执行结束逻辑")
        }
    }

    fun cancelTimer(){
        isCancelled = true
        cancel()
        L.e(TAG, "CountDownTimer cancel")
    }

    abstract fun onFinished()

}