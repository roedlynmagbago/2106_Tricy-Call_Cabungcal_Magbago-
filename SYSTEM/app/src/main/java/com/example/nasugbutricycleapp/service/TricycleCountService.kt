package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.model.TricycleCount
import com.example.nasugbutricycleapp.repository.TricycleCountRepository

object TricycleCountService {

    // SAVE OR UPDATE TRICYCLE COUNT
    fun saveCount(
        count: TricycleCount,
        onResult: (Boolean, String) -> Unit
    ) {
        if (count.todaId.isBlank() || count.todaName.isBlank()) {
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

        TricycleCountRepository.saveCount(
            count,
            onResult
        )
    }

    // GET ALL TRICYCLE COUNTS
    fun getAllCounts(
        onResult: (List<TricycleCount>, String?) -> Unit
    ) {
        TricycleCountRepository.getAllCounts(onResult)
    }
}
