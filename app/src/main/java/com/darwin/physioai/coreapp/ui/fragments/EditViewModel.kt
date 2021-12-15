package com.darwin.physioai.coreapp.ui.fragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physioai.data.Repository.EditRepository
import com.example.physioai.data.models.EditResponse
import com.example.physioai.data.network.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class EditViewModel @Inject constructor(private val repository: EditRepository) : ViewModel() {

    private val _EditRes: MutableLiveData<Resource<EditResponse>> = MutableLiveData()
    val EditRes: LiveData<Resource<EditResponse>>
        get() = _EditRes

    fun updateProfile(user: JsonObject) = viewModelScope.launch {
        Log.d("LogEditViewModel", user.toString())
        _EditRes.value = Resource.Loading
        _EditRes.value = repository.updateProfile(user)
    }
}