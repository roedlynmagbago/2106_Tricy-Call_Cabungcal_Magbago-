package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.ParadaSchedule

object ParadaScheduleRepository {

    private const val COLLECTION = "paradaSchedules"

    // ADD PARADA SCHEDULE
    fun addSchedule(
        schedule: ParadaSchedule,
        onResult: (Boolean, String) -> Unit
    ) {
        val todaName = schedule.todaName.trim()
        val standId = schedule.standId.trim()

        if (todaName.isBlank() || standId.isBlank()) {
            onResult(false, "TODA name and stand ID are required.")
            return
        }

        if (
            schedule.openingTime.isBlank() ||
            schedule.closingTime.isBlank() ||
            schedule.cutOffTime.isBlank()
        ) {
            onResult(false, "Complete the schedule details.")
            return
        }

        val scheduleId = FirebaseManager.db
            .collection(COLLECTION)
            .document()
            .id

        val newSchedule = schedule.copy(
            scheduleId = scheduleId,
            todaName = todaName,
            standId = standId
        )

        FirebaseManager.db
            .collection(COLLECTION)
            .document(scheduleId)
            .set(newSchedule)
            .addOnSuccessListener {
                onResult(true, "Parada schedule added successfully.")
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage ?: "Failed to add parada schedule."
                )
            }
    }

    // GET ACTIVE PARADA SCHEDULES
    fun getActiveSchedules(
        onResult: (List<ParadaSchedule>, String?) -> Unit
    ) {
        FirebaseManager.db
            .collection(COLLECTION)
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { documents ->

                val schedules = documents.documents.mapNotNull { document ->
                    document.toObject(ParadaSchedule::class.java)
                }

                onResult(schedules, null)
            }
            .addOnFailureListener { error ->
                onResult(
                    emptyList(),
                    error.localizedMessage
                        ?: "Failed to get parada schedules."
                )
            }
    }
}
