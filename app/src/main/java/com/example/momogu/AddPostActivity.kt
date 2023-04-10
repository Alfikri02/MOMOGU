package com.example.momogu

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.PermissionChecker
import com.example.momogu.databinding.ActivityAddPostBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import java.text.DecimalFormat

class AddPostActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPicRef: StorageReference? = null
    private val LOCATION_CODE = 100
    private lateinit var locationManager: LocationManager
    var latitude = 0.0
    var longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Post Pictures")

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

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

        binding.btnLocation.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 23) {
                if (PermissionChecker.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PermissionChecker.PERMISSION_DENIED
                ) {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_CODE
                    )
                } else if (PermissionChecker.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    if (checkGPS()) {
                        val locationClient = LocationServices.getFusedLocationProviderClient(this)
                        locationClient.lastLocation
                            .addOnSuccessListener { location ->
                                if (location != null) {
                                    latitude = location.latitude
                                    longitude = location.longitude
                                    val i = Intent(this, MapAdminActivity::class.java)
                                    i.putExtra("latitude", latitude)
                                    i.putExtra("longitude", longitude)
                                    startActivity(i)
                                } else {
                                    Snackbar.make(
                                        it,
                                        "Check is GPS enabled !",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error wile get location", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                }
            } else {
                checkGPS()
            }
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

    companion object {
        private const val REQUEST_POST_IMAGE = 100
        lateinit var binding: ActivityAddPostBinding
        var latitudeProduct = 0.0
        var longitudeProduct = 0.0
        fun setProductLocation(latitude: Double, longitude: Double) {
            latitudeProduct = latitude
            longitudeProduct = longitude
            binding.savedLocation.visibility = View.VISIBLE
        }
    }

    private fun checkGPS(): Boolean {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true
        } else {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("GPS isn't enabled !")
            dialog.setMessage("Enable it to be able to locate the product")
            dialog.setCancelable(true)
            dialog.setIcon(R.drawable.ic_location_off)
            dialog.setPositiveButton("OK") { d, _ ->
                d.dismiss()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            dialog.setNegativeButton("NO") { d, _ ->
                d.dismiss()
            }
            dialog.show()
        }
        return false
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

            binding.savedLocation.text.isNullOrEmpty() -> {
                Toast.makeText(this, "Silahkan pilih lokasi sapi anda!", Toast.LENGTH_LONG).show()
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
                        postMap["latitude"] = latitudeProduct
                        postMap["longitude"] = longitudeProduct
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