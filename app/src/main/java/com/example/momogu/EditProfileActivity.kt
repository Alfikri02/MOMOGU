@file:Suppress("DEPRECATION")

package com.example.momogu

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.example.momogu.model.UserModel
import com.example.momogu.databinding.ActivityEditProfileBinding
import com.example.momogu.utils.Constanta.LocationPicker
import com.example.momogu.utils.Constanta.LOCATION_PERMISSION_CODE
import com.example.momogu.utils.Constanta.REQUEST_PROFILE_IMAGE
import com.example.momogu.utils.Constanta.coordinateLatitude
import com.example.momogu.utils.Constanta.coordinateLongitude
import com.example.momogu.utils.Constanta.isLocationPicked
import com.example.momogu.utils.Helper
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage


class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private lateinit var firebaseUser: FirebaseUser
    private var checkerImage: Boolean = false
    private var checkerLocation: Boolean = false
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null
    private lateinit var builder: AlertDialog.Builder

    private var getResult: ActivityResultLauncher<Intent>? = null
    private var isPicked: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Picture")

        builder = AlertDialog.Builder(this)

        getResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.let { res ->
                    isPicked = res.getBooleanExtra(LocationPicker.IsPicked.name, false)
                    isLocationPicked.postValue(isPicked)
                    val lat = res.getDoubleExtra(
                        LocationPicker.Latitude.name,
                        0.0
                    )
                    val lon = res.getDoubleExtra(
                        LocationPicker.Longitude.name,
                        0.0
                    )
                    binding.fieldLocation.text = Helper.parseAddressLocation(this, lat, lon)
                    binding.fieldCity.text = Helper.parseCityLocation(this, lat, lon)
                    coordinateLatitude = lat
                    coordinateLongitude = lon
                }
            }
        }

        binding.logoutBtn.setOnClickListener {
            builder.setTitle("Peringatan!")
                .setMessage("Apakah anda ingin keluar dari akun ini?")
                .setCancelable(true)
                .setPositiveButton("Iya") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, SignInActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }.setNegativeButton("Tidak") { dialogInterface, _ ->
                    dialogInterface.cancel()
                }.show()
        }

        binding.changeImageTextBtn.setOnClickListener {
            checkerImage = true
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_PROFILE_IMAGE)
        }

        binding.profileImageViewProfileFrag.setOnClickListener {
            checkerImage = true

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_PROFILE_IMAGE)
        }



        binding.saveProfileBtn.setOnClickListener {
            if (checkerImage && checkerLocation) {
                updateImageAndLocation()
            } else if (checkerImage){
                updateImage()
            } else if (checkerLocation) {
                updateLocation()
            } else {
                updateUser()
            }
        }

        binding.closeProfileBtn.setOnClickListener {
            finish()
        }

        userInfo()

        isLocationPicked.postValue(false)

        binding.btnClearLocation.setOnClickListener {
            isLocationPicked.postValue(false)
            binding.tiAddressProfile.visibility = View.VISIBLE
        }

        binding.btnSelectLocation.setOnClickListener {
            checkerLocation = true
            if (Helper.isPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                val intentPickLocation = Intent(this, MapAdminActivity::class.java)
                getResult?.launch(intentPickLocation)
                binding.tiAddressProfile.visibility = View.GONE
            } else {
                ActivityCompat.requestPermissions(
                    this@EditProfileActivity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_CODE
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

        if (requestCode == REQUEST_PROFILE_IMAGE) {
            val uri = data?.data
            CropImage.activity(uri)
                .setAspectRatio(1, 1)
                .start(this)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            binding.profileImageViewProfileFrag.setImageURI(imageUri)
        }
    }

    private fun userInfo() {
        val usersRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UserModel::class.java)

                    if (user!!.getImage().isNullOrEmpty()) {
                        binding.profileImageViewProfileFrag.setImageResource(R.drawable.profile)
                    } else {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile)
                            .into(binding.profileImageViewProfileFrag)
                    }
                    binding.etFullnameProfile.setText(user.getFullname())
                    binding.etPhoneProfile.setText(user.getPhone())
                    binding.etAddressProfile.setText(user.getAddress())
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun updateUser() {
        when {
            TextUtils.isEmpty(binding.etFullnameProfile.text.toString()) -> {
                Toast.makeText(this, "Nama Lengkap Dibutuhkan!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etPhoneProfile.text.toString()) -> {
                Toast.makeText(this, "Nomor Telepon Dibutuhkan!", Toast.LENGTH_LONG).show()
            }

            else -> {
                val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
                val userMap = HashMap<String, Any>()

                userMap["fullname"] = binding.etFullnameProfile.text.toString()
                userMap["phone"] = binding.etPhoneProfile.text.toString()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(
                    this,
                    "Informasi akun berhasil diperbarui!",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun updateImageAndLocation() {
        when {
            imageUri == null -> {
                Toast.makeText(this, "Silahkan pilih foto profil anda!", Toast.LENGTH_LONG)
                    .show()
            }

            TextUtils.isEmpty(binding.fieldLocation.text) -> {
                Toast.makeText(this, "Alamat Lengkap Dibutuhkan!", Toast.LENGTH_LONG).show()
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Ubah Profil")
                progressDialog.setMessage("Harap tunggu, akun sedang diperbarui...")
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child(firebaseUser.uid + ".jpg")
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

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()

                        userMap["image"] = myUrl
                        userMap["city"] = binding.fieldCity.text.toString()
                        userMap["address"] = binding.fieldLocation.text.toString()
                        userMap["latitude"] = coordinateLatitude
                        userMap["longitude"] = coordinateLongitude

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(
                            this,
                            "Akun berhasil diperbarui!",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                        progressDialog.dismiss()
                    } else {
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun updateLocation() {
        when (binding.fieldLocation.text) {
            null -> {
                Toast.makeText(this, "Alamat lengkap dibutuhkan!", Toast.LENGTH_LONG).show()
            }

            else -> {
                val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
                val userMap = HashMap<String, Any>()

                userMap["city"] = binding.fieldCity.text.toString()
                userMap["address"] = binding.fieldLocation.text.toString()
                userMap["latitude"] = coordinateLatitude
                userMap["longitude"] = coordinateLongitude

                usersRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(
                    this,
                    "Alamat berhasil diperbarui!",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun updateImage() {
        when (imageUri) {
            null -> {
                Toast.makeText(this, "Silahkan pilih foto profil anda!", Toast.LENGTH_LONG)
                    .show()
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Ubah Profil")
                progressDialog.setMessage("Harap tunggu, Foto profil sedang diperbarui...")
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child(firebaseUser.uid + ".jpg")
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

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()

                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(
                            this,
                            "Foto profil berhasil diperbarui!",
                            Toast.LENGTH_LONG
                        ).show()
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