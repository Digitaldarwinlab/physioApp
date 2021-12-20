package com.darwin.physioai.posenet.core

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darwin.physioai.coreapp.data.Repository.AIscreenRepository
import com.darwin.physioai.coreapp.data.models.AIscreenResponse
import com.example.physioai.data.models.profileResponse
import com.example.physioai.data.network.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PoseNetViewModel @Inject constructor(private val repository: AIscreenRepository): ViewModel() {

    private val _AIRes: MutableLiveData<Resource<AIscreenResponse>> = MutableLiveData()
    val AIRes: LiveData<Resource<AIscreenResponse>>
        get() = _AIRes

    fun updateCP(user: JsonObject) = viewModelScope.launch {
        Log.d("LogLoginViewModel", user.toString())
        _AIRes.value = Resource.Loading
        _AIRes.value = repository.updateCP(user)
    }
}