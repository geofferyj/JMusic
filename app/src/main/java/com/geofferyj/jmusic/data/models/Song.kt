package com.geofferyj.jmusic.data.models


import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Song(
    var album: String = "",
    var albumArt: String = "",
    var artist: String = "",
    var description: String = "",
    var duration: String = "",
    var explicit: String = "",
    var featuring: String = "",
    var fileUrl: String = "",
    var genre: String = "",
    var id: String = "",
    @field:JvmField
    var is_featured: String = "",
    @field:JvmField
    var is_trending: String = "",
    var producer: String = "",

    @ServerTimestamp
    var release_date: Date = Date(),
    var stats: Stats = Stats(),
    var title: String = "",
) : Parcelable {
    @Parcelize
    data class Stats(
        var comments: Int = 0,
        var downloads: Int = 0,
        var favorites: Int = 0,
        var playlists: Int = 0,
        var plays: Int = 0
    ) : Parcelable
}
