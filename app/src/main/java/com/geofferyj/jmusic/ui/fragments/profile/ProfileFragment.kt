package com.geofferyj.jmusic.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.geofferyj.jmusic.R
import com.geofferyj.jmusic.databinding.FragmentProfileBinding
import com.geofferyj.jmusic.ui.fragments.profile.favourites.FavouritesFragment
import com.geofferyj.jmusic.ui.fragments.profile.followers.FollowersFragment
import com.geofferyj.jmusic.ui.fragments.profile.following.FollowingFragment
import com.geofferyj.jmusic.ui.fragments.profile.uploads.UploadsFragment
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val profileVPAdapter: ProfileVPAdapter by inject {
        parametersOf(childFragmentManager, lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vp = binding.vp
        val tabLayout = binding.tabLayout

        vp.adapter = profileVPAdapter

        TabLayoutMediator(tabLayout, vp) { tab, position ->
            tab.text = when(position){
                0 -> "Favourites"
                1 -> "Followers"
                2 -> "Following"
                3 -> "Uploads"
                else -> ""
            }
        }.attach()

        binding.materialToolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.submit_proof ->{
                findNavController().navigate("jmusic://payment/submit-proof".toUri())
                }

            }
            true
        }
    }

    override fun onPause() {
        super.onPause()

        binding.vp.adapter = null
    }
}