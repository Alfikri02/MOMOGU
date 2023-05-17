@file:Suppress("DEPRECATION")

package com.example.momogu

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.example.momogu.Model.PostModel
import com.example.momogu.databinding.ActivityEditProductBinding
import com.example.momogu.utils.Helper.decimalFormatET
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

        binding.layoutImageEdit.setOnClickListener {
            checker = true

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_POST_EDIT_IMAGE)
        }

        binding.saveEdit.setOnClickListener {
            if (checker) {
                uploadImageAndUpdateInfo()
            } else {
                updateProductInfoOnly()
            }
        }

        binding.closeEdit.setOnClickListener {
            finish()
        }

        decimalFormatET(binding.etPriceEdit)
        decimalFormatET(binding.etShippingEdit)

        productInfo()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_POST_EDIT_IMAGE) {
            val uri = data?.data
            CropImage.activity(uri)
                .setAspectRatio(4, 3)
                .start(this)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            binding.imageEdit.setImageURI(imageUri)
        }
    }

    companion object {
        private const val REQUEST_POST_EDIT_IMAGE = 100
    }

    private fun productInfo() {
        val postRef =
            FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(PostModel::class.java)

                    Picasso.get().load(post!!.getPostimage()).into(binding.imageEdit)

                    binding.etTypeEdit.setText(post.getProduct())
                    binding.etAgeEdit.setText(post.getAge())
                    binding.etWeightEdit.setText(post.getWeight())
                    binding.etColorEdit.setText(post.getColor())
                    binding.etGenderEdit.setText(post.getGender())
                    binding.etDescEdit.setText(post.getDesc())
                    binding.etPriceEdit.setText(post.getPrice()!!)
                    binding.etShippingEdit.setText(post.getShipping()!!)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun updateProductInfoOnly() {
        when {
            binding.etTypeEdit.text.isNullOrEmpty() -> {
                binding.etTypeEdit.error = getString(R.string.toast_type_add)
            }

            binding.etAgeEdit.text.isNullOrEmpty() -> {
                binding.etAgeEdit.error = getString(R.string.toast_age_add)
            }

            binding.etWeightEdit.text.isNullOrEmpty() -> {
                binding.etWeightEdit.error = getString(R.string.toast_weight_add)
            }

            binding.etColorEdit.text.isNullOrEmpty() -> {
                binding.etColorEdit.error = getString(R.string.toast_color_add)
            }

            binding.etGenderEdit.text.isNullOrEmpty() -> {
                binding.etGenderEdit.error = getString(R.string.toast_gender_add)
            }

            binding.etDescEdit.text.isNullOrEmpty() -> {
                binding.etDescEdit.error = getString(R.string.toast_desc_add)
            }

            binding.etPriceEdit.text.isNullOrEmpty() -> {
                binding.etPriceEdit.error = getString(R.string.toast_price_add)
            }

            binding.etShippingEdit.text.isNullOrEmpty() -> {
                binding.etShippingEdit.error = getString(R.string.toast_shipping_add)
            }

            binding.etShippingEdit.text.isNullOrEmpty() -> {
                Toast.makeText(this, getString(R.string.toast_location_add), Toast.LENGTH_LONG)
                    .show()
            }

            else -> {
                val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
                val postMap = HashMap<String, Any>()

                postMap["product"] = binding.etTypeEdit.text.toString()
                postMap["age"] = binding.etAgeEdit.text.toString()
                postMap["weight"] = binding.etWeightEdit.text.toString()
                postMap["color"] = binding.etColorEdit.text.toString()
                postMap["gender"] = binding.etGenderEdit.text.toString()
                postMap["desc"] = binding.etDescEdit.text.toString()
                postMap["price"] = binding.etPriceEdit.text.toString()
                postMap["shipping"] = binding.etShippingEdit.text.toString()

                postRef.child(postId).updateChildren(postMap)

                Toast.makeText(
                    this,
                    "Data sapi berhasil dirubah!",
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
                Toast.makeText(this, getString(R.string.toast_image_add), Toast.LENGTH_LONG).show()
            }

            binding.etTypeEdit.text.isNullOrEmpty() -> {
                binding.etTypeEdit.error = getString(R.string.toast_type_add)
            }

            binding.etAgeEdit.text.isNullOrEmpty() -> {
                binding.etAgeEdit.error = getString(R.string.toast_age_add)
            }

            binding.etWeightEdit.text.isNullOrEmpty() -> {
                binding.etWeightEdit.error = getString(R.string.toast_weight_add)
            }

            binding.etColorEdit.text.isNullOrEmpty() -> {
                binding.etColorEdit.error = getString(R.string.toast_color_add)
            }

            binding.etGenderEdit.text.isNullOrEmpty() -> {
                binding.etGenderEdit.error = getString(R.string.toast_gender_add)
            }

            binding.etDescEdit.text.isNullOrEmpty() -> {
                binding.etDescEdit.error = getString(R.string.toast_desc_add)
            }

            binding.etPriceEdit.text.isNullOrEmpty() -> {
                binding.etPriceEdit.error = getString(R.string.toast_price_add)
            }

            binding.etShippingEdit.text.isNullOrEmpty() -> {
                binding.etShippingEdit.error = getString(R.string.toast_shipping_add)
            }

            binding.etShippingEdit.text.isNullOrEmpty() -> {
                Toast.makeText(this, getString(R.string.toast_location_add), Toast.LENGTH_LONG)
                    .show()
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle(getString(R.string.toast_add_cow))
                progressDialog.setMessage(getString(R.string.toast_wait_add))
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

                        postMap["product"] = binding.etTypeEdit.text.toString()
                        postMap["age"] = binding.etAgeEdit.text.toString()
                        postMap["weight"] = binding.etWeightEdit.text.toString()
                        postMap["color"] = binding.etColorEdit.text.toString()
                        postMap["gender"] = binding.etGenderEdit.text.toString()
                        postMap["desc"] = binding.etDescEdit.text.toString()
                        postMap["price"] = binding.etPriceEdit.text.toString()
                        postMap["shipping"] = binding.etShippingEdit.text.toString()
                        postMap["postimage"] = myUrl

                        ref.child(postId).updateChildren(postMap)

                        Toast.makeText(this, getString(R.string.toast_cow_add), Toast.LENGTH_LONG)
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