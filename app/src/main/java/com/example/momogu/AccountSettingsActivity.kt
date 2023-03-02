package com.example.momogu

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.momogu.Model.UserModel
import com.example.momogu.databinding.ActivityAccountSettingsBinding
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

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountSettingsBinding

    private lateinit var firebaseUser: FirebaseUser
    private var checker: Boolean = false
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Picture")

        binding.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding.changeImageTextBtn.setOnClickListener {
            checker = true
            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this)
        }

        binding.profileImageViewProfileFrag.setOnClickListener {
            checker = true

            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this)
        }

        binding.saveProfileBtn.setOnClickListener {
            if (checker) {
                uploadImageAndUpdateInfo()
            } else {
                updateUserInfoOnly()
            }
        }

        binding.closeProfileBtn.setOnClickListener {
            finish()
        }

        userInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
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

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(binding.profileImageViewProfileFrag)
                    binding.etUsernameProfile.setText(user.getUsername())
                    binding.etFullnameProfile.setText(user.getFullname())
                    binding.etWhatsappProfile.setText(user.getWa())
                    binding.etCityProfile.setText(user.getCity())
                    binding.etAddressProfile.setText(user.getAddress())
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun updateUserInfoOnly() {
        when {
            TextUtils.isEmpty(binding.etFullnameProfile.text.toString()) -> {
                Toast.makeText(this, "Full Name is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etUsernameProfile.text.toString()) -> {
                Toast.makeText(this, "Username is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etWhatsappProfile.text.toString()) -> {
                Toast.makeText(this, "Whatsapp Number is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etCityProfile.text.toString()) -> {
                Toast.makeText(this, "City is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etAddressProfile.text.toString()) -> {
                Toast.makeText(this, "Full Address is Required!", Toast.LENGTH_LONG).show()
            }

            else -> {
                val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
                val userMap = HashMap<String, Any>()

                userMap["fullname"] = binding.etFullnameProfile.text.toString()
                userMap["username"] = binding.etUsernameProfile.text.toString().lowercase()
                userMap["wa"] = binding.etWhatsappProfile.text.toString()
                userMap["city"] = binding.etCityProfile.text.toString()
                userMap["address"] = binding.etAddressProfile.text.toString()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(
                    this,
                    "Account Information has been updated successfully.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun uploadImageAndUpdateInfo() {
        when {
            imageUri == null -> {
                Toast.makeText(this, "Please select your profile picture.", Toast.LENGTH_LONG)
                    .show()
            }

            TextUtils.isEmpty(binding.etFullnameProfile.text.toString()) -> {
                Toast.makeText(this, "Full Name is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etUsernameProfile.text.toString()) -> {
                Toast.makeText(this, "Username is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etWhatsappProfile.text.toString()) -> {
                Toast.makeText(this, "Whatsapp Number is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etCityProfile.text.toString()) -> {
                Toast.makeText(this, "City is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etAddressProfile.text.toString()) -> {
                Toast.makeText(this, "Full Address is Required!", Toast.LENGTH_LONG).show()
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, while we are updating your profile...")
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

                        userMap["fullname"] = binding.etFullnameProfile.text.toString()
                        userMap["username"] = binding.etUsernameProfile.text.toString().lowercase()
                        userMap["wa"] = binding.etWhatsappProfile.text.toString()
                        userMap["city"] = binding.etCityProfile.text.toString()
                        userMap["address"] = binding.etAddressProfile.text.toString()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(
                            this,
                            "Account Information has been updated successfully.",
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