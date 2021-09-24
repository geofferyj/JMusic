package com.geofferyj.jmusic.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import java.text.SimpleDateFormat
import java.util.*

fun copyText(text: String, activity: Activity?) {
    val myClipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
    val myClip = ClipData.newPlainText("text", text)
    myClipboard?.setPrimaryClip(myClip)
    Toast.makeText(
        activity?.applicationContext,
        "Address Copied",
        Toast.LENGTH_SHORT
    ).show()
}

val Date.shortString: String
get() {
    val sdf = SimpleDateFormat("d_MMM_yyyy_HH_mm_ss_a", Locale.getDefault())
    return sdf.format(this)
}