package com.example.nasugbutricycleapp.service

import com.example.nasugbutricycleapp.model.DriverReport
import com.example.nasugbutricycleapp.repository.DriverReportRepository

object DriverReportService {

    // SUBMIT DRIVER REPORT
    fun submitReport(
        report: DriverReport,
        onResult: (Boolean, String) -> Unit
    ) {
        if (report.bookingId.isBlank()) {
            onResult(false, "Invalid booking ID.")
            return
        }

        if (report.driverId.isBlank()) {
            onResult(false, "Invalid driver ID.")
            return
        }

        if (report.category.isBlank()) {
            onResult(false, "Please select a report category.")
            return
        }

        if (report.description.isBlank()) {
            onResult(false, "Please enter a report description.")
            return
        }

        DriverReportRepository.submitReport(
            report,
            onResult
        )
    }

    // GET ALL DRIVER REPORTS
    fun getAllReports(
        onResult: (List<DriverReport>, String?) -> Unit
    ) {
        DriverReportRepository.getAllReports(onResult)
    }
}