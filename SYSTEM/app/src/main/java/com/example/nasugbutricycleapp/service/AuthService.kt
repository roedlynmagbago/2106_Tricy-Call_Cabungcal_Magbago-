package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.User
import com.example.nasugbutricycleapp.repository.FirebaseAuthRepository
import com.example.nasugbutricycleapp.repository.UserRepository

object AuthService {

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
        FirebaseAuthRepository.register(
            name = name,
            username = username,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            applyAsDriver = applyAsDriver,
            onResult = onResult
        )
    }

    // LOGIN
    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        FirebaseAuthRepository.login(
            email = email,
            password = password,
            onResult = onResult
        )
    }

    // RESET PASSWORD
    fun resetPassword(
        email: String,
        onResult: (Boolean, String) -> Unit
    ) {
        FirebaseAuthRepository.resetPassword(
            email = email,
            onResult = onResult
        )
    }

    // LOGOUT
    fun logout() {
        FirebaseAuthRepository.logout()
    }

    // GET CURRENT USER
    fun getCurrentUser(
        onResult: (User?, String?) -> Unit
    ) {
        UserRepository.getCurrentUser(onResult)
    }

    // CHECK LOGIN STATUS
    fun isLoggedIn(): Boolean {
        return FirebaseManager.auth.currentUser != null
    }
}
