package com.example.momogu.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.momogu.databinding.FragmentScalesBinding

class ScalesFragment : Fragment() {

    private lateinit var binding: FragmentScalesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentScalesBinding.inflate(inflater)

        binding.etChest.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hitung()
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        binding.etBody.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hitung()
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun hitung() {
        val a = binding.etChest.text.toString().toDoubleOrNull() ?: 0.0
        val b = binding.etBody.text.toString().toDoubleOrNull() ?: 0.0
        val hasilHitung = (a * a * b) / 10840
        binding.etWeight.setText(String.format("%.1f KG", hasilHitung))
    }

}