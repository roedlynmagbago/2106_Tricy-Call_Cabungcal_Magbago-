package com.example.nasugbutricycleapp.repository
import com.example.nasugbutricycleapp.firebase.FirebaseManager

object FirebaseAuthRepository {

    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        applyAsDriver: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            onResult(false, "Please fill in all required fields.")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email.trim()).matches()) {
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
            .createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid

                if (uid == null) {
                    onResult(false, "Unable to get user ID.")
                    return@addOnSuccessListener
                }

                FirestoreUserRepository.saveUser(
                    userId = uid,
                    name = name,
                    email = email.trim(),
                    applyAsDriver = applyAsDriver
                ) { success ->
                    if (success) {
                        onResult(true, "Registration successful.")
                    } else {
                        result.user?.delete()
                            ?.addOnCompleteListener { task ->
                                FirebaseManager.auth.signOut()

                                if (task.isSuccessful) {
                                    onResult(
                                        false,
                                        "Registration failed. Please try again."
                                    )
                                } else {
                                    onResult(
                                        false,
                                        "Profile was not saved. Please contact support."
                                    )
                                }
                            }
                    }
                }
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage ?: "Registration failed."
                )
            }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Please enter your email and password.")
            return
        }
        FirebaseManager.auth
            .signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid

                if (uid == null) {
                    FirebaseManager.auth.signOut()
                    onResult(false, "Unable to get user ID.")
                    return@addOnSuccessListener
                }
                FirebaseManager.db.collection("users")
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
                            "SUPER_ADMIN", "ADMIN", "PASSENGER" ->
                                onResult(true, role)

                            "DRIVER" -> {
                                if (driverStatus == "APPROVED") {
                                    onResult(true, "DRIVER")
                                } else {
                                    FirebaseManager.auth.signOut()
                                    onResult(false, "Driver is not approved.")
                                }
                            }
                            else -> {
                                FirebaseManager.auth.signOut()
                                onResult(false, "Invalid user role.")
                            }
                        }
                    }
                    .addOnFailureListener {
                        FirebaseManager.auth.signOut()
                        onResult(false, "Unable to load user profile.")
                    }
            }
            .addOnFailureListener {
                onResult(false, "Invalid email or password.")
            }
    }

    fun resetPassword(
        email: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (email.isBlank()) {
            onResult(false, "Please enter your email address.")
            return
        }
        FirebaseManager.auth
            .sendPasswordResetEmail(email.trim())
            .addOnSuccessListener {
                onResult(true, "Password reset email sent.")
            }
            .addOnFailureListener {
                onResult(false, "Unable to send reset email.")
            }
    }
    fun logout() {
        FirebaseManager.auth.signOut()
    }
}
