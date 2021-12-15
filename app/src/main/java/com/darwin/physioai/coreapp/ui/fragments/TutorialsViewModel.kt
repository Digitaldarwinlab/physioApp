package com.darwin.physioai.coreapp.ui.fragments

import androidx.lifecycle.ViewModel
import com.example.physioai.data.Repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class TutorialsViewModel @Inject constructor(private val repository: UserRepository) : ViewModel() {

}