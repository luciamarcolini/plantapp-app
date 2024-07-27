package com.example.plantapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.plantapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import android.util.Log
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCamera.setOnClickListener {
            dispatchTakePictureIntent()

            val navView: BottomNavigationView = binding.navView
            Log.w("myApp", "Entra");
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
                )
            )

            //setupActionBarWithNavController(navController, appBarConfiguration)

            navView.setupWithNavController(navController)
        }

    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap

            binding.imgViewer.setImageBitmap(imageBitmap)
            val result = uploadImage(imageBitmap)



        }
    }



    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }



    private fun uploadImage(bitmap: Bitmap){
        val client = OkHttpClient()

        val byteArray = bitmapToByteArray(bitmap)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "image.jpg", RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray))
            .build()

        val request = Request.Builder()
            .url("https://2e36-168-181-208-155.ngrok-free.app/uploadFromCamera?dev=true")
            .header("ngrok-skip-browser-warning","asd")
            .header("User-Agent","asd")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonData = response.body?.string();
                    var extractedValue = ""
                    if(jsonData != null){
                        var jsonResponse = JSONObject(jsonData)
                        val choicesArray = jsonResponse.getJSONArray("choices")
                        val firstChoice = choicesArray.getJSONObject(0)
                        val messageObject = firstChoice.getJSONObject("message")
                        extractedValue = messageObject.getString("content")
                        //extractedValue = jsonResponse.getString("choices")
                    }
                    runOnUiThread{
                        binding.body.setText(extractedValue)
                    }

                    println("Upload successful")
                } else {
                    println("Upload failed: ${response.message}")
                }
            }
        })
    }
}