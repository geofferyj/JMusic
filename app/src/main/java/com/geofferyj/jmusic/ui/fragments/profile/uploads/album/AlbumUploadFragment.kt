package com.geofferyj.jmusic.ui.fragments.profile.uploads.album

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.geofferyj.jmusic.R
import com.geofferyj.jmusic.databinding.FragmentAlbumUploadBinding


class AlbumUploadFragment : Fragment() {
   private lateinit var binding: FragmentAlbumUploadBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlbumUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

}