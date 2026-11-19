package com.example.nasugbutricycleapp.repository

import android.util.Log
import com.example.nasugbutricycleapp.firebase.FirebaseManager

object FirestoreUserRepository {

    private const val TAG = "FirestoreUserRepository"
    private const val USERS_COLLECTION = "users"

    fun saveUser(
        userId: String,
        name: String,
        email: String,
        applyAsDriver: Boolean,
        username: String = "",
        phoneNumber: String = "",
        toda: String = "",
        address: String = "",
        onResult: (Boolean, String?) -> Unit
    ) {
        val cleanName = name.trim()
        val cleanUsername = username.trim()
        val cleanEmail = email.trim()
        val cleanPhone = phoneNumber.trim()

        // Validate required information
        if (
            userId.isBlank() ||
            cleanName.isBlank() ||
            cleanUsername.isBlank() ||
            cleanEmail.isBlank()
        ) {
            onResult(false, "Please fill in all required fields.")
            return
        }

        // Make sure the account matches the logged-in Firebase user
        val currentUser = FirebaseManager.auth.currentUser

        if (currentUser == null || currentUser.uid != userId) {
            onResult(false, "User session mismatch. Please try again.")
            return
        }

        val user = hashMapOf(
            "userId" to userId,
            "name" to cleanName,
            "username" to cleanUsername,
            "email" to cleanEmail,
            "phoneNumber" to cleanPhone,
            "role" to if (applyAsDriver) "DRIVER" else "PASSENGER",
            "status" to "ACTIVE",
            "driverApplicationStatus" to if (applyAsDriver) "APPROVED" else "NONE",
            "toda" to toda.trim(),
            "address" to address.trim()
        )

        val userRef = FirebaseManager.db
            .collection(USERS_COLLECTION)
            .document(userId)

        userRef.set(user)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to save user profile to Firestore.", error)
                onResult(
                    false,
                    error.localizedMessage ?: "Failed to save user profile."
                )
            }
    }
}
