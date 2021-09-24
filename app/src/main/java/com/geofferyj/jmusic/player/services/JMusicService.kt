package com.geofferyj.jmusic.player.services

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.geofferyj.jmusic.R
import com.geofferyj.jmusic.data.models.Song
import com.geofferyj.jmusic.player.JNotificationManager
import com.geofferyj.jmusic.player.extensions.id
import com.geofferyj.jmusic.player.extensions.mediaUri
import com.geofferyj.jmusic.player.music_sources.PlayListSource
import com.geofferyj.jmusic.utils.Constants
import com.geofferyj.jmusic.utils.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.android.scope.serviceScope
import org.koin.core.scope.Scope
import timber.log.Timber

class JMusicService : MediaBrowserServiceCompat(), AndroidScopeComponent {


    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()

    private var isForegroundService: Boolean = false
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var notificationManager: JNotificationManager
    private lateinit var mediaSession: MediaSessionCompat

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var playCountJob: Job = Job()
    private var currentSongId: String = ""

    override val scope: Scope by serviceScope()
    val player: SimpleExoPlayer by inject()

    val playListSource: PlayListSource by inject()

    private val playerListener = PlayerEventListener()


    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            playListSource.initPlaylist()
        }
        // Build a PendingIntent that can be used to launch the UI.
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        // Create a new MediaSession.
        mediaSession = MediaSessionCompat(this, "MusicService")
            .apply {
                setSessionActivity(sessionActivityPendingIntent)
                isActive = true
            }

        sessionToken = mediaSession.sessionToken

        notificationManager = JNotificationManager(
            this,
            mediaSession.sessionToken,
            PlayerNotificationListener()
        ) {

            curSongDuration = player.duration

        }


        // ExoPlayer will manage the MediaSession for us.
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(JPlaybackPreparer())
        mediaSessionConnector.setQueueNavigator(JQueueNavigator(mediaSession))
        mediaSessionConnector.setPlayer(player)


        notificationManager.showNotificationForPlayer(player)

        player.addListener(playerListener)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("MEDIA_ROOT_ID", null)
    }


    /**
     * Load the supplied list of songs and the song to play into the current player.
     */

    private fun resetPlayer() {
        player.stop()
        player.clearMediaItems()
    }


    private fun preparePlaylist(
        metadataList: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playWhenReady: Boolean
    ) {
        val mediaItems = metadataList.map {
            MediaItem.fromUri(it.mediaUri)
        }
        resetPlayer()
        val initialWindowIndex = if (itemToPlay == null) 0 else metadataList.indexOf(itemToPlay)
        currentPlaylistItems = metadataList
        player.addMediaItems(mediaItems)
        player.prepare()
        player.seekTo(initialWindowIndex, 0L)
        player.playWhenReady = playWhenReady

    }


    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            Constants.MEDIA_ROOT_ID -> {
                val resultsSent = playListSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(playListSource.asMediaItems())

                    } else {
                        mediaSession.sendSessionEvent(Constants.NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }
                if (!resultsSent) {
                    result.detach()
                }
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        resetPlayer()
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }

        // Cancel coroutines when the service is going away.
        serviceJob.cancel()

        // Free ExoPlayer resources.
        player.removeListener(playerListener)
        player.release()
    }

    private inner class PlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, this@JMusicService.javaClass)
                )

                startForeground(NOTIFICATION_ID, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    /**
     * Listen for events from ExoPlayer.
     */
    private inner class PlayerEventListener : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

            if (!playWhenReady) {
                playCountJob.cancel()
            } else {
                startCounter()
            }
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    notificationManager.showNotificationForPlayer(player)
                    if (playbackState == Player.STATE_READY) {

                        if (!playWhenReady) {
                            // If playback is paused we remove the foreground state which allows the
                            // notification to be dismissed. An alternative would be to provide a
                            // "close" button in the notification which stops playback and clears
                            // the notification.
                            stopForeground(false)
                            isForegroundService = false
                        }
                    }
                }

                else -> {
                    notificationManager.hideNotification()
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Toast.makeText(applicationContext, R.string.generic_error, Toast.LENGTH_LONG).show()
        }


        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            startCounter()
        }
    }

    fun startCounter() {
        val currentSong = currentPlaylistItems[player.currentWindowIndex]


        if (currentSong.id != null) {

            // if this is a different song
            if (currentSongId != currentSong.id) {

                // set current song id to new song id
                currentSongId = currentSong.id!!

                // if the job is still running, cancel it
                if (playCountJob.isActive) {
                    playCountJob.cancel()
                }


                // start job for new music
                playCountJob = serviceScope.launch {
                    Timber.d("called begin wait")
                    delay(5000)
                    commitViewCount(currentSongId)
                    Timber.d("called counted")
                }
            }
        }

    }

    fun commitViewCount(id: String) {
        Firebase.firestore.collection("songs")
            .document(id)
            .update("stats.plays", FieldValue.increment(1))

    }

    private inner class JQueueNavigator(
        mediaSession: MediaSessionCompat
    ) : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat =
            currentPlaylistItems[windowIndex].description
    }

    private inner class JPlaybackPreparer : MediaSessionConnector.PlaybackPreparer {

        override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

        override fun onPrepare(playWhenReady: Boolean) {
            // TODO: Add settings for current playing item
        }


        override fun onPrepareFromMediaId(
            mediaId: String,
            playWhenReady: Boolean,
            extras: Bundle?
        ) {


            val playlist: List<Song> = extras?.getParcelableArrayList("CURRENT_PLAY_LIST")!!

            playListSource.changePlaylist(playlist)

            playListSource.whenReady {
                val itemToPlay: MediaMetadataCompat? = playListSource.songs.find { item ->
                    item.id == mediaId
                }

                if (itemToPlay == null) {
                    Timber.w("onPrepareFromMediaId: ItemToPlay Not Found: $itemToPlay")
                    Timber.w("onPrepareFromMediaId: Item Not Found: $mediaId")

                } else {
                    preparePlaylist(
                        playListSource.songs,
                        itemToPlay,
                        playWhenReady
                    )

                    notifyChildrenChanged(Constants.MEDIA_ROOT_ID)
                }
            }

        }


        override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {}

        override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit

        override fun onCommand(
            player: Player,
            controlDispatcher: ControlDispatcher,
            command: String,
            extras: Bundle?,
            cb: ResultReceiver?
        ) = false

    }


    companion object {
        var curSongDuration = 0L
            private set

        private const val TAG = "MusicService"
    }
}