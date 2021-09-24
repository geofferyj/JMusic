package com.geofferyj.jmusic.player.callbacks

import android.widget.Toast
import com.geofferyj.jmusic.player.services.MusicService
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player

class MusicPlayerEventListener(
    private val musicService: MusicService
) : Player.Listener {

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)

        if (!playWhenReady && reason == Player.STATE_READY){

            musicService.stopForeground(false)

        }

    }
}