package com.geofferyj.jmusic.ui.fragments.profile.uploads

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.geofferyj.jmusic.R
import com.geofferyj.jmusic.databinding.FragmentUploadsBinding

class UploadsFragment : Fragment() {

    private lateinit var binding: FragmentUploadsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addSong.setOnClickListener {
            findNavController().navigate(R.id.singleUploadFragment)
        }

        binding.addAlbum.setOnClickListener {
            findNavController().navigate(R.id.albumUploadFragment)
        }
    }

}