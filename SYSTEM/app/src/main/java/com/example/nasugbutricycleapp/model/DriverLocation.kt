package com.example.nasugbutricycleapp.model

data class DriverLocation(
    val driverId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f,
    val updatedAt: Long = System.currentTimeMillis()
)
