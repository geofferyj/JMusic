package com.geofferyj.jmusic.utils.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior


class CustomBottomSheetBehavior<V : View?>(context: Context?, attrs: AttributeSet?) :
    BottomSheetBehavior<V>(
        context!!, attrs
    ) {
    private var allowDragging = true

    fun setAllowDragging(allowDragging: Boolean) {
        this.allowDragging = allowDragging
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: V,
        event: MotionEvent
    ): Boolean {
        return if (!allowDragging) {
            false
        } else super.onInterceptTouchEvent(parent, child!!, event)
    }

    companion object {
        private const val TAG = "CustomBottomSheetBehaviour"
    }
}