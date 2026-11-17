package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.TricycleStand

object TricycleStandRepository {

    private const val COLLECTION = "tricycleStands"

    // ADD A TRICYCLE STAND
    fun addStand(
        stand: TricycleStand,
        onResult: (Boolean, String) -> Unit
    ) {
        val name = stand.name.trim()

        if (name.isBlank()) {
            onResult(false, "Stand name is required.")
            return
        }

        if (stand.type !in listOf("SAKAYAN", "BABAAAN")) {
            onResult(false, "Invalid stand type.")
            return
        }

        if (
            stand.latitude !in -90.0..90.0 ||
            stand.longitude !in -180.0..180.0 ||
            (stand.latitude == 0.0 && stand.longitude == 0.0)
        ) {
            onResult(false, "Valid coordinates are required.")
            return
        }

        val standId = FirebaseManager.db
            .collection(COLLECTION)
            .document()
            .id

        val newStand = stand.copy(
            standId = standId,
            name = name
        )

        FirebaseManager.db
            .collection(COLLECTION)
            .document(standId)
            .set(newStand)
            .addOnSuccessListener {
                onResult(true, "Tricycle stand added successfully.")
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage ?: "Failed to add tricycle stand."
                )
            }
    }

    // GET ACTIVE TRICYCLE STANDS
    fun getActiveStands(
        onResult: (List<TricycleStand>, String?) -> Unit
    ) {
        FirebaseManager.db
            .collection(COLLECTION)
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { documents ->

                val stands = documents.documents.mapNotNull { document ->
                    document.toObject(TricycleStand::class.java)
                }

                onResult(stands, null)
            }
            .addOnFailureListener { error ->
                onResult(
                    emptyList(),
                    error.localizedMessage ?: "Failed to get tricycle stands."
                )
            }
    }
}
