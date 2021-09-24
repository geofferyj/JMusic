package com.geofferyj.jmusic.player

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import com.geofferyj.jmusic.R
import com.geofferyj.jmusic.utils.Constants
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * A wrapper class for ExoPlayer's PlayerNotificationManager. It sets up the notification shown to
 * the user during audio playback and provides track metadata, such as track title and icon image.
 */
class JNotificationManager(

    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val newSongCallback: (currentMediaItem: MediaItem?) -> Unit
) {

    private var player: Player? = null
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val notificationManager: PlayerNotificationManager
    private val platformNotificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)

        notificationManager = PlayerNotificationManager.Builder(
            context,
            Constants.NOTIFICATION_ID,
            Constants.NOTIFICATION_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            .setNotificationListener(notificationListener)
            .setChannelDescriptionResourceId(R.string.notification_channel_description)
            .setChannelNameResourceId(R.string.notification_channel_name)
            .build().apply {
                setMediaSessionToken(sessionToken)

//            setSmallIcon(R.drawable.ic_notification)


                // Don't display the rewind or fast-forward buttons.
                setUseRewindAction(false)
                setUseFastForwardAction(false)
            }
    }

    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    fun showNotificationForPlayer(player: Player) {
        notificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {

        override fun getCurrentContentTitle(player: Player): CharSequence {
            newSongCallback(player.currentMediaItem)
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


    companion object {
        private const val TAG = "JNotificationManager"
    }
}