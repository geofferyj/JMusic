package com.geofferyj.jmusic.auth

import androidx.lifecycle.ViewModel
import com.geofferyj.jmusic.data.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {


    suspend fun userExists(email: String) = !Firebase.firestore
        .collection("users")
        .whereEqualTo("email", email)
        .get()
        .await().isEmpty


    suspend fun signIn(email: String, password: String): String {
        val userId = Firebase.auth
            .signInWithEmailAndPassword(email, password)
            .await()
            .user?.uid

        return if (userId.isNullOrBlank()) {
            "ERROR"
        } else {
            "SUCCESS"
        }
    }


    suspend fun signUp(name: String, email: String, password: String): User {
        val userId = Firebase.auth
            .createUserWithEmailAndPassword(email, password)
            .await()
            .user?.uid

        return if (userId.isNullOrBlank()) {
            User()
        } else {
            val user = User(userId, name, email)
            Firebase.firestore.collection("users").document(userId).set(User(userId, name, email))

            user
        }
    }

}