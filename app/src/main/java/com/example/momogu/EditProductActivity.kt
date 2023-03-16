@file:Suppress("DEPRECATION")

package com.example.momogu

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.momogu.Model.PostModel
import com.example.momogu.databinding.ActivityEditProductBinding
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


class EditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProductBinding

    private var postId: String = ""
    private lateinit var firebaseUser: FirebaseUser
    private var checker: Boolean = false
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Post Pictures")

        val preferences = this.getSharedPreferences("POST", Context.MODE_PRIVATE)
        if (preferences != null) {
            postId = preferences.getString("postid", "none")!!
        }

        binding.imageEditProduct.setOnClickListener {
            CropImage.activity()
                .setAspectRatio(4, 3)
                .start(this)
        }

        binding.saveEditProduct.setOnClickListener {
            if (checker) {
                uploadImageAndUpdateInfo()
            } else {
                updateProductInfoOnly()
            }
        }

        binding.closeEditProduct.setOnClickListener {
            finish()
        }

        productInfo()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            binding.imageEditProduct.setImageURI(imageUri)
        }
    }

    private fun productInfo() {
        val postRef =
            FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(PostModel::class.java)

                    Picasso.get().load(post!!.getPostimage()).placeholder(R.drawable.profile)
                        .into(binding.imageEditProduct)
                    binding.etProductEdit.setText(post.getProduct())
                    binding.etAgeEdit.setText(post.getAge())
                    binding.etWeightEdit.setText(post.getWeight())
                    binding.etColorEdit.setText(post.getColor())
                    binding.etGenderEdit.setText(post.getGender())
                    binding.etDescEdit.setText(post.getDesc())
                    binding.etPriceEdit.setText(post.getPrice())
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun updateProductInfoOnly() {
        when {
            TextUtils.isEmpty(binding.etProductEdit.text.toString()) -> {
                Toast.makeText(this, "Product is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etAgeEdit.text.toString()) -> {
                Toast.makeText(this, "Age is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etWeightEdit.text.toString()) -> {
                Toast.makeText(this, "Weight is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etColorEdit.text.toString()) -> {
                Toast.makeText(this, "Color is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etGenderEdit.text.toString()) -> {
                Toast.makeText(this, "Gender is Required!", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(binding.etDescEdit.text.toString()) -> {
                Toast.makeText(this, "Description is Required!", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(binding.etPriceEdit.text.toString()) -> {
                Toast.makeText(this, "Price is Required!", Toast.LENGTH_LONG).show()
            }

            else -> {
                val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
                val postMap = HashMap<String, Any>()

                postMap["product"] = binding.etProductEdit.text.toString()
                postMap["age"] = binding.etAgeEdit.text.toString()
                postMap["weight"] = binding.etWeightEdit.text.toString()
                postMap["color"] = binding.etColorEdit.text.toString()
                postMap["gender"] = binding.etGenderEdit.text.toString()
                postMap["desc"] = binding.etDescEdit.text.toString()
                postMap["price"] = binding.etPriceEdit.text.toString()

                postRef.child(postId).updateChildren(postMap)

                Toast.makeText(
                    this,
                    "Data Berhasil Dirubah",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun uploadImageAndUpdateInfo() {
        when {
            imageUri == null -> {
                Toast.makeText(this, "Please select your profile picture.", Toast.LENGTH_LONG)
                    .show()
            }

            TextUtils.isEmpty(binding.etProductEdit.text.toString()) -> {
                Toast.makeText(this, "Product is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etAgeEdit.text.toString()) -> {
                Toast.makeText(this, "Age is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etWeightEdit.text.toString()) -> {
                Toast.makeText(this, "Weight is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etColorEdit.text.toString()) -> {
                Toast.makeText(this, "Color is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.etGenderEdit.text.toString()) -> {
                Toast.makeText(this, "Gender is Required!", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(binding.etDescEdit.text.toString()) -> {
                Toast.makeText(this, "Description is Required!", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(binding.etPriceEdit.text.toString()) -> {
                Toast.makeText(this, "Price is Required!", Toast.LENGTH_LONG).show()
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Ubah Data")
                progressDialog.setMessage("Harap Tunggu, Kami Sedang Memperbarui Data Anda...")
                progressDialog.show()

                val ref = FirebaseDatabase.getInstance().reference.child("Posts")

                val fileRef = storagePostPicRef!!.child("$postId.jpg")
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

                        postMap["product"] = binding.etProductEdit.text.toString()
                        postMap["age"] = binding.etAgeEdit.text.toString()
                        postMap["weight"] = binding.etWeightEdit.text.toString()
                        postMap["color"] = binding.etColorEdit.text.toString()
                        postMap["gender"] = binding.etGenderEdit.text.toString()
                        postMap["desc"] = binding.etDescEdit.text.toString()
                        postMap["price"] = binding.etPriceEdit.text.toString()
                        postMap["postimage"] = myUrl

                        ref.child(postId).updateChildren(postMap)

                        Toast.makeText(
                            this,
                            "Data Berhasil Dirubah",
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