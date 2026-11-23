package com.example.nasugbutricycleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nasugbutricycleapp.model.DriverRating
import com.example.nasugbutricycleapp.service.DriverRatingService

class DriverRatingViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _ratings = MutableLiveData<List<DriverRating>>(emptyList())
    val ratings: LiveData<List<DriverRating>> = _ratings

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isSuccessful = MutableLiveData<Boolean?>()
    val isSuccessful: LiveData<Boolean?> = _isSuccessful

    // SUBMIT DRIVER RATING
    fun submitRating(rating: DriverRating) {
        _isLoading.value = true

        DriverRatingService.submitRating(rating) { success, resultMessage ->
            _isLoading.postValue(false)
            _isSuccessful.postValue(success)
            _message.postValue(resultMessage)
        }
    }

    // GET RATINGS FOR ONE DRIVER
    fun getDriverRatings(driverId: String) {
        _isLoading.value = true

        DriverRatingService.getDriverRatings(driverId) { result, error ->
            _isLoading.postValue(false)
            _ratings.postValue(result)

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
