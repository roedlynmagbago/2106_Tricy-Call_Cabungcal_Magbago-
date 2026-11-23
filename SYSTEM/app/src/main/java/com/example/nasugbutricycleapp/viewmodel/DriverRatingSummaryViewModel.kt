package com.example.nasugbutricycleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nasugbutricycleapp.model.DriverRatingSummary
import com.example.nasugbutricycleapp.service.DriverRatingSummaryService

class DriverRatingSummaryViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _ratingSummary = MutableLiveData<DriverRatingSummary?>()
    val ratingSummary: LiveData<DriverRatingSummary?> = _ratingSummary

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isSuccessful = MutableLiveData<Boolean?>()
    val isSuccessful: LiveData<Boolean?> = _isSuccessful

    // GET DRIVER RATING SUMMARY
    fun getRatingSummary(driverId: String) {
        _isLoading.value = true

        DriverRatingSummaryService.getRatingSummary(driverId) { result, error ->
            _isLoading.postValue(false)
            _ratingSummary.postValue(result)

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
