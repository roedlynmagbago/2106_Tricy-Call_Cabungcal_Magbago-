package com.example.nasugbutricycleapp.model

data class TricycleCount(
    val todaId: String = "",
    val todaName: String = "",
    val totalTricycles: Int = 0,
    val availableTricycles: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
