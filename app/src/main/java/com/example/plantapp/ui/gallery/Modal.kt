package com.example.plantapp.ui.gallery

import android.graphics.Bitmap

class Modal(name: String, image: Int) {
    var name:String? = null
    var image:Bitmap? = null

}
data class Image(val imageUrl: String, val descripcion: String)