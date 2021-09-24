package com.geofferyj.jmusic.ui.fragments.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FeedViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Feeds will appear here when available"
    }
    val text: LiveData<String> = _text
}