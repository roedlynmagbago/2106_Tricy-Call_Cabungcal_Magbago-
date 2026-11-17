package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.model.ParadaSchedule
import com.example.nasugbutricycleapp.repository.ParadaScheduleRepository

object ParadaScheduleService {

    // ADD PARADA SCHEDULE
    fun addSchedule(
        schedule: ParadaSchedule,
        onResult: (Boolean, String) -> Unit
    ) {
        if (schedule.todaName.isBlank()) {
            onResult(false, "TODA name is required.")
            return
        }

        if (schedule.standId.isBlank()) {
            onResult(false, "Stand ID is required.")
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

        ParadaScheduleRepository.addSchedule(
            schedule,
            onResult
        )
    }

    // GET ACTIVE PARADA SCHEDULES
    fun getActiveSchedules(
        onResult: (List<ParadaSchedule>, String?) -> Unit
    ) {
        ParadaScheduleRepository.getActiveSchedules(onResult)
    }
}
