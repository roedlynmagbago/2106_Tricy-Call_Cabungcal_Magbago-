package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.model.NoParkingZone
import com.example.nasugbutricycleapp.repository.NoParkingZoneRepository

object NoParkingZoneService {

    // ADD NO-PARKING ZONE
    fun addZone(
        zone: NoParkingZone,
        onResult: (Boolean, String) -> Unit
    ) {
        if (zone.name.isBlank()) {
            onResult(false, "Zone name is required.")
            return
        }

        if (zone.radius <= 0.0) {
            onResult(false, "Invalid zone radius.")
            return
        }

        NoParkingZoneRepository.addZone(
            zone,
            onResult
        )
    }

    // GET ACTIVE NO-PARKING ZONES
    fun getActiveZones(
        onResult: (List<NoParkingZone>, String?) -> Unit
    ) {
        NoParkingZoneRepository.getActiveZones(onResult)
    }
}
