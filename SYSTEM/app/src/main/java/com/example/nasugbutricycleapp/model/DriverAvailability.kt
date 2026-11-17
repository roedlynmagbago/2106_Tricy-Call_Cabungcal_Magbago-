package com.example.nasugbutricycleapp.model

data class DriverAvailability(
    val driverId: String = "",
    val isOnline: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
