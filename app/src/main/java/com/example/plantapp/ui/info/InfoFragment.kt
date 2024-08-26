package com.example.plantapp.ui.info

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.createBitmap
import com.example.plantapp.R
import com.example.plantapp.databinding.FragmentHomeBinding
import com.example.plantapp.databinding.FragmentInfoBinding

class InfoFragment : Fragment() {

    private lateinit var textView1: TextView
    private lateinit var textView2: TextView
    private lateinit var textView3: TextView
    private lateinit var textView4: TextView
    private lateinit var textView5: TextView
    private lateinit var textView6: TextView
    private lateinit var textView7: TextView
    private lateinit var ImageView: ImageView
    private var _binding: FragmentInfoBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deseasePlant = arguments?.getBoolean("deseasePlant")
        /*textView1 = binding.textView1
         if (deseasePlant == true) {
             textView1.text = "La planta está enferma!"
         } else {
             textView1.text = "La planta está sana!"
         }*/

        val scientific_name = arguments?.getString("scientific_name")
        textView2 = binding.textView2
        textView2.text = scientific_name
        val deseaseScientificName = arguments?.getString("deseaseScientificName")
        val deseaseName = arguments?.getString("deseaseName")
        textView3 = binding.textView3
        textView3.text = "$deseaseName"

        val deseaseDescription = arguments?.getString("deseaseDescription")
        textView4 = binding.textView4
        textView4.text = deseaseDescription

        val descriptionPlant = arguments?.getString("plantDescription")
        textView5 = binding.textView5
        textView5.text = descriptionPlant

        val solution = arguments?.getString("solution")
        textView6 = binding.textView6
        textView6.text = solution

        val fertilizer = arguments?.getString("fertilizer")
        textView7 = binding.textView7
        textView7.text = fertilizer

        val image : Bitmap? = arguments?.getParcelable("image")
        ImageView = binding.imageView
        ImageView.setImageBitmap(image)



    }
}