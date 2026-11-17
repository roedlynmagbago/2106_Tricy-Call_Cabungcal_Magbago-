package com.example.nasugbutricycleapp.repository

import com.example.nasugbutricycleapp.firebase.FirebaseManager
import com.example.nasugbutricycleapp.model.DriverReport

object DriverReportRepository {

    private const val COLLECTION = "driverReports"

    // SUBMIT DRIVER REPORT
    fun submitReport(
        report: DriverReport,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentUser = FirebaseManager.auth.currentUser

        if (currentUser == null) {
            onResult(false, "Please log in first.")
            return
        }

        val bookingId = report.bookingId.trim()
        val driverId = report.driverId.trim()
        val category = report.category.trim()
        val description = report.description.trim()

        if (bookingId.isBlank() || driverId.isBlank()) {
            onResult(false, "Invalid booking or driver ID.")
            return
        }

        if (category.isBlank() || category.length > 50) {
            onResult(false, "Invalid report category.")
            return
        }

        if (description.isBlank() || description.length > 1000) {
            onResult(false, "Description must be between 1 and 1000 characters.")
            return
        }

        val reportId = FirebaseManager.db
            .collection(COLLECTION)
            .document()
            .id

        val newReport = report.copy(
            reportId = reportId,
            bookingId = bookingId,
            passengerId = currentUser.uid,
            driverId = driverId,
            category = category,
            description = description,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )

        FirebaseManager.db
            .collection(COLLECTION)
            .document(reportId)
            .set(newReport)
            .addOnSuccessListener {
                onResult(true, "Report submitted successfully.")
            }
            .addOnFailureListener { error ->
                onResult(
                    false,
                    error.localizedMessage ?: "Failed to submit report."
                )
            }
    }

    // GET ALL DRIVER REPORTS (ADMIN)
    fun getAllReports(
        onResult: (List<DriverReport>, String?) -> Unit
    ) {
        FirebaseManager.db
            .collection(COLLECTION)
            .orderBy("createdAt")
            .get()
            .addOnSuccessListener { documents ->

                val reports = documents.documents.mapNotNull { document ->
                    document.toObject(DriverReport::class.java)
                }

                onResult(
                    reports.sortedByDescending { it.createdAt },
                    null
                )
            }
            .addOnFailureListener { error ->
                onResult(
                    emptyList(),
                    error.localizedMessage ?: "Failed to get reports."
                )
            }
    }
}
