package com.darwin.physioai.coreapp.ui.fragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physioai.data.Repository.ForgetRepository
import com.example.physioai.data.models.forgetpassResponse
import com.example.physioai.data.network.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgetPasswordViewModel @Inject constructor(private val repository: ForgetRepository) : ViewModel() {
    private val _forgetpass: MutableLiveData<Resource<forgetpassResponse>> = MutableLiveData()
    val forgetpass: LiveData<Resource<forgetpassResponse>>
        get() = _forgetpass

    fun getforpass(num: JsonObject) = viewModelScope.launch {
        Log.d("LogLoginViewModel", num.toString())
        _forgetpass.value = Resource.Loading
        _forgetpass.value = repository.getforgetpass(num)
    }
}