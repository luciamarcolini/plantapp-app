package com.example.plantapp.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Toma fotos de las partes enfermas de la planta con la cámara"
    }
    val text: LiveData<String> = _text
}