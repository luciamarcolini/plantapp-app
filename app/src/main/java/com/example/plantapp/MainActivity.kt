package com.example.plantapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.Image
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.plantapp.ui.gallery.GalleryFragment
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var binding: ActivityMainBinding

    companion object {
        const val BASE_URL = "https://dd1a-168-181-208-155.ngrok-free.app"
        const val DEV = true;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*seteo layout*/
        setContentView(R.layout.activity_main)

        /*inicializar botton navigation*/
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)
        // Set scan screen selected
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);



      /*  supportFragmentManager.beginTransaction()
            .replace(R.id.galle, fragment)
            .commit()*/
        /*bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_scan -> {
                    dispatchTakePictureIntent()
                    true
                }
                else -> {
                    // Handle other menu items
                    val navController2 = findNavController(R.id.nav_host_fragment)
                    navController2.navigate(item.itemId)
                    true
                }
            }
        }*/


       
        
        // binding = ActivityMainBinding.inflate(layoutInflater)
       // setContentView(binding.root)

        //binding.btnCamera.setOnClickListener {
           // dispatchTakePictureIntent()

           // val navView: BottomNavigationView = binding.navView
            Log.w("myApp", "Entra");
         //   val navController = findNavController(R.id.nav_host_fragment)
           // val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
          //  val appBarConfiguration = AppBarConfiguration(
            //    setOf(
              //      R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
               // )
           // )

            //setupActionBarWithNavController(navController, appBarConfiguration)

    //    bottomNavigationView.setupWithNavController(navController)
       // }

    }

 /*   private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        }
    }*/


    

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

           // binding.imgViewer.setImageBitmap(imageBitmap)
            val result = uploadImage(imageBitmap)



        }
    }


    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }





    private fun uploadImage(bitmap: Bitmap){
        val client = OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS)  // Connection timeout
            .readTimeout(30, TimeUnit.SECONDS)     // Read timeout
            .writeTimeout(30, TimeUnit.SECONDS)    // Write timeout
            .build()

        val byteArray = bitmapToByteArray(bitmap)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "image.jpg", RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray))
            .build()

        val request = Request.Builder()
            .url("$BASE_URL/uploadFromCamera?dev=$DEV")
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
                    var deseasePlant = false
                    var deseaseName = ""
                    var deseaseDescription = ""
                    var deseaseCientificName = ""

                    if(jsonData != null){
                        var jsonResponse = JSONObject(jsonData)
                        val choicesArray = jsonResponse.getJSONArray("posibleEnfermedad")
                        val firstChoice = choicesArray.getJSONObject(0)
                        deseaseName = firstChoice.getString("enfermedad")
                        deseaseDescription = firstChoice.getString("descripcion")
                        deseaseCientificName = firstChoice.getString("nombreCientifico")
                        //extractedValue = jsonResponse.getString("choices")
                    }
                    runOnUiThread{
                        val texto= "$deseaseName $deseaseDescription $deseaseCientificName";
                        //binding.body.text = texto
                    }

                    println("Upload successful")
                } else {
                    println("Upload failed: ${response.message}")
                }
            }
        })
    }
}