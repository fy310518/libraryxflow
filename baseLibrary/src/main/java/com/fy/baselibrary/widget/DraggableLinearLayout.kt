package com.fy.baselibrary.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.customview.widget.ViewDragHelper
import com.fy.baselibrary.utils.notify.L

/**
 * 拖拽 ViewGroup
 */
class DraggableLinearLayout(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    // View 是否向左拖拽
    private var isToLeftMove = false
    private var onCloseListener: (() -> Unit)? = null

    private var viewDragHelper: ViewDragHelper? = null

    // 拖拽距离 大于 moveDistance 松开关闭，小于 moveDistance 恢复原位，单位 px
    private var moveDistance = 150f

    init {
        init()
    }

    private var moveLeft = 0 // 记录移动的距离
    private fun init() {
        viewDragHelper = ViewDragHelper.create(this, 1.0f, object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return true
            }

            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
                L.e("clampViewPositionHorizontal", "left: $left, dx: $dx")

                if(isToLeftMove){ //向左拖拽
                    if (left > 0) {
                        return 0 // 不移动
                    }
                } else {
                    if (left < 0) {
                        return 0
                    }
                }

                moveLeft = left

                return left
            }

//            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
//                return top
//            }

            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
                super.onViewReleased(releasedChild, xvel, yvel)
                if(moveLeft > moveDistance){ //超过150dp,则关闭
                    L.e("clampViewPositionHorizontal", "close")
                    onCloseListener?.invoke()
                    return
                }

                //调用这个方法,就可以设置releasedChild回弹的位置
                viewDragHelper?.settleCapturedViewAt(0, 0) //参数就是x,y的坐标
                postInvalidate() //注意一定要调用这个方法,否则没效果.
            }
        })
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return viewDragHelper?.shouldInterceptTouchEvent(ev) ?: super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        viewDragHelper?.processTouchEvent(event)
        return true
    }


    override fun computeScroll() {
//        super.computeScroll()
        if (viewDragHelper?.continueSettling(true) == true) {
            postInvalidate() //注意此处.
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        moveDistance = (w * 0.35).toFloat()
    }


    /**
     * 设置监听
     * @param isToLeftMove 是否向左 拖拽 关闭
     * @param listener 关闭监听
     */
    fun setOnCloseListener(isToLeftMove: Boolean?, listener: () -> Unit) {
        isToLeftMove?.let {
            this.isToLeftMove = it
        }
        this.onCloseListener = listener
    }
}