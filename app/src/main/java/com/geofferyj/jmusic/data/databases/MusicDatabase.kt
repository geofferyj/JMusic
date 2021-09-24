package com.geofferyj.jmusic.data.databases

import android.util.Log
import com.geofferyj.jmusic.data.models.Song
import com.geofferyj.jmusic.utils.Constants.SONG_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class MusicDatabase {

    private val fireStore = Firebase.firestore
    private val songCollection = fireStore.collection(SONG_COLLECTION)

    suspend fun getAllSongs(): List<Song> {
        return try {
            val songs = mutableListOf<Song>()
            val t = songCollection.get().await()
            for (d in t){

                songs.add(d.toObject<Song>())
            }
            songs
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSongs(term: String): List<Song> {
        return try {
            songCollection.get().await().toObjects(Song::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}