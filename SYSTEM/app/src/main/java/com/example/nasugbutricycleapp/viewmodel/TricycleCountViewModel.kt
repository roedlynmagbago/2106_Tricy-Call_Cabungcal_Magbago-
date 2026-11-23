package com.example.nasugbutricycleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nasugbutricycleapp.model.TricycleCount
import com.example.nasugbutricycleapp.service.TricycleCountService

class TricycleCountViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _counts = MutableLiveData<List<TricycleCount>>(emptyList())
    val counts: LiveData<List<TricycleCount>> = _counts

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isSuccessful = MutableLiveData<Boolean?>()
    val isSuccessful: LiveData<Boolean?> = _isSuccessful

    // SAVE OR UPDATE TRICYCLE COUNT
    fun saveCount(count: TricycleCount) {
        _isLoading.value = true

        TricycleCountService.saveCount(count) { success, resultMessage ->
            _isLoading.postValue(false)
            _isSuccessful.postValue(success)
            _message.postValue(resultMessage)

            if (success) {
                getAllCounts()
            }
        }
    }

    // GET ALL TRICYCLE COUNTS
    fun getAllCounts() {
        _isLoading.value = true

        TricycleCountService.getAllCounts { result, error ->
            _isLoading.postValue(false)
            _counts.postValue(result)

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
