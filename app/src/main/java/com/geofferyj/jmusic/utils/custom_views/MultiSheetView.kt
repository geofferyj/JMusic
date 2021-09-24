package com.geofferyj.jmusic.utils.custom_views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.Nullable
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.geofferyj.jmusic.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.tabs.TabLayout


class MultiSheetView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
    CoordinatorLayout(context!!, attrs, defStyleAttr) {
    internal annotation class Sheet {
        companion object {
            var NONE = 0
            var FIRST = 1
            var SECOND = 2
        }
    }

    private var bottomSheetBehavior1: CustomBottomSheetBehavior<*>? = null
    private var bottomSheetBehavior2: CustomBottomSheetBehavior<*>? = null

    constructor(context: Context) : this(context, null) {}
    constructor(context: Context, @Nullable attrs: AttributeSet?) : this(context, attrs, 0) {}

    override fun onFinishInflate() {
        super.onFinishInflate()
        val sheet1 = findViewById<View>(R.id.player_main)
        bottomSheetBehavior1 = BottomSheetBehavior.from(sheet1) as CustomBottomSheetBehavior<*>
        val sheet2 = findViewById<View>(R.id.comments_layout)
        bottomSheetBehavior2 = BottomSheetBehavior.from(sheet2) as CustomBottomSheetBehavior<*>
        bottomSheetBehavior2!!.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior1!!.setAllowDragging(false)
                } else {
                    bottomSheetBehavior1!!.setAllowDragging(true)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                bottomSheetBehavior1!!.setAllowDragging(false)
            }
        })

        //First sheet view click listener
        findViewById<View>(sheet1PeekViewResId).setOnClickListener {
            bottomSheetBehavior1!!.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // FIXME:
        // This click listener (combined with a nested RecyclerView in Sheet 2's container), causes
        // the second peek view to stop responding to drag events.
        // See `Sheet2Controller`. Remove this ClickListener here to see things working as they should.

        //Second sheet view click listener
        (findViewById<View>(sheet2PeekViewResId) as TabLayout).addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                bottomSheetBehavior2!!.state = BottomSheetBehavior.STATE_EXPANDED

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                bottomSheetBehavior2!!.state = BottomSheetBehavior.STATE_EXPANDED

            }
        })

        // FIXED:
        // issue was  bottomSheetBehavior1 is taking drag event when getSheet2PeekView is dragging
        // so detect touch event  getSheet2PeekView set bottomSheetBehavior1 dragging false and bottomSheetBehavior2 true
        val tabStrip =  (findViewById<View>(sheet2PeekViewResId) as TabLayout).getChildAt(0)
        if (tabStrip is ViewGroup) {
            val childCount = tabStrip.childCount
            for (i in 0 until childCount) {
                val tabView = tabStrip.getChildAt(i)
                tabView.setOnTouchListener { v, event ->
                    Log.e(TAG, "onTouch: ")
                    bottomSheetBehavior1!!.setAllowDragging(false)
                    bottomSheetBehavior2!!.setAllowDragging(true)
                    false
                }
            }
        }

    }

    @get:Sheet
    val currentSheet: Int
        get() = when {
            bottomSheetBehavior2!!.state == BottomSheetBehavior.STATE_EXPANDED -> Sheet.SECOND
            bottomSheetBehavior1!!.state == BottomSheetBehavior.STATE_EXPANDED -> Sheet.FIRST
            else -> Sheet.NONE
        }

    private fun showSheet(@Sheet sheet: Int) {

        // if we are already at our target panel, then don't do anything
        if (sheet == currentSheet) {
            return
        }
        when (sheet) {
            Sheet.NONE -> {
                bottomSheetBehavior2!!.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetBehavior1!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
            Sheet.FIRST -> {
                bottomSheetBehavior2!!.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetBehavior1!!.setState(BottomSheetBehavior.STATE_EXPANDED)
            }
            Sheet.SECOND -> {
                bottomSheetBehavior2!!.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetBehavior1!!.setState(BottomSheetBehavior.STATE_EXPANDED)
            }
        }
    }

    fun consumeBackPress(): Boolean {
        when (currentSheet) {
            Sheet.SECOND -> {
                showSheet(Sheet.FIRST)
                return true
            }
            Sheet.FIRST -> {
                showSheet(Sheet.NONE)
                return true
            }
            Sheet.NONE -> {
            }
        }
        return false
    }

    @get:IdRes
    val sheet1PeekViewResId: Int
        get() = R.id.mini_player_main

    @get:IdRes
    val sheet2PeekViewResId: Int
        get() = R.id.stats_tab

    companion object {
        private const val TAG = "MultiSheetView"
    }
}