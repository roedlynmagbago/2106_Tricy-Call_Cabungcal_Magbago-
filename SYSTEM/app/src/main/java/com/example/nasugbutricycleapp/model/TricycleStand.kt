package com.example.nasugbutricycleapp.model

data class TricycleStand(
    val standId: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type: String = "SAKAYAN",
    val isActive: Boolean = true
)
