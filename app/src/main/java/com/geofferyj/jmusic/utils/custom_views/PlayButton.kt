package com.geofferyj.jmusic.utils.custom_views

import android.content.Context
import android.util.AttributeSet
import com.geofferyj.jmusic.R
import com.google.android.material.floatingactionbutton.FloatingActionButton





class PlayButton : FloatingActionButton {



    private val STATE_PLAYING = intArrayOf(R.attr.state_playing)
    private var mIsPlaying = false


    fun setIsPlaying(isPlaying: Boolean){
        mIsPlaying = isPlaying
        refreshDrawableState()

    }

    fun getIsPlaying() = mIsPlaying

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    override fun onCreateDrawableState(extraSpace: Int): IntArray? {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)

        if (mIsPlaying) {
            mergeDrawableStates(drawableState, STATE_PLAYING)
        }

        return drawableState
    }

    companion object {
        private const val TAG = "PlayButton"
    }
}