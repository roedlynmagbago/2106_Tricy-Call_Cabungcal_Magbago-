package com.example.nasugbutricycleapp.repository

import android.util.Log
import android.util.Patterns
import com.example.nasugbutricycleapp.firebase.FirebaseManager

object FirebaseAuthRepository {

    private const val TAG = "FirebaseAuthRepository"
    private const val USERS_COLLECTION = "users"

    // REGISTER
    fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        applyAsDriver: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        val cleanName = name.trim()
        val cleanUsername = username.trim()
        val cleanEmail = email.trim()

        if (
            cleanName.isBlank() ||
            cleanUsername.isBlank() ||
            cleanEmail.isBlank() ||
            password.isBlank() ||
            confirmPassword.isBlank()
        ) {
            onResult(false, "Please fill in all required fields.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            onResult(false, "Please enter a valid email address.")
            return
        }

        if (password != confirmPassword) {
            onResult(false, "Passwords do not match.")
            return
        }

        if (password.length < 6) {
            onResult(false, "Password must be at least 6 characters.")
            return
        }

        FirebaseManager.auth
            .createUserWithEmailAndPassword(cleanEmail, password)
            .addOnSuccessListener { result ->

                val createdUser = result.user

                if (createdUser == null) {
                    FirebaseManager.auth.signOut()
                    onResult(false, "Unable to get user ID.")
                    return@addOnSuccessListener
                }

                val uid = createdUser.uid

                FirestoreUserRepository.saveUser(
                    userId = uid,
                    name = cleanName,
                    username = cleanUsername,
                    email = cleanEmail,
                    applyAsDriver = applyAsDriver
                ) { success ->

                    if (success) {
                        onResult(true, "Registration successful.")
                    } else {

                        // Roll back Firebase Authentication account
                        createdUser.delete()
                            .addOnCompleteListener { task ->

                                FirebaseManager.auth.signOut()

                                if (task.isSuccessful) {
                                    onResult(
                                        false,
                                        "Registration failed. Please try again."
                                    )
                                } else {
                                    Log.e(
                                        TAG,
                                        "Unable to remove Auth account after profile creation failed.",
                                        task.exception
                                    )

                                    onResult(
                                        false,
                                        "Profile was not saved and account cleanup failed. Please contact support."
                                    )
                                }
                            }
                    }
                }
            }
            .addOnFailureListener { error ->

                Log.e(TAG, "Registration failed.", error)

                onResult(
                    false,
                    error.localizedMessage ?: "Registration failed."
                )
            }
    }

    // LOGIN
    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val cleanEmail = email.trim()

        if (cleanEmail.isBlank() || password.isBlank()) {
            onResult(false, "Please enter your email and password.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            onResult(false, "Please enter a valid email address.")
            return
        }

        FirebaseManager.auth
            .signInWithEmailAndPassword(cleanEmail, password)
            .addOnSuccessListener { result ->

                val uid = result.user?.uid

                if (uid == null) {
                    FirebaseManager.auth.signOut()
                    onResult(false, "Unable to get user ID.")
                    return@addOnSuccessListener
                }

                FirebaseManager.db.collection(USERS_COLLECTION)
                    .document(uid)
                    .get()
                    .addOnSuccessListener { document ->

                        if (!document.exists()) {
                            FirebaseManager.auth.signOut()
                            onResult(false, "User profile not found.")
                            return@addOnSuccessListener
                        }

                        val role = document.getString("role")
                        val status = document.getString("status")
                        val driverStatus =
                            document.getString("driverApplicationStatus")

                        if (status != "ACTIVE") {
                            FirebaseManager.auth.signOut()
                            onResult(false, "Account is not active.")
                            return@addOnSuccessListener
                        }

                        when (role) {

                            "SUPER_ADMIN", "ADMIN", "PASSENGER" -> {
                                onResult(true, role)
                            }

                            "DRIVER" -> {
                                if (driverStatus == "APPROVED") {
                                    onResult(true, "DRIVER")
                                } else {
                                    FirebaseManager.auth.signOut()
                                    onResult(
                                        false,
                                        "Driver is not approved."
                                    )
                                }
                            }

                            else -> {
                                FirebaseManager.auth.signOut()
                                onResult(false, "Invalid user role.")
                            }
                        }
                    }
                    .addOnFailureListener { error ->

                        Log.e(TAG, "Unable to load user profile.", error)

                        FirebaseManager.auth.signOut()
                        onResult(false, "Unable to load user profile.")
                    }
            }
            .addOnFailureListener { error ->

                Log.e(TAG, "Login failed.", error)

                onResult(
                    false,
                    error.localizedMessage ?: "Invalid email or password."
                )
            }
    }

    // RESET PASSWORD
    fun resetPassword(
        email: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val cleanEmail = email.trim()

        if (cleanEmail.isBlank()) {
            onResult(false, "Please enter your email address.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            onResult(false, "Please enter a valid email address.")
            return
        }

        FirebaseManager.auth
            .sendPasswordResetEmail(cleanEmail)
            .addOnSuccessListener {
                onResult(true, "Password reset email sent.")
            }
            .addOnFailureListener { error ->

                Log.e(TAG, "Password reset failed.", error)

                onResult(
                    false,
                    error.localizedMessage ?: "Unable to send reset email."
                )
            }
    }

    // LOGOUT
    fun logout() {
        FirebaseManager.auth.signOut()
    }
}
