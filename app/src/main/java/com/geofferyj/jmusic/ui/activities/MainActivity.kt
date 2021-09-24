package com.geofferyj.jmusic.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.geofferyj.jmusic.R
import com.geofferyj.jmusic.data.models.Song
import com.geofferyj.jmusic.databinding.ActivityMainBinding
import com.geofferyj.jmusic.payment.PaymentActivity
import com.geofferyj.jmusic.player.extensions.isNone
import com.geofferyj.jmusic.player.extensions.isPlaying
import com.geofferyj.jmusic.player.extensions.toSong
import com.geofferyj.jmusic.ui.viewmodels.MainViewModel
import com.geofferyj.jmusic.utils.contracts.AuthContract
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: MainViewModel by viewModel()

    private var currentSong: Song? = null
    private var shouldUpdateSeekBar = true
    private lateinit var navController: NavController
    private val authLauncher = registerForActivityResult(AuthContract()) {
        if (!it) {
            navController.navigateUp()
        } else {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.lifecycleOwner = this

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding.viewModel = viewModel
        bottomSheetBehavior = BottomSheetBehavior.from(binding.playerMain)

        viewModel.curPlayingSong.observe(this) {
            if (it == null) return@observe
            viewModel.getCurrentSongData(it.toSong()?.id)

        }

        viewModel.curPlayerPosition.observe(this) {
            if (shouldUpdateSeekBar) {
                binding.seekBar.progress = it.toInt()
                binding.miniPlayerMain.playbackProgress.progress = it.toInt()
            }
        }

        viewModel.curSongDuration.observe(this) {
            binding.seekBar.max = it.toInt()
            binding.miniPlayerMain.playbackProgress.max = it.toInt()
            val sf = SimpleDateFormat("mm:ss", Locale.getDefault())
            val oldDuration = binding.duration.text.toString()
            val newDuration = sf.format(it)

            if (oldDuration != newDuration) {
                binding.duration.text = sf.format(it)
            }
        }

        viewModel.currentSong.observe(this) {
            setFavorite()
            currentSong = it
        }
        viewModel.playbackState.observe(this) {
            if (it == null) return@observe
            binding.playerMain.isGone = it.isNone
            binding.btnPlayPause.isActivated = it.isPlaying || it.isNone
            binding.miniPlayerMain.btnPlayPause.isActivated = it.isPlaying || it.isNone
        }

        binding.btnPlayPause.setOnClickListener {
            when (it.isActivated) {
                true -> viewModel.play()
                false -> viewModel.pause()
            }
        }

        binding.miniPlayerMain.btnPlayPause.setOnClickListener {
            when (it.isActivated) {
                true -> viewModel.play()
                false -> viewModel.pause()
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekBar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

                seekBar?.let {
                    viewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekBar = true
                }
            }
        })

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                slideDown(binding.navView, slideOffset * 15)
                slideDown(binding.miniPlayerMain.root, slideOffset * 28)
            }
        })

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment

        val navView: BottomNavigationView = binding.navView

        navController = navHostFragment.navController

        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {
                R.id.navigation_profile -> {
                    if (Firebase.auth.currentUser == null) {
                        authLauncher.launch("start")
                    }
                }
                R.id.singleUploadFragment,
                R.id.albumUploadFragment -> {
                    slideDown(binding.navView, 1F, 500)
                    slideDown(binding.playerMain, 1F, 500)
                }
                else -> {
                    slideDown(binding.navView, 0F, 300)
                    slideDown(binding.playerMain, 0F, 500)
                }
            }
        }

        binding.btnFavourite.setOnClickListener {

            /** 1.  check if user is logged */

            val userId = Firebase.auth.currentUser?.uid
            if (userId == null) {

                /** 2.  TODO: navigate to login or register flow */
                Toast.makeText(this, "Login Required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /** 3.  if no song is playing or an error occurred, do nothing */
            if (currentSong == null) return@setOnClickListener


            lifecycleScope.launch(Dispatchers.IO) {



                /** 4.  check if user has favorited this song */

                /** i.  filter the documents by document id using
                 *      whereEqualTo(FieldPath.documentId(), userId)
                 *
                 *  ii. check for songId in ids array
                 *      using .whereArrayContains("ids", songId)
                 */

               val fav =  Firebase.firestore.collection("favorites")
                    .whereEqualTo(FieldPath.documentId(), userId)
                    .whereArrayContains("ids", currentSong!!.id)
                    .get().await().documents.isEmpty()

                /**
                 *  if fav is true, song not favorited,
                 *  so favorite song
                 * */

                if (fav){
                    Firebase.firestore.runBatch {
                        it.set(
                            Firebase.firestore.collection("favorites").document(userId), mapOf(
                                "ids" to
                                        FieldValue.arrayUnion(currentSong!!.id)
                            ), SetOptions.merge()
                        )

                        it.update(
                            Firebase.firestore.collection("songs")
                                .document(currentSong!!.id),
                            "stats.favorites",
                            FieldValue.increment(1)
                        )
                    }
                }

                /**
                 *  if fav is false, song alread favorited,
                 *  so unfavorite song
                 * */

                else {
                    Firebase.firestore.runBatch {
                        it.set(
                            Firebase.firestore.collection("favorites").document(userId), mapOf(
                                "ids" to
                                        FieldValue.arrayRemove(currentSong!!.id)
                            ), SetOptions.merge()
                        )

                        it.update(
                            Firebase.firestore.collection("songs")
                                .document(currentSong!!.id),
                            "stats.favorites",
                            FieldValue.increment(-1)
                        )
                    }
                }

            }
        }


        /**
         * TODO: Statistics and Comments Viewpager and tabs setup
         */


    }

    fun setFavorite(){
        /** 1.  check if user is logged */

        val userId = Firebase.auth.currentUser?.uid
        if (userId == null) {

            /** 2.  TODO: navigate to login or register flow */
            Toast.makeText(this, "Login Required", Toast.LENGTH_SHORT).show()
            return
        }

        /** 3.  if no song is playing or an error occurred, do nothing */
        if (currentSong == null) return

        Firebase.firestore.collection("favorites")
            .document(userId)
            .addSnapshotListener { value, error ->

                if (error != null ) return@addSnapshotListener

                val userFavorites: List<*>? = value?.get("ids") as List<*>?

                if (userFavorites != null) {
                    binding.btnFavourite.isActivated = currentSong!!.id in userFavorites
                }

                Timber.d("Favorites: $userFavorites")
            }
    }

    override fun onBackPressed() {
        val m = binding.multiSheetView

        if (m.consumeBackPress()) {
            return
        }

        super.onBackPressed()
    }

    private fun slideDown(child: View, slideOffset: Float, duration: Long = 0) {
        val heightToAnimate = slideOffset * child.height
        val animator = child.animate()
        animator
            .translationY(heightToAnimate)
            .setInterpolator(DecelerateInterpolator())
            .setDuration(duration)
            .start()
    }

}