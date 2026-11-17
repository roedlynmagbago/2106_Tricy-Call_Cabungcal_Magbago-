package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager

object FirestoreUserRepository {

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
        onResult: (Boolean) -> Unit
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
            onResult(false)
            return
        }

        // Make sure the account matches the logged-in Firebase user
        val currentUser = FirebaseManager.auth.currentUser

        if (
            currentUser == null ||
            currentUser.uid != userId ||
            !currentUser.email.equals(cleanEmail, ignoreCase = true)
        ) {
            onResult(false)
            return
        }

        val user = hashMapOf(
            "userId" to userId,
            "name" to cleanName,
            "username" to cleanUsername,
            "email" to cleanEmail,
            "phoneNumber" to cleanPhone,
            "role" to "PASSENGER",
            "status" to "ACTIVE",
            "driverApplicationStatus" to
                    if (applyAsDriver) "PENDING" else "NONE",
            "toda" to toda.trim(),
            "address" to address.trim()
        )

        val userRef = FirebaseManager.db
            .collection(USERS_COLLECTION)
            .document(userId)

        // Prevent overwriting an existing user profile
        FirebaseManager.db.runTransaction<Void?> { transaction ->

            val existingUser = transaction.get(userRef)

            if (existingUser.exists()) {
                throw IllegalStateException("User profile already exists.")
            }

            transaction.set(userRef, user)

            null
        }
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}
