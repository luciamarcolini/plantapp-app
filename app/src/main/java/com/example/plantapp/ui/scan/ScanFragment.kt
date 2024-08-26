package com.example.plantapp.ui.scan

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.os.Bundle
import android.content.Intent
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.plantapp.MainActivity
import com.example.plantapp.R
import com.example.plantapp.databinding.ActivityMainBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit



class ScanFragment : Fragment() {
    val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var binding: ActivityMainBinding
    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor : ExecutorService
    companion object {
        const val BASE_URL = "https://dd1a-168-181-208-155.ngrok-free.app"
        const val DEV = true;
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }




    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Toast.makeText(requireContext(), "Failed to start camera.", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }




   /* override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //previewView = view.findViewById(R.id.previewView)
        previewView = view.findViewById(R.id.previewView)
        val captureButton = view.findViewById<Button>(R.id.captureButton)

        // Initialize the camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        view.post {  startCamera()}


        // Capture button click listener
        captureButton.setOnClickListener {
           // takePhoto()
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

              //  binding.img_viewer.setImageBitmap(imageBitmap)
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
                    .url("${MainActivity.BASE_URL}/uploadFromCamera?dev=${MainActivity.DEV}")
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
                            //runOnUiThread{
                           //     val texto= "$deseaseName $deseaseDescription $deseaseCientificName";
                               // binding.body.text = texto
                            //}

                            println("Upload successful")
                        } else {
                            println("Upload failed: ${response.message}")
                        }
                    }
                })
            }

}