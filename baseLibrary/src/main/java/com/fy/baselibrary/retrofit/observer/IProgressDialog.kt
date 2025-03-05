package com.fy.baselibrary.retrofit.observer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.fy.baselibrary.base.dialog.CommonDialog
import com.fy.baselibrary.retrofit.NetAnimListener
import kotlinx.coroutines.CoroutineScope

/**
 * 自定义对话框的dialog
 * Created by fangs on 2017/11/7.
 */
class IProgressDialog() {
    protected var mContext: Context? = null

    /** 传递进来的 环境（AppCompatActivity or Fragment）  */
    var obj: Any? = null

    var mDialog: CommonDialog<*, *>? = null

    protected var netAnimListener: NetAnimListener? = null

    constructor(obj: Any, netAnimListener: NetAnimListener): this() {
        this.obj = obj
        this.netAnimListener = netAnimListener
    }

    constructor(obj: Any, dialog: CommonDialog<*, *>): this() {
        this.obj = obj
        this.mDialog = dialog
    }


    private fun runShowDialog(manager: FragmentManager){
        mDialog?.apply {
            if((null == dialog || dialog!!.isShowing)){
                show(manager, javaClass.name)
            }
        }
    }

    /**
     * 显示对话框
     */
    fun show() {
        obj?.apply {
            when (this) {
                is AppCompatActivity -> {
                    mContext = obj as AppCompatActivity
                    runShowDialog(supportFragmentManager)
                }
                is Fragment -> {
                    mContext = (obj as Fragment).context
                    runShowDialog(childFragmentManager)
                }
            }
        }
    }

    /**
     * 关闭对话框
     */
    fun close() {
        if (null != mDialog && null != mContext) {
            mDialog?.dismiss(false)
        } else if (null != netAnimListener && null != mContext) {
            netAnimListener?.stop()
        }
    }
}
