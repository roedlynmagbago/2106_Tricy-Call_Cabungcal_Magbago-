package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.TricycleCount

object TricycleCountRepository {

    private const val COLLECTION = "tricycleCounts"

    // SAVE OR UPDATE TODA TRICYCLE COUNT
    fun saveCount(
        count: TricycleCount,
        onResult: (Boolean, String) -> Unit
    ) {
        val todaId = count.todaId.trim()
        val todaName = count.todaName.trim()

        if (todaId.isBlank() || todaName.isBlank()) {
            onResult(false, "TODA information is required.")
            return
        }

        if (
            count.totalTricycles < 0 ||
            count.availableTricycles < 0 ||
            count.availableTricycles > count.totalTricycles
        ) {
            onResult(false, "Invalid tricycle count.")
            return
        }

        val updatedCount = count.copy(
            todaId = todaId,
            todaName = todaName,
            updatedAt = System.currentTimeMillis()
        )

        FirebaseManager.db
            .collection(COLLECTION)
            .document(todaId)
            .set(updatedCount)
            .addOnSuccessListener {
                onResult(true, "Tricycle count saved successfully.")
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage ?: "Failed to save tricycle count."
                )
            }
    }

    // GET ALL TODA TRICYCLE COUNTS
    fun getAllCounts(
        onResult: (List<TricycleCount>, String?) -> Unit
    ) {
        FirebaseManager.db
            .collection(COLLECTION)
            .get()
            .addOnSuccessListener { documents ->

                val counts = documents.documents.mapNotNull { document ->
                    document.toObject(TricycleCount::class.java)
                }

                onResult(counts, null)
            }
            .addOnFailureListener { error ->
                onResult(
                    emptyList(),
                    error.localizedMessage
                        ?: "Failed to get tricycle counts."
                )
            }
    }
}
