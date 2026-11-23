package com.example.nasugbutricycleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nasugbutricycleapp.model.DriverReport
import com.example.nasugbutricycleapp.service.DriverReportService

class DriverReportViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _reports = MutableLiveData<List<DriverReport>>(emptyList())
    val reports: LiveData<List<DriverReport>> = _reports

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isSuccessful = MutableLiveData<Boolean?>()
    val isSuccessful: LiveData<Boolean?> = _isSuccessful

    // SUBMIT DRIVER REPORT
    fun submitReport(report: DriverReport) {
        _isLoading.value = true

        DriverReportService.submitReport(report) { success, resultMessage ->
            _isLoading.postValue(false)
            _isSuccessful.postValue(success)
            _message.postValue(resultMessage)
        }
    }

    // GET ALL DRIVER REPORTS (ADMIN)
    fun getAllReports() {
        _isLoading.value = true

        DriverReportService.getAllReports { result, error ->
            _isLoading.postValue(false)
            _reports.postValue(result)

            if (error != null) {
                _message.postValue(error)
                _isSuccessful.postValue(false)
            } else {
                _isSuccessful.postValue(true)
            }
        }
    }

    // CLEAR MESSAGE
    fun clearMessage() {
        _message.value = ""
        _isSuccessful.value = null
    }
}
