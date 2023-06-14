package com.example.momogu.fragments

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.momogu.R
import com.example.momogu.databinding.FragmentScalesBinding
import com.example.momogu.utils.Helper.showDialogInfo
import com.github.chrisbanes.photoview.PhotoView

class ScalesFragment : Fragment() {

    private lateinit var binding: FragmentScalesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScalesBinding.inflate(inflater)

        val preferences = requireContext().getSharedPreferences("MyPrefs", MODE_PRIVATE)
        if (preferences != null) {
            binding.etChestScales.setText(preferences.getString("text1", ""))
            binding.etBodyScales.setText(preferences.getString("text2", ""))
            binding.etWeightScales.setText(preferences.getString("text3", ""))
        }

        binding.etChestScales.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hitung()
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        binding.etBodyScales.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hitung()
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        binding.exteriorImage.setOnClickListener {
            costumImage()
        }

        binding.tvDetailExterior.setOnClickListener {
            showDialogInfo(
                requireContext(),
                getString(R.string.UI_info_exterior),
                Gravity.START
            )
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun hitung() {
        val a = binding.etChestScales.text.toString().toDoubleOrNull() ?: 0.0
        val b = binding.etBodyScales.text.toString().toDoubleOrNull() ?: 0.0
        val hasilHitung = (a * a * b) / 10840
        binding.etWeightScales.setText(String.format("%.1f KG", hasilHitung))
    }

    private fun costumImage() {

        val mBuilder = AlertDialog.Builder(requireContext())
        val mView = layoutInflater.inflate(R.layout.dialog_layout_image, null)

        val imageView = mView.findViewById<PhotoView>(R.id.imageView)
        imageView.setImageResource(R.drawable.exterior_sapi)

        mBuilder.setView(mView)
        val mDialog = mBuilder.create()
        mDialog.show()

    }

    override fun onPause() {
        super.onPause()
        saveText()
    }

    @SuppressLint("CommitPrefEdits")
    private fun saveText() {
        val editor = requireContext().getSharedPreferences("MyPrefs", MODE_PRIVATE).edit()
        editor.putString("text1", binding.etChestScales.text.toString())
        editor.putString("text2", binding.etBodyScales.text.toString())
        editor.putString("text3", binding.etWeightScales.text.toString())
        editor.apply()
    }

}