@file:Suppress("DEPRECATION")

package com.example.momogu

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.example.momogu.databinding.ActivityAddPostBinding
import com.example.momogu.utils.Constanta
import com.example.momogu.utils.Constanta.REQUEST_POST_IMAGE
import com.example.momogu.utils.Constanta.REQUEST_VIDEO_CODE
import com.example.momogu.utils.Constanta.coordinateLatitude
import com.example.momogu.utils.Constanta.coordinateLongitude
import com.example.momogu.utils.Constanta.isLocationPicked
import com.example.momogu.utils.Helper
import com.example.momogu.utils.Helper.decimalFormatET
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage

class AddPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPostBinding

    private var myUrl = ""
    private var myVideoUrl = ""
    private var imageUri: Uri? = null
    private var videoUri: Uri? = null
    private var storagePicRef: StorageReference? = null
    private var storageVideoRef: StorageReference? = null
    private var getResult: ActivityResultLauncher<Intent>? = null
    private var isPicked: Boolean? = false


    @SuppressLint("IntentReset")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        storagePicRef = FirebaseStorage.getInstance().reference.child("Post Pictures")

        storageVideoRef = FirebaseStorage.getInstance().reference.child("Post Video")

        visVal()
        dropdownItem()

        decimalFormatET(binding.etPriceAdd)
        decimalFormatET(binding.etShippingAdd)

        binding.saveAdd.setOnClickListener {
            uploadImage()
        }

        binding.closeAdd.setOnClickListener {
            finish()
        }

        binding.layoutImageAdd.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_POST_IMAGE)
        }

        binding.btnClearImageAdd.setOnClickListener {
            binding.imageAdd.setImageURI(null)
            binding.cvImageAdd.visibility = View.GONE
            binding.btnClearImageAdd.visibility = View.GONE
        }

        binding.layoutVideoAdd.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.type = "video/*"
            startActivityForResult(intent, REQUEST_VIDEO_CODE)
        }

        binding.btnClearVideoAdd.setOnClickListener {
            binding.tvMaxVideoAdd.visibility = View.VISIBLE
            binding.tvFileVideoAdd.visibility = View.GONE
            binding.btnClearVideoAdd.visibility = View.GONE
        }

        isLocationPicked.postValue(false)

        binding.btnClearLocationAdd.setOnClickListener {
            isLocationPicked.postValue(false)
        }

        getResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.let { res ->
                    isPicked = res.getBooleanExtra(Constanta.LocationPicker.IsPicked.name, false)
                    isLocationPicked.postValue(isPicked)
                    val lat = res.getDoubleExtra(
                        Constanta.LocationPicker.Latitude.name,
                        0.0
                    )
                    val lon = res.getDoubleExtra(
                        Constanta.LocationPicker.Longitude.name,
                        0.0
                    )
                    binding.tvFieldLocationAdd.text = Helper.parseAddressLocation(this, lat, lon)
                    coordinateLatitude = lat
                    coordinateLongitude = lon
                }
            }
        }

        binding.btnSelectLocationAdd.setOnClickListener {
            if (Helper.isPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                val intentPickLocation = Intent(this, MapAdminActivity::class.java)
                getResult?.launch(intentPickLocation)
            } else {
                ActivityCompat.requestPermissions(
                    this@AddPostActivity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    Constanta.LOCATION_PERMISSION_CODE
                )
            }
        }

        isLocationPicked.observe(this) {
            binding.previewLocationAdd.isVisible = it
            binding.btnSelectLocationAdd.isVisible = !it
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_POST_IMAGE) {
            val uri = data?.data
            CropImage.activity(uri)
                .setAspectRatio(4, 3)
                .start(this)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            binding.imageAdd.setImageURI(imageUri)
            binding.cvImageAdd.visibility = View.VISIBLE
            binding.btnClearImageAdd.visibility = View.VISIBLE
        }

        if (requestCode == REQUEST_VIDEO_CODE && resultCode == RESULT_OK && data != null) {
            videoUri = data.data
            videoUri?.let {
                val videoName = getVideoName(videoUri!!)

                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(this, it)

                val duration =
                    mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLong()

                if (duration != null && duration > 30000) {
                    Toast.makeText(this, getString(R.string.toast_video_max_add), Toast.LENGTH_LONG).show()
                } else {
                    binding.tvFileVideoAdd.text = videoName
                    binding.tvMaxVideoAdd.visibility = View.GONE
                    binding.tvFileVideoAdd.visibility = View.VISIBLE
                    binding.btnClearVideoAdd.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getVideoName(videoUri: Uri): String {
        val cursor = contentResolver.query(videoUri, null, null, null, null)
        cursor?.moveToFirst()
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val videoName = nameIndex?.let { cursor.getString(it) }
        cursor?.close()
        return videoName ?: "Unknown"
    }

    private fun dropdownItem() {
        val gender = resources.getStringArray(R.array.Gender)
        val arrayGender = ArrayAdapter(this, R.layout.dropdown_item, gender)
        binding.etGenderAdd.setAdapter(arrayGender)

        val typeCow = resources.getStringArray(R.array.TypeCow)
        val arrayCow = ArrayAdapter(this, R.layout.dropdown_item, typeCow)
        binding.etTypeAdd.setAdapter(arrayCow)
    }

    private fun visVal() {
        binding.tiTypeAdd.visibility = View.GONE
        binding.tiAgeAdd.visibility = View.GONE
        binding.tiWeightAdd.visibility = View.GONE
        binding.tiColorAdd.visibility = View.GONE
        binding.tiGenderAdd.visibility = View.GONE
        binding.tiDescAdd.visibility = View.GONE
        binding.tiPriceAdd.visibility = View.GONE
        binding.tiShippingAdd.visibility = View.GONE
        binding.cbLetterAdd.visibility = View.GONE
        binding.cbConditionAdd.visibility = View.GONE

        binding.tvFileVideoAdd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tiTypeAdd.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etTypeAdd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tiAgeAdd.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etAgeAdd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tiWeightAdd.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etWeightAdd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tiColorAdd.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etColorAdd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tiGenderAdd.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etGenderAdd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tiDescAdd.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etDescAdd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tiPriceAdd.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etPriceAdd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tiShippingAdd.visibility =
                    if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etShippingAdd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnSelectLocationAdd.visibility =
                    if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.tvFieldLocationAdd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.cbLetterAdd.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                binding.cbConditionAdd.visibility =
                    if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    @Suppress("DEPRECATION")
    private fun uploadImage() {
        when {

            imageUri == null -> {
                Toast.makeText(this, getString(R.string.toast_image_add), Toast.LENGTH_LONG).show()
            }

            videoUri == null -> {
                Toast.makeText(this, getString(R.string.toast_video_add), Toast.LENGTH_LONG).show()
            }

            binding.etTypeAdd.text.isNullOrEmpty() -> {
                binding.etTypeAdd.error = getString(R.string.toast_type_add)
            }

            binding.etAgeAdd.text.isNullOrEmpty() -> {
                binding.etAgeAdd.error = getString(R.string.toast_age_add)
            }

            binding.etWeightAdd.text.isNullOrEmpty() -> {
                binding.etWeightAdd.error = getString(R.string.toast_weight_add)
            }

            binding.etColorAdd.text.isNullOrEmpty() -> {
                binding.etColorAdd.error = getString(R.string.toast_color_add)
            }

            binding.etGenderAdd.text.isNullOrEmpty() -> {
                binding.etGenderAdd.error = getString(R.string.toast_gender_add)
            }

            binding.etDescAdd.text.isNullOrEmpty() -> {
                binding.etDescAdd.error = getString(R.string.toast_desc_add)
            }

            binding.etPriceAdd.text.isNullOrEmpty() -> {
                binding.etPriceAdd.error = getString(R.string.toast_price_add)
            }

            binding.etShippingAdd.text.isNullOrEmpty() -> {
                binding.etShippingAdd.error = getString(R.string.toast_shipping_add)
            }

            binding.tvFieldLocationAdd.text.isNullOrEmpty() -> {
                Toast.makeText(this, getString(R.string.toast_location_add), Toast.LENGTH_LONG).show()
            }

            binding.cbLetterAdd.isChecked.not() -> {
                Toast.makeText(this, getString(R.string.toast_letter_add), Toast.LENGTH_LONG).show()
            }

            binding.cbConditionAdd.isChecked.not() -> {
                Toast.makeText(this, getString(R.string.toast_condition_add), Toast.LENGTH_LONG).show()
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle(getString(R.string.toast_add_cow))
                progressDialog.setMessage(getString(R.string.toast_wait_add))
                progressDialog.show()

                val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                val postId = ref.push().key

                val fileRef = storagePicRef!!.child("$postId.jpg")
                val fileVideoRef = storageVideoRef!!.child("$postId.mp4")

                val fileRefTask = fileRef.putFile(imageUri!!).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            progressDialog.dismiss()
                            throw it
                        }
                    }
                    fileRef.downloadUrl
                }

                val fileVideoRefTask = fileVideoRef.putFile(videoUri!!).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            progressDialog.dismiss()
                            throw it
                        }
                    }
                    fileVideoRef.downloadUrl
                }
                Tasks.whenAllSuccess<Uri>(fileRefTask, fileVideoRefTask)
                    .addOnSuccessListener { task ->
                        myUrl = task[0].toString()
                        myVideoUrl = task[1].toString()

                        val postMap = HashMap<String, Any>()

                        postMap["postid"] = postId!!
                        postMap["postimage"] = myUrl
                        postMap["postvideo"] = myVideoUrl
                        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["product"] = binding.etTypeAdd.text.toString()
                        postMap["age"] = binding.etAgeAdd.text.toString()
                        postMap["weight"] = binding.etWeightAdd.text.toString()
                        postMap["color"] = binding.etColorAdd.text.toString()
                        postMap["gender"] = binding.etGenderAdd.text.toString()
                        postMap["desc"] = binding.etDescAdd.text.toString()
                        postMap["price"] = binding.etPriceAdd.text.toString()
                        postMap["shipping"] = binding.etShippingAdd.text.toString()
                        postMap["latitude"] = coordinateLatitude
                        postMap["longitude"] = coordinateLongitude
                        postMap["location"] = binding.tvFieldLocationAdd.text.toString()
                        postMap["dateTime"] = System.currentTimeMillis().toString()

                        ref.child(postId).updateChildren(postMap)

                        Toast.makeText(this, getString(R.string.toast_cow_add), Toast.LENGTH_LONG)
                            .show()

                        finish()
                        progressDialog.dismiss()
                    }.addOnFailureListener {
                        progressDialog.dismiss()
                    }
            }
        }
    }
}
