package com.fy.baselibrary.widget

import android.view.MotionEvent
import android.view.View
import android.widget.EditText

/**
 * editText 滑动冲突处理
 * mEditText.setOnTouchListener(EditTextTouchListener(R.id.mEditText, mEditText))
 */
class EditTextTouchListener(var editId: Int, var mEditText: EditText): View.OnTouchListener {
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        // 当触摸的是EditText & 当前EditText可滚动时，则将事件交给EditText处理；
        if ((v.id == editId && canVerticalScroll(mEditText))) {
            v.parent.requestDisallowInterceptTouchEvent(true);
            // 否则将事件交由其父类处理
            if (event.action == MotionEvent.ACTION_UP) {
                v.parent.requestDisallowInterceptTouchEvent(false);
            }
        }
        return false
    }

    // 判断当前EditText是否可滚动
    private fun canVerticalScroll(editText: EditText): Boolean {
        return editText.lineCount > editText.maxLines
    }

}