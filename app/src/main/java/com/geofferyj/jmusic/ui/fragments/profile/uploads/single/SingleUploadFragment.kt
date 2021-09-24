package com.geofferyj.jmusic.ui.fragments.profile.uploads.single

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.load
import com.geofferyj.jmusic.data.models.Song
import com.geofferyj.jmusic.databinding.FragmentSingleUploadBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class SingleUploadFragment : Fragment() {

    private var albumArtUri: Uri? = null
    private var musicFileUri: Uri? = null
    private val song = Song()

    private lateinit var binding: FragmentSingleUploadBinding


    private val getAlbumArt =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            binding.albumArt.load(uri)
            albumArtUri = uri
            /*
            val imagesDir = File("${requireContext().getExternalFilesDir("")}")
            var filename = ""
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

            /*
            if (uri != null) {
                albumArtUri = uri
                requireContext().contentResolver.query(uri, projection, null, null, null)
                    .use { metaCursor ->
                        if (metaCursor != null) {
                            if (metaCursor.moveToFirst()) {
                                filename = metaCursor.getString(0)
                            }
                        }
                    }
                /*
                requireContext().contentResolver.openInputStream(uri).use { inputStream ->
                    val file = File("$imagesDir/$filename")

                    if (!file.exists()) file.createNewFile()
                    file.outputStream().use {
                        inputStream?.copyTo(it)

                        it.close()
                    }
                    inputStream?.close()

                }
                 */
            }

             */

            */
        }


    private val getMusicFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            musicFileUri = uri
            var filename: String
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

            if (uri != null) {
                musicFileUri = uri
                requireContext().contentResolver.query(uri, projection, null, null, null)
                    .use { metaCursor ->
                        if (metaCursor != null) {
                            if (metaCursor.moveToFirst()) {
                                filename = metaCursor.getString(0)
                                binding.file.editText?.setText(filename)
                            }
                        }
                    }
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSingleUploadBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val explicitItems = listOf("Explicit", "Clean")
        val explicitAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            explicitItems
        )
        (binding.explicit.editText as? AutoCompleteTextView)?.setAdapter(explicitAdapter)


        val genreItems = listOf(
            "Hip-Hop/Rap",
            "R&B",
            "Electronic",
            "Reggae",
            "Pop",
            "Afrobeats",
            "DJ Mix",
            "Rock",
            "Latin",
            "Soca",
            "Kompa",
            "Jazz/Blues",
            "Country",
            "Classical",
            "Gospel",
            "Acapella",
            "Folk",
            "Punjabi",
            "Bollywood",
            "Desi",
            "Instrumental"
        )
        val genreAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genreItems)
        (binding.genre.editText as? AutoCompleteTextView)?.setAdapter(genreAdapter)


        binding.albumArt.setOnClickListener {
            getAlbumArt.launch("image/*")
        }


        binding.file.editText?.setOnClickListener {
            getMusicFile.launch("audio/*")
        }

        binding.uploadBtn.setOnClickListener {

            val _album: String = binding.album.editText?.text.toString()
            val _artist: String = binding.artist.editText?.text.toString()
            val _explicit: String = binding.explicit.editText?.text.toString()
            val _featuring: String = binding.featuring.editText?.text.toString()
            val _genre: String = binding.genre.editText?.text.toString()
            val _producer: String = binding.producer.editText?.text.toString()
            val _title: String = binding.songTitle.editText?.text.toString()
            val _file: String = binding.file.editText?.text.toString()


            song.apply {
                album = _album
                artist = _artist
                explicit = _explicit
                featuring = _featuring
                genre = _genre
                producer = _producer
                title = _title
            }

            uploadAlbumArt(albumArtUri, _file)
            uploadSong(musicFileUri, _file)

        }

    }

    private fun uploadSong(musicFileUri: Uri?, filename: String) {
        musicFileUri?.let {
            val ref = Firebase.storage.reference.child("music/${filename}")

            ref.putFile(it)
                .addOnSuccessListener { task ->
                    val progress = task.bytesTransferred / task.totalByteCount * 100
                    Log.d(TAG, "uploadSong - progress: $progress")
                    ref.downloadUrl.addOnSuccessListener { dUri ->
                        Log.d(TAG, "uploadSong - url: $dUri")
                        song.fileUrl = dUri.toString()

                        saveSongToDb(song)
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "uploadAlbumArt: ${it.message}")
                }

        }
    }

    private fun saveSongToDb(song: Song) {
        Firebase.firestore.collection("songs")
            .add(song)
            .addOnSuccessListener {
                it.update("id", it.id).addOnSuccessListener {
                    findNavController().navigateUp()
                }
            }
    }

    private fun uploadAlbumArt(albumArtUri: Uri?, filename: String) {

        albumArtUri?.let {
            val ref = Firebase.storage.reference.child(
                "images/${
                    filename.split(".").dropLast(1).joinToString(separator = "")
                }_art"
            )

            ref.putFile(it)
                .addOnSuccessListener { task ->
                    val progress = task.bytesTransferred / task.totalByteCount * 100
                    Log.d(TAG, "uploadAlbumArt - progress: $progress")
                    ref.downloadUrl.addOnSuccessListener { dUri ->
                        Log.d(TAG, "uploadAlbumArt - url: $dUri")
                        song.albumArt = dUri.toString()
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "uploadAlbumArt: ${it.message}")
                }
                .addOnCompleteListener {

                }
        }
    }

    companion object {
        private const val TAG = "SingleUploadFragment"
    }

}

