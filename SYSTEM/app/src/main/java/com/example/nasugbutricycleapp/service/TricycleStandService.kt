package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.model.TricycleStand
import com.example.nasugbutricycleapp.repository.TricycleStandRepository

object TricycleStandService {

    // ADD A TRICYCLE STAND
    fun addStand(
        stand: TricycleStand,
        onResult: (Boolean, String) -> Unit
    ) {
        if (stand.name.isBlank()) {
            onResult(false, "Stand name is required.")
            return
        }

        if (stand.type !in listOf("SAKAYAN", "BABAAAN")) {
            onResult(false, "Invalid stand type.")
            return
        }

        TricycleStandRepository.addStand(
            stand,
            onResult
        )
    }

    // GET ACTIVE TRICYCLE STANDS
    fun getActiveStands(
        onResult: (List<TricycleStand>, String?) -> Unit
    ) {
        TricycleStandRepository.getActiveStands(onResult)
    }
}
