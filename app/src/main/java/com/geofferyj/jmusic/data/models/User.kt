package com.geofferyj.jmusic.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val plan: Plan = Plan()
) {
    data class Plan(
        @field:JvmField
        val is_subscribed: Boolean = false,
        val expiresIn: Int = 0,
        val expiresAt: Date = Date(),
        @ServerTimestamp
        val dateCreated: Date = Date()
    )
}
