package com.geofferyj.jmusic.ui.fragments.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.geofferyj.jmusic.R
import com.geofferyj.jmusic.data.models.Song
import com.geofferyj.jmusic.databinding.FragmentDiscoverBinding
import com.geofferyj.jmusic.ui.viewmodels.MainViewModel
import com.geofferyj.jmusic.utils.Status
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class DiscoverFragment : Fragment() {

    private val viewModel: MainViewModel by viewModel()
    private lateinit var binding: FragmentDiscoverBinding
    private val songRvAdapter: SongRvAdapter by inject()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()

        val divider =
            AppCompatResources.getDrawable(requireContext(), R.drawable.rv_item_divider_light)
        val decorator = DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL).apply {

            setDrawable(divider!!)
        }

        binding.recyclerView.addItemDecoration(decorator)

        binding.recyclerView.adapter = songRvAdapter
        songRvAdapter.setItemClickListener {

            val bundle = bundleOf(
                Pair("CURRENT_PLAY_LIST", songRvAdapter.differ.currentList)
            )
            viewModel.playOrToggleSong(it, extras = bundle)
        }

        getSongs()
    }

    private fun getSongs() {

        Firebase.firestore.collection("songs")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                val songsList = mutableListOf<Song>()
                value?.forEach {
                    val song: Song = it.toObject()
                    songsList.add(song)
                }
                songRvAdapter.differ.submitList(songsList)

            }
    }

    private fun subscribeToObservers() {
        viewModel.mediaItems.observe(viewLifecycleOwner) { result ->


            when (result.status) {

                Status.SUCCESS -> {

//                    allSongsProgressBar.isVisible = false
                    result.data?.let { songs ->

//                        songRvAdapter.differ.submitList(songs)
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> {
//                    allSongsProgressBar.isVisible = true
                }
            }
        }
    }

    companion object {
        private const val TAG = "DiscoverFragment"
    }
}