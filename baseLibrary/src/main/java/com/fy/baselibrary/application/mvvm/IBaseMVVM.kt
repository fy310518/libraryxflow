package com.fy.baselibrary.application.mvvm

import android.app.Activity
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.AndroidViewModel
import com.fy.baselibrary.utils.ScreenUtils

/**
 * description：mvvm 架构 之 activity 实现接口 统一规范；
 * 项目自己创建的 activity 建议实现 此 接口
 * Created by fangs on 2022/7/4 16:58.
 */
interface IBaseMVVM<VM : AndroidViewModel, VDB : ViewDataBinding> {
    /**
     * 设置 activity 布局 ID
     */
    @LayoutRes
    fun setContentLayout(): Int

    /**
     * 在 setContentLayout 之前执行，屏幕适配，横竖屏 适配 用到
     */
    @LayoutRes
    fun executeBefore(): Int {
        if(this is Activity){
            ScreenUtils.screenAdapter(this)
        }
        return setContentLayout()
    }

    /**
     * 初始化
     * 注：在 activity 声明 binding: VDB 和 viewModel: VM 变量，并用 viewModel dataBinding 赋值
     */
    fun initData(viewModel: VM, dataBinding: VDB, savedInstanceState: Bundle?)
}
