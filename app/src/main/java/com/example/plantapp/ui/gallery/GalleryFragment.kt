package com.example.plantapp.ui.gallery

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.plantapp.R
import com.example.plantapp.databinding.FragmentGalleryBinding
import com.example.plantapp.databinding.FragmentInfoBinding
import com.example.plantapp.ui.home.HomeFragment.Companion.BASE_URL
import com.example.plantapp.ui.home.HomeFragment.Companion.DEV
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 * Use the [GalleryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GalleryFragment : Fragment() {
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private lateinit var gridView : GridView
    private lateinit var progressBar2 : ProgressBar

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageGridAdapter

    private var images: List<Image> = listOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_gallery, container, false)
        progressBar2 = view.findViewById(R.id.progress_bar)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 2) // 2 columns in the grid
        adapter = ImageGridAdapter(images)
        recyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragment = GalleryFragment()

        progressBar2.setVisibility(View.VISIBLE);
        val client =
            OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS)  // Connection timeout
                .readTimeout(30, TimeUnit.SECONDS)     // Read timeout
                .writeTimeout(30, TimeUnit.SECONDS)    // Write timeout
                .build()

        val request = Request.Builder()
            .url("$BASE_URL/captures?&deviceId=" + getDeviceId(requireContext()))
            .get()
            .build()

        val imageList = mutableListOf<Image>()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonData = response.body?.string();
                    if (jsonData != null) {
                        var jsonResponse = JSONObject(jsonData)
                        val imagesArray: JSONArray = jsonResponse.getJSONArray("capture")

                        // Loop through each item in the array
                        for (i in 0 until imagesArray.length()) {
                            // Get the JSONObject representing a single image
                            val imageObject: JSONObject = imagesArray.getJSONObject(i)

                            // Extract the 'imageUrl' and create an Image object
                            val imageUrl = imageObject.getString("urlImage")
                            val Texto = imageObject.getString("scientific_name")
                            val image = Image(imageUrl,Texto)

                            // Add the Image object to the list
                            imageList.add(image)
                        }
                        val immutableImages: List<Image> = imageList.toList()
                        val images = immutableImages

                        requireActivity().runOnUiThread {
                            progressBar2.setVisibility(View.GONE);
                            adapter = ImageGridAdapter(images)
                            recyclerView.adapter = adapter
                        }
                    }
                }else{
                println("Upload failed: ${response.message}")
            }
        }
        })
    }



    // Method to set images in the fragment
    fun setImages(images: List<Image>) {
        this.images = images
        adapter.notifyDataSetChanged()  // Notify the adapter of data change
    }



    class ImageGridAdapter(private val images: List<Image>) : RecyclerView.Adapter<ImageGridAdapter.ImageViewHolder>() {

        class ImageViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.imageView)
            val imageText: TextView = view.findViewById(R.id.imageName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_items, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val image = images[position]
            // Load the image using Glide, Picasso, or any other image loading library
            Glide.with(holder.view.context)
                .load(image.imageUrl)
                .into(holder.imageView)

            holder.imageText.text = image.descripcion

        }

        override fun getItemCount() = images.size
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

}