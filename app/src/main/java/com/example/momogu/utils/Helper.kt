@file:Suppress("DEPRECATION")

package com.example.momogu.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.momogu.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar

object Helper {

    //Date
    @SuppressLint("SimpleDateFormat")
    fun getDate(milliSeconds: Long, dateFormat: String?): String? {
        val formatter = SimpleDateFormat(dateFormat)
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun isPermissionGranted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    //DecimalFormat
    fun decimalFormatET(editText: EditText) {
        val decimalFormat = DecimalFormat("#,##0")

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editText.removeTextChangedListener(this)
                try {
                    val value = s.toString().replace(Regex("[.,]"), "")
                    val formatted = decimalFormat.format(value.toDouble())
                    editText.setText(formatted)
                    editText.setSelection(formatted.length)
                } catch (e: NumberFormatException) {
                    Toast.makeText(editText.context, "Format angka tidak valid", Toast.LENGTH_SHORT).show()
                }
                editText.addTextChangedListener(this)
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        editText.addTextChangedListener(textWatcher)
    }


    //AddressLocation
    fun parseAddressLocation(
        context: Context,
        lat: Double,
        lon: Double
    ): String {
        val geocoder = Geocoder(context)
        val geoLocation =
            geocoder.getFromLocation(lat, lon, 1)
        if (geoLocation != null) {
            return if (geoLocation.size > 0) {
                val location = geoLocation[0]
                val fullAddress = location.getAddressLine(0)
                StringBuilder("")
                    .append(fullAddress).toString()
            } else {
                "Location Unknown"
            }
        }
        return parseAddressLocation(context, lat, lon)
    }

    //CityLocation
    fun parseCityLocation(
        context: Context,
        lat: Double,
        lon: Double
    ): String {
        val geocoder = Geocoder(context)
        val geoLocation =
            geocoder.getFromLocation(lat, lon, 1)
        if (geoLocation != null) {
            return if (geoLocation.size > 0) {
                val location = geoLocation[0].subAdminArea
                StringBuilder("")
                    .append(location).toString()
            } else {
                "City Unknown"
            }
        }
        return parseAddressLocation(context, lat, lon)
    }

    //costumDialog
    fun showDialogInfo(
        context: Context,
        message: String,
        alignment: Int = Gravity.CENTER
    ) {
        val dialog = dialogInfoBuilder(context, message, alignment)
        val btnOk = dialog.findViewById<Button>(R.id.button_ok)
        btnOk.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun dialogInfoBuilder(
        context: Context,
        message: String,
        alignment: Int = Gravity.CENTER
    ): Dialog {
        val dialog = Dialog(context)
        dialog.setCancelable(false)
        dialog.window!!.apply {
            val params: WindowManager.LayoutParams = this.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.windowAnimations = android.R.transition.fade
            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.setContentView(R.layout.dialog_info)
        val tvMessage = dialog.findViewById<TextView>(R.id.message)
        when (alignment) {
            Gravity.CENTER -> tvMessage.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER
            Gravity.START -> tvMessage.gravity = Gravity.CENTER_VERTICAL or Gravity.START
            Gravity.END -> tvMessage.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        }
        tvMessage.text = message
        return dialog
    }

}