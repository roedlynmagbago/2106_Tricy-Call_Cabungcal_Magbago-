package com.example.nasugbutricycleapp.model

data class DriverApplication(
    val userId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val toda: String = "",
    val address: String = "",
    val email: String = "",
    val status: String = "PENDING"
)