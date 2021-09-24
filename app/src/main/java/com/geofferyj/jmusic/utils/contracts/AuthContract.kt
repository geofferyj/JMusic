package com.geofferyj.jmusic.utils.contracts

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.geofferyj.jmusic.auth.AuthActivity

class AuthContract: ActivityResultContract<Any, Boolean>() {

    override fun createIntent(context: Context, input: Any?) = Intent(AuthActivity.ACTION_LAUNCH)

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {

        intent?.let{
        return    it.getBooleanExtra(AuthActivity.RESULT_STATUS, false)
        }
        return false
    }
}