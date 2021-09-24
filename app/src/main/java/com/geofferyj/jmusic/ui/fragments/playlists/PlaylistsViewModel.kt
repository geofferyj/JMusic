package com.geofferyj.jmusic.ui.fragments.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlaylistsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Playlists Coming soon"
    }
    val text: LiveData<String> = _text
}