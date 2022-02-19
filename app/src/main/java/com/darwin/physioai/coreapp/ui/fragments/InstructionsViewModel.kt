package com.darwin.physioai.coreapp.ui.fragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physioai.data.Repository.InstructionsRepository
import com.example.physioai.data.models.InstructionRes
import com.example.physioai.data.network.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class InstructionsViewModel @Inject constructor(private val repository: InstructionsRepository) : ViewModel() {

    private val _instructionsRes: MutableLiveData<Resource<InstructionRes>> = MutableLiveData()
    val instructionsRes: LiveData<Resource<InstructionRes>>
        get() = _instructionsRes

    fun getInstructions(exercise: JsonObject) = viewModelScope.launch {
        Log.d("LogIns", exercise.toString())
        _instructionsRes.value = Resource.Loading
        _instructionsRes.value = repository.getInstructions(exercise)
    }
}
