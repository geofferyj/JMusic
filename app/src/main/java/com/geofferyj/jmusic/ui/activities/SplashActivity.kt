package com.geofferyj.jmusic.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.geofferyj.jmusic.R
import com.geofferyj.jmusic.databinding.ActivitySplashBinding
import com.geofferyj.jmusic.utils.custom_views.MultiSheetView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sothree.slidinguppanel.SlidingUpPanelLayout

class SplashActivity : AppCompatActivity() {
    private val binding:ActivitySplashBinding by lazy{
        ActivitySplashBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.multisheettest)
        setContentView(binding.root)
//        binding.btnPlayPause.setOnClickListener {
//            val btn = (it as FloatingActionButton)
//            btn.hide()
//            btn.isActivated = !btn.isActivated
//            btn.show()
//        }
//
//        binding.btnFavourite.setOnClickListener {
//            val btn = (it as FloatingActionButton)
//            btn.hide()
//            btn.isActivated = !btn.isActivated
//            btn.show()
//        }

    }




    companion object{
        private const val TAG = "SplashActivity"
    }
}