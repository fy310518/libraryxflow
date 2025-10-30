package com.fy.baselibrary.utils.config

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.Window
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * description 状态栏，导航栏 工具类
 * Created by fangs on 2023/8/3 10:05.
 */
class StatusBarUtils {

    companion object{

        interface KeyboardVisibilityListener {
            fun onKeyboardVisibilityChanged(isOpen: Boolean)
        }

        /**
         * 监听键盘 显示/隐藏
         */
        fun attachKeyboardListeners(activity: Activity, listener: KeyboardVisibilityListener) {
            val rootView = activity.findViewById<View>(android.R.id.content)

            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v: View?, insets: WindowInsetsCompat ->
                val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                listener.onKeyboardVisibilityChanged(imeHeight > 0)
                insets
            }
        }


        /**
         * 控制键盘 显示/隐藏
         * @param isVisible 是否显示
         */
        fun setKeyBoardVisible(window: Window, editText: EditText, isVisible: Boolean){
            WindowCompat.getInsetsController(window, editText).let { controller ->
                if(isVisible){
                    controller.show(WindowInsetsCompat.Type.ime())
                } else {
                    controller.hide(WindowInsetsCompat.Type.ime())
                }
            }
        }

        /**
         * 控制状态栏、导航栏 显示/隐藏
         * activity.window dialog?.window
         * @param isVisible 是否显示
         */
        fun setStatusBarVisible(window: Window, isVisible: Boolean) {
            WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                if (isVisible) {
                    controller.show(WindowInsetsCompat.Type.statusBars())
                    controller.show(WindowInsetsCompat.Type.navigationBars())
                } else {
                    controller.hide(WindowInsetsCompat.Type.statusBars())
                    controller.hide(WindowInsetsCompat.Type.navigationBars())
                }
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        /**
         *  设置状态栏 & 导航栏 背景颜色
         *  这里还是直接操作window的statusBarColor
         */
        fun setStatusBarColor(window: Window, @ColorInt statusBarColor: Int, @ColorInt navigationBarColor: Int) {
            setStatusBarBgColor(window, statusBarColor)

            setNavigationBars(window, navigationBarColor)
        }

        /**
         *  沉浸式状态栏
         *  @param contentColor 设置内容颜色:获取内容的颜色，传入系统，它自动修改字体颜色(黑/白)
         */
        fun immersiveStatusBar(window: Window, @ColorInt contentColor: Int) {
            // 设置状态栏字体颜色
            setStatusBarBgColor(window, contentColor)
            setNavigationBars(window, contentColor)

            // 背景色 设置透明
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT

            WindowCompat.setDecorFitsSystemWindows(window, false)

//            activity.findViewById<FrameLayout>(android.R.id.content).apply {
//                ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
//                    val params = view.layoutParams as LinearLayout.LayoutParams
//                    params.topMargin = insets.systemWindowInsetTop
//                    insets
//                }
//            }
        }

        /**
         *  设置状态栏背景色 & 字体颜色（字体颜色是根据 背景色计算） 此api只能控制字体颜色为 黑/白
         *  @param color 这里的颜色是指背景颜色
         */
        fun setStatusBarBgColor(window: Window, @ColorInt color: Int) {
            window.statusBarColor = color
            // 计算颜色亮度
            val luminanceValue = ColorUtils.calculateLuminance(color)
            WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                if (color == Color.TRANSPARENT) {
                    // 如果是透明颜色就默认设置成黑色
                    controller.isAppearanceLightStatusBars = true
                } else {
                    // 通过亮度来决定字体颜色是黑还是白
                    controller.isAppearanceLightStatusBars = luminanceValue >= 0.5
                }
            }
        }

        /**
         *  设置导航栏背景色 & 字体颜色（字体颜色是根据 背景色计算） 此api只能控制字体颜色为 黑/白
         *  @param navigationBarColor 这里的颜色是指背景颜色
         */
        fun setNavigationBars(window: Window, @ColorInt navigationBarColor: Int) {
            window.navigationBarColor = navigationBarColor

            val luminanceValue = ColorUtils.calculateLuminance(navigationBarColor)
            WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                if (navigationBarColor == Color.TRANSPARENT) {
                    controller.isAppearanceLightNavigationBars = true
                } else {
                    controller.isAppearanceLightNavigationBars = luminanceValue >= 0.5
                }
            }
        }

    }
}