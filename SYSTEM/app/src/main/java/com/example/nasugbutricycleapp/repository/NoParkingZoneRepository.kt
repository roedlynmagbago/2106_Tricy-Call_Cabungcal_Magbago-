package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.NoParkingZone

object NoParkingZoneRepository {

    private const val COLLECTION = "noParkingZones"

    // ADD NO-PARKING ZONE
    fun addZone(
        zone: NoParkingZone,
        onResult: (Boolean, String) -> Unit
    ) {
        val name = zone.name.trim()
        val description = zone.description.trim()

        if (name.isBlank()) {
            onResult(false, "Zone name is required.")
            return
        }

        if (
            zone.latitude !in -90.0..90.0 ||
            zone.longitude !in -180.0..180.0 ||
            (zone.latitude == 0.0 && zone.longitude == 0.0)
        ) {
            onResult(false, "Valid coordinates are required.")
            return
        }

        if (zone.radius <= 0.0) {
            onResult(false, "Zone radius must be greater than zero.")
            return
        }

        val zoneId = FirebaseManager.db
            .collection(COLLECTION)
            .document()
            .id

        val newZone = zone.copy(
            zoneId = zoneId,
            name = name,
            description = description
        )

        FirebaseManager.db
            .collection(COLLECTION)
            .document(zoneId)
            .set(newZone)
            .addOnSuccessListener {
                onResult(true, "No-parking zone added successfully.")
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage ?: "Failed to add no-parking zone."
                )
            }
    }

    // GET ACTIVE NO-PARKING ZONES
    fun getActiveZones(
        onResult: (List<NoParkingZone>, String?) -> Unit
    ) {
        FirebaseManager.db
            .collection(COLLECTION)
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { documents ->

                val zones = documents.documents.mapNotNull { document ->
                    document.toObject(NoParkingZone::class.java)
                }

                onResult(zones, null)
            }
            .addOnFailureListener { error ->
                onResult(
                    emptyList(),
                    error.localizedMessage
                        ?: "Failed to get no-parking zones."
                )
            }
    }
}
