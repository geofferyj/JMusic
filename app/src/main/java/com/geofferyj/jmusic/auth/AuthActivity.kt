package com.geofferyj.jmusic.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.geofferyj.jmusic.R
import com.geofferyj.jmusic.databinding.AuthActivityBinding

class AuthActivity : AppCompatActivity() {

    private val binding: AuthActivityBinding by lazy {
        AuthActivityBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        val toolbar = binding.toolbar

        toolbar.setupWithNavController(navController)

        binding.cancelAction.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    companion object {
        const val ACTION_LAUNCH = "com.geofferyj.jmusic.auth.AuthActivity"
        const val RESULT_STATUS = "com.geofferyj.jmusic.auth.AuthActivity.RESULT_STATUS"
    }
}