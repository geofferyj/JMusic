package com.geofferyj.jmusic.player

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import com.geofferyj.jmusic.R
import com.geofferyj.jmusic.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.geofferyj.jmusic.utils.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val newSongCallback: () -> Unit

) {
    private lateinit var notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)
        notificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        )
            .setNotificationListener(notificationListener)
            .setChannelDescriptionResourceId(R.string.notification_channel_description)
            .setChannelNameResourceId(R.string.notification_channel_name)
            .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            .build().apply {
                setMediaSessionToken(sessionToken)
            }
    }

    fun showNotification(player: Player){
        notificationManager.setPlayer(player)
    }
    private inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return mediaController.metadata.description.subtitle.toString()
        }


        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val request = ImageRequest.Builder(context)
                .data(mediaController.metadata.description.iconUri)
                .target(
                    onStart = { placeholder ->
                        // Handle the placeholder drawable.
                    },
                    onSuccess = { result ->
                        callback.onBitmap(result.toBitmap())
                    },
                    onError = { error ->
                        // Handle the error drawable.
                    }
                )
                .build()

            val imageLoader = ImageLoader.Builder(context).build()
            imageLoader.enqueue(request)

            return null
        }
    }
}