package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.model.User
import com.example.nasugbutricycleapp.repository.UserRepository

object UserService {

    fun getCurrentUser(
        onResult: (User?, String?) -> Unit
    ) {
        UserRepository.getCurrentUser(onResult)
    }

    fun getUserById(
        userId: String,
        onResult: (User?, String?) -> Unit
    ) {
        if (userId.isBlank()) {
            onResult(null, "Invalid user ID.")
            return
        }

        UserRepository.getUserById(userId, onResult)
    }

    fun updateProfile(
        name: String,
        username: String,
        phoneNumber: String,
        onResult: (Boolean, String) -> Unit
    ) {
        UserRepository.updateProfile(
            name,
            username,
            phoneNumber,
            onResult
        )
    }
}
