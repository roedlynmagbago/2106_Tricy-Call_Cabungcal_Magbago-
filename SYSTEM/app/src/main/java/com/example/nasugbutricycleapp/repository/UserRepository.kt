package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.User

object UserRepository {

    fun getCurrentUser(
        onResult: (User?, String?) -> Unit
    ) {
        val firebaseUser = FirebaseManager.auth.currentUser

        if (firebaseUser == null) {
            onResult(null, "No user is currently logged in.")
            return
        }
        getUserById(firebaseUser.uid, onResult)
    }

    fun getUserById(
        userId: String,
        onResult: (User?, String?) -> Unit
    ) {
        FirebaseManager.db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    onResult(user, null)
                } else {
                    onResult(null, "User not found.")
                }
            }
            .addOnFailureListener { error ->
                onResult(
                    null,
                    error.localizedMessage ?: "Unable to load user."
                )
            }
    }

    fun updateProfile(
        name: String,
        username: String,
        phoneNumber: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = FirebaseManager.auth.currentUser?.uid
        if (uid == null) {
            onResult(false, "Please log in first.")
            return
        }
        if (name.isBlank() || username.isBlank()) {
            onResult(false, "Name and username are required.")
            return
        }

        val updates = mapOf(
            "name" to name.trim(),
            "username" to username.trim(),
            "phoneNumber" to phoneNumber.trim()
        )
        FirebaseManager.db.collection("users")
            .document(uid)
            .update(updates)
            .addOnSuccessListener {
                onResult(true, "Profile updated successfully.")
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage ?: "Unable to update profile."
                )
            }
    }
}