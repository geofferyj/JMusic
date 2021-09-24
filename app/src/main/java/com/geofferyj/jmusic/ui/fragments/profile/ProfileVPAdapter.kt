package com.geofferyj.jmusic.ui.fragments.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProfileVPAdapter(private val frags: List<Fragment>, fm: FragmentManager, lc: Lifecycle) :
    FragmentStateAdapter(fm, lc) {

    override fun getItemCount(): Int = frags.size

    override fun createFragment(position: Int): Fragment = frags[position]

    companion object {
        private const val TAG = "ProfileVPAdapter"
    }
}