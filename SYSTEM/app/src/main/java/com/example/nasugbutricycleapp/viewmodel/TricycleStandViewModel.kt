package com.example.nasugbutricycleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nasugbutricycleapp.model.TricycleStand
import com.example.nasugbutricycleapp.service.TricycleStandService

class TricycleStandViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _stands = MutableLiveData<List<TricycleStand>>(emptyList())
    val stands: LiveData<List<TricycleStand>> = _stands

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isSuccessful = MutableLiveData<Boolean?>()
    val isSuccessful: LiveData<Boolean?> = _isSuccessful

    // ADD A TRICYCLE STAND
    fun addStand(stand: TricycleStand) {
        _isLoading.value = true

        TricycleStandService.addStand(stand) { success, resultMessage ->
            _isLoading.postValue(false)
            _isSuccessful.postValue(success)
            _message.postValue(resultMessage)

            if (success) {
                getActiveStands()
            }
        }
    }

    // GET ACTIVE TRICYCLE STANDS
    fun getActiveStands() {
        _isLoading.value = true

        TricycleStandService.getActiveStands { result, error ->
            _isLoading.postValue(false)
            _stands.postValue(result)

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
