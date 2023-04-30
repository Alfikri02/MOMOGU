package com.example.momogu

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.example.momogu.databinding.ActivityAddPostBinding
import com.example.momogu.utils.Constanta
import com.example.momogu.utils.Constanta.REQUEST_POST_IMAGE
import com.example.momogu.utils.Constanta.coordinateLatitude
import com.example.momogu.utils.Constanta.coordinateLongitude
import com.example.momogu.utils.Constanta.isLocationPicked
import com.example.momogu.utils.Helper
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import java.text.DecimalFormat

class AddPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPostBinding

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPicRef: StorageReference? = null


    private var getResult: ActivityResultLauncher<Intent>? = null
    private var isPicked: Boolean? = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Post Pictures")

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
                    binding.fieldLocation.text = Helper.parseAddressLocation(this, lat, lon)
                    coordinateLatitude = lat
                    coordinateLongitude = lon
                }
            }
        }

        binding.saveNewPostBtn.setOnClickListener {
            uploadImage()
        }

        binding.closeAddPostBtn.setOnClickListener {
            finish()
        }

        binding.imagePost.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_POST_IMAGE)
        }

        binding.etPrice.addTextChangedListener(object : TextWatcher {
            val decimalFormat = DecimalFormat("#,##0")

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etPrice.removeTextChangedListener(this)
                try {
                    val value = s.toString().replace(".", "").replace(",", "")
                    val formatted = decimalFormat.format(value.toDouble())
                    binding.etPrice.setText(formatted)
                    binding.etPrice.setSelection(formatted.length)
                } catch (_: NumberFormatException) {
                }
                binding.etPrice.addTextChangedListener(this)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etShipping.addTextChangedListener(object : TextWatcher {
            val decimalFormat = DecimalFormat("#,##0")

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etShipping.removeTextChangedListener(this)
                try {
                    val value = s.toString().replace(".", "").replace(",", "")
                    val formatted = decimalFormat.format(value.toDouble())
                    binding.etShipping.setText(formatted)
                    binding.etShipping.setSelection(formatted.length)
                } catch (_: NumberFormatException) {
                }
                binding.etShipping.addTextChangedListener(this)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        isLocationPicked.postValue(false)

        binding.btnClearLocation.setOnClickListener {
            isLocationPicked.postValue(false)
        }

        binding.btnSelectLocation.setOnClickListener {
            /* check permission to granted apps pick user location */
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
            /* if location picked -> show picked location address, else -> hide address & show pick location button */
            binding.previewLocation.isVisible = it
            binding.btnSelectLocation.isVisible = !it
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
            binding.imagePost.setImageURI(imageUri)
        }
    }

    @Suppress("DEPRECATION")
    private fun uploadImage() {
        when {

            imageUri == null -> {
                Toast.makeText(this, "Silahkan pilih foto sapi anda!", Toast.LENGTH_LONG).show()
            }

            binding.etProduct.text.isNullOrEmpty() -> {
                binding.etProduct.error = "Nama sapi dibutuhkan!"
            }

            binding.etAge.text.isNullOrEmpty() -> {
                binding.etAge.error = "Umur sapi dibutuhkan!"
            }

            binding.etWeight.text.isNullOrEmpty() -> {
                binding.etWeight.error = "Bobot sapi dibutuhkan!"
            }

            binding.etColor.text.isNullOrEmpty() -> {
                binding.etColor.error = "Warna sapi dibutuhkan!"
            }

            binding.etGender.text.isNullOrEmpty() -> {
                binding.etGender.error = "Jenis kelamin sapi dibutuhkan!"
            }

            binding.etDesc.text.isNullOrEmpty() -> {
                binding.etDesc.error = "Deskripsi sapi dibutuhkan!"
            }

            binding.etPrice.text.isNullOrEmpty() -> {
                binding.etPrice.error = "Harga sapi dibutuhkan!"
            }

            binding.etShipping.text.isNullOrEmpty() -> {
                binding.etShipping.error = "Ongkos pengiriman sapi dibutuhkan!"
            }

            binding.fieldLocation.text.isNullOrEmpty() -> {
                Toast.makeText(this, "Silahkan pilih lokasi sapi anda!", Toast.LENGTH_LONG).show()
            }

            binding.cbLetter.isChecked.not() -> {
                Toast.makeText(this, "Penjaminan surat sapi dibutuhkan!", Toast.LENGTH_LONG).show()
            }

            binding.cbCondition.isChecked.not() -> {
                Toast.makeText(this, "Penjaminan sapi dibutuhkan!", Toast.LENGTH_LONG).show()
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Menambahkan Sapi!")
                progressDialog.setMessage("Silahkan tunggu, sementara kami sedang menambahkan postingan sapi anda!")
                progressDialog.show()

                val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                val postId = ref.push().key

                val fileRef =
                    storagePostPicRef!!.child("$postId.jpg")
                val uploadTask: StorageTask<*>

                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            progressDialog.dismiss()
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val postMap = HashMap<String, Any>()

                        postMap["postid"] = postId!!
                        postMap["postimage"] = myUrl
                        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["product"] = binding.etProduct.text.toString()
                        postMap["age"] = binding.etAge.text.toString()
                        postMap["weight"] = binding.etWeight.text.toString()
                        postMap["color"] = binding.etColor.text.toString()
                        postMap["gender"] = binding.etGender.text.toString()
                        postMap["desc"] = binding.etDesc.text.toString()
                        postMap["price"] = binding.etPrice.text.toString()
                        postMap["shipping"] = binding.etShipping.text.toString()
                        postMap["latitude"] = coordinateLatitude
                        postMap["longitude"] = coordinateLongitude
                        postMap["location"] = binding.fieldLocation.text.toString()
                        postMap["dateTime"] = System.currentTimeMillis().toString()

                        ref.child(postId).updateChildren(postMap)

                        Toast.makeText(this, "Sapi berhasil ditambahkan!", Toast.LENGTH_LONG)
                            .show()

                        finish()
                        progressDialog.dismiss()
                    } else {
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }
}