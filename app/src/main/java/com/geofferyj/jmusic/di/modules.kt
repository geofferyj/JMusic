package com.geofferyj.jmusic.di

import androidx.fragment.app.Fragment
import com.geofferyj.jmusic.auth.AuthViewModel
import com.geofferyj.jmusic.data.databases.MusicDatabase
import com.geofferyj.jmusic.player.MusicServiceConnection
import com.geofferyj.jmusic.player.music_sources.FirebaseMusicSource
import com.geofferyj.jmusic.player.music_sources.PlayListSource
import com.geofferyj.jmusic.player.services.JMusicService
import com.geofferyj.jmusic.player.services.MusicService
import com.geofferyj.jmusic.ui.fragments.discover.SongRvAdapter
import com.geofferyj.jmusic.ui.fragments.profile.ProfileVPAdapter
import com.geofferyj.jmusic.ui.fragments.profile.favourites.FavouritesFragment
import com.geofferyj.jmusic.ui.fragments.profile.followers.FollowersFragment
import com.geofferyj.jmusic.ui.fragments.profile.following.FollowingFragment
import com.geofferyj.jmusic.ui.fragments.profile.uploads.UploadsFragment
import com.geofferyj.jmusic.ui.viewmodels.MainViewModel
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    factory {
        SongRvAdapter()
    }

    factory {
        listOf<Fragment>(
            FavouritesFragment(),
            FollowersFragment(),
            FollowingFragment(),
            UploadsFragment()
        )
    }

    factory {

        ProfileVPAdapter(get(), get(), get())
    }

    factory {
        FirebaseMusicSource(get())
    }

    factory {
        PlayListSource()
    }

    factory {
        MusicDatabase()
    }

    viewModel {
        MainViewModel(get())
    }

    viewModel {
        AuthViewModel()
    }

    single {
        MusicServiceConnection(get())
    }
}

val serviceModule = module {
    scope<MusicService> {

        scoped {
            MusicServiceConnection(get())
        }

        scoped {
            AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()
        }

        scoped {
            SimpleExoPlayer.Builder(get()).build().apply {
                setAudioAttributes(get(), true)
                setHandleAudioBecomingNoisy(true)


            }
        }

        scoped {
            DefaultDataSourceFactory(get(), Util.getUserAgent(get(), "JMusic"))
        }

    }

    scope<JMusicService> {

        scoped {
            MusicServiceConnection(get())
        }

        scoped {
            AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()
        }

        scoped {
            SimpleExoPlayer.Builder(get()).build().apply {
                setAudioAttributes(get(), true)
                setHandleAudioBecomingNoisy(true)
            }
        }

        scoped {
            DefaultDataSourceFactory(get(), Util.getUserAgent(get(), "JMusic"))
        }

    }

}