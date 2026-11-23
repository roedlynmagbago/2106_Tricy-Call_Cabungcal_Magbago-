package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.User

object UserRepository {

    private const val USERS_COLLECTION = "users"
    private val phonePattern = Regex("^09[0-9]{9}$")

    // Get currently logged-in user
    fun getCurrentUser(
        onResult: (User?, String?) -> Unit
    ) {
        val firebaseUser = FirebaseManager.auth.currentUser

        if (firebaseUser == null) {
            onResult(null, "No user is currently logged in.")
            return
        }

        getUserById(firebaseUser.uid) { user, error ->
            if (user != null) {
                // If user.name in Firestore is blank, fall back to Auth displayName or email
                if (user.name.isBlank()) {
                    user.name = firebaseUser.displayName
                        ?: firebaseUser.email?.substringBefore("@")
                        ?: "User"
                }
                if (user.email.isBlank()) {
                    user.email = firebaseUser.email ?: ""
                }
                onResult(user, null)
            } else {
                // Fallback User if Firestore document is unreachable or missing
                val fallbackName = firebaseUser.displayName
                    ?: firebaseUser.email?.substringBefore("@")
                    ?: "User"
                val fallbackUser = User(
                    userId = firebaseUser.uid,
                    name = fallbackName,
                    email = firebaseUser.email ?: ""
                )
                onResult(fallbackUser, null)
            }
        }
    }

    // Get user by ID
    fun getUserById(
        userId: String,
        onResult: (User?, String?) -> Unit
    ) {
        if (userId.isBlank()) {
            onResult(null, "Invalid user ID.")
            return
        }

        FirebaseManager.db.collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    onResult(null, "User not found.")
                    return@addOnSuccessListener
                }

                val user = document.toObject(User::class.java)

                if (user == null) {
                    onResult(null, "Unable to read user information.")
                } else {
                    onResult(user, null)
                }
            }
            .addOnFailureListener { error ->
                onResult(
                    null,
                    error.localizedMessage ?: "Unable to load user."
                )
            }
    }

    // Update current user's profile
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

        val cleanName = name.trim()
        val cleanUsername = username.trim()
        val cleanPhone = phoneNumber.trim()

        if (cleanName.isBlank() || cleanUsername.isBlank()) {
            onResult(false, "Name and username are required.")
            return
        }

        if (cleanPhone.isNotBlank() && !phonePattern.matches(cleanPhone)) {
            onResult(false, "Please enter a valid Philippine mobile number.")
            return
        }

        val updates = mapOf(
            "name" to cleanName,
            "username" to cleanUsername,
            "phoneNumber" to cleanPhone
        )

        FirebaseManager.db.collection(USERS_COLLECTION)
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
