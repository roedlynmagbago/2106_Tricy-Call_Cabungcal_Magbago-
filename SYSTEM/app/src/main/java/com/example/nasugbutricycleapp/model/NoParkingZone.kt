package com.example.nasugbutricycleapp.model

data class NoParkingZone(
    val zoneId: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Double = 0.0,
    val description: String = "",
    val isActive: Boolean = true
)
