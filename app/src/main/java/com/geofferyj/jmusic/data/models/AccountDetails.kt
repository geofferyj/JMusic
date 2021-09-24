package com.geofferyj.jmusic.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountDetails(
    val bankName: String,
    val accountName: String,
    val accountNumber: String
) :
    Parcelable
