package com.geofferyj.jmusic.ui.viewmodels

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.*
import com.geofferyj.jmusic.data.models.Song
import com.geofferyj.jmusic.player.MusicServiceConnection
import com.geofferyj.jmusic.player.extensions.currentPlaybackPosition
import com.geofferyj.jmusic.player.extensions.isPlayEnabled
import com.geofferyj.jmusic.player.extensions.isPlaying
import com.geofferyj.jmusic.player.extensions.isPrepared
import com.geofferyj.jmusic.player.services.JMusicService
import com.geofferyj.jmusic.utils.Constants.MEDIA_ROOT_ID
import com.geofferyj.jmusic.utils.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import com.geofferyj.jmusic.utils.Resource
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class MainViewModel(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.curPlayingSong
    var currentSong = MutableLiveData<Song?>()

    val playbackState = musicServiceConnection.playbackState

    var playerPositionJob: Job? = null

    private val _curSongDuration = MutableLiveData<Long>()
    val curSongDuration: LiveData<Long> = _curSongDuration

    private val _curPlayerPosition = MutableLiveData<Long>()
    val curPlayerPosition: LiveData<Long> = _curPlayerPosition

    init {
        updateCurrentPlayerPosition()
    }

    private fun updateCurrentPlayerPosition() {
        playerPositionJob = viewModelScope.launch {
            while (true) {
                val pos = playbackState.value?.currentPlaybackPosition
                if (curPlayerPosition.value != pos) {
                    _curPlayerPosition.postValue(pos!!)
                    _curSongDuration.postValue(JMusicService.curSongDuration)
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }

    init {
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)
                    val items = children.map {
                        Song(
                            id = it.mediaId!!,
                            title = it.description.title.toString(),
                            fileUrl = it.description.mediaUri.toString(),
                            albumArt = it.description.iconUri.toString(),
                        )
                    }
                    _mediaItems.postValue(Resource.success(items))
                }
            })

    }

    fun getCurrentSongData(id: String?) {
        viewModelScope.launch {

            Timber.d("Current Song Id: $id")
            if (id.isNullOrBlank()) return@launch
            val song: Song? = Firebase.firestore.collection("songs").document(id).get().await().toObject()
            currentSong.postValue(song)
        }
    }


    fun skipToNextSong() {
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun play() {
        musicServiceConnection.transportControls.play()
    }

    fun pause() {
        musicServiceConnection.transportControls.pause()
    }

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = true, extras: Bundle? = null) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        val currentPlayingSong = curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)
        if (isPrepared && (mediaItem.id == currentPlayingSong)) {
            playbackState.value?.let { playbackState ->

                when {
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.id, extras)
        }
    }


    fun playOrPause() {
        val isPrepared = playbackState.value?.isPrepared ?: false
        val currentPlayingSong = curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)
        if (isPrepared && (currentPlayingSong != null)) {
            playbackState.value?.let { playbackState ->

                when {
                    playbackState.isPlaying -> musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }
}







