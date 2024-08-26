package com.example.plantapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class HomeModel(
    val texto1: String,
    val texto2: String
)
class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Toma fotos de las partes enfermas de la planta con la cámara"

    }
    private val _texto1 = MutableLiveData<String>().apply {
        value = "Consejo 1"
    }
    val texto1:LiveData<String> = _texto1
    val text: LiveData<String> = _text
}