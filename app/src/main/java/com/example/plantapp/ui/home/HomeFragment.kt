package com.example.plantapp.ui.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.plantapp.R
import com.example.plantapp.databinding.FragmentHomeBinding
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
import java.util.concurrent.TimeUnit

import android.content.Context
import android.provider.Settings
import android.widget.ProgressBar
import androidx.navigation.NavOptions

class HomeFragment : Fragment() {
    val REQUEST_IMAGE_CAPTURE = 1
    private var _binding: FragmentHomeBinding? = null
    private lateinit var progressBar : ProgressBar

    companion object {
        const val BASE_URL = "https://dd1a-168-181-208-155.ngrok-free.app"
        const val DEV = false;
    }
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        val textView2: TextView = binding.texto1
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        homeViewModel.texto1.observe(viewLifecycleOwner) {
            textView2.text = it
        }
        progressBar = binding.progressBar
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the ImageView
        val imageView: ImageView = binding.sampleImageView

        // Set an image to the ImageView
        imageView.setImageResource(R.drawable.planta2) // Use your image resource here

        binding.btnCamera.setOnClickListener {
            dispatchTakePictureIntent()
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

            // binding.imgViewer.setImageBitmap(imageBitmap)
            val result = uploadImage(imageBitmap)



        }
    }


    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }





    private fun uploadImage(bitmap: Bitmap) {

        progressBar.setVisibility(View.VISIBLE);
        val client =
            OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS)  // Connection timeout
                .readTimeout(30, TimeUnit.SECONDS)     // Read timeout
                .writeTimeout(30, TimeUnit.SECONDS)    // Write timeout
                .build()

        val byteArray = bitmapToByteArray(bitmap)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                "image.jpg",
                RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
            )
            .build()

        val request = Request.Builder()
            .url("$BASE_URL/uploadFromCamera?dev=$DEV&deviceId=" + getDeviceId(requireContext()))
            .header("ngrok-skip-browser-warning", "asd")
            .header("User-Agent", "asd")
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
                    var fertilizer = ""
                    var plantDescription = ""
                    var deseaseName = ""
                    var deseaseDescription = ""
                    var deseaseScientificName = ""
                    var solution = ""

                    if (jsonData != null) {
                        var jsonResponse = JSONObject(jsonData)
                        val listDeseases = jsonResponse.getJSONArray("deseases")
                        deseasePlant = jsonResponse.getBoolean("isDesease")
                        plantDescription = jsonResponse.getString("description")

                         val firstChoice = listDeseases.getJSONObject(0)
                          deseaseName = firstChoice.getString("deseaseName")
                          deseaseDescription = firstChoice.getString("deseaseDescription")
                          deseaseScientificName = firstChoice.getString("deseaseScientificName")
                          solution = firstChoice.getString("solution")
                          fertilizer = firstChoice.getString("fertilizer")
                    }

                        val bundle = Bundle().apply {
                            putBoolean("deseasePlant", deseasePlant)
                            putString("plantDescription", plantDescription)
                            putString("deseaseName", deseaseName)
                            putString("deseaseDescription", deseaseDescription)
                            putString("deseaseScientificName", deseaseScientificName)
                            putString("solution", solution)
                            putString("fertilizer", fertilizer)
                            putParcelable("image", bitmap)
                        }

                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.navigation_home, true) // Clear everything up to HomeFragment
                        .build()
                    requireActivity().runOnUiThread {
                        progressBar.setVisibility(View.GONE);
                        // Code that needs to run on the main thread
                        findNavController().navigate(
                            R.id.action_homeFragment_to_infoFragment,
                            bundle,navOptions
                        )
                    }



                        println("Upload successful")
                   // }
                } else {
                    println("Upload failed: ${response.message}")
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

}