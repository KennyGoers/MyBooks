package com.trixiesoft.mybooks

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class TouchyRecyclerView: RecyclerView {

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
    }

    var monitorTouch: Boolean = false
    var onTouched: OnTouched? = null

    interface OnTouched {
        fun onTouched()
    }

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        val retVal = super.onInterceptTouchEvent(e)

        if (monitorTouch && e?.actionMasked != MotionEvent.ACTION_DOWN)
            onTouched?.onTouched()

        return retVal
    }
}