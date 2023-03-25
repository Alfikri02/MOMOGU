@file:Suppress("DEPRECATION")

package com.example.momogu

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import java.text.DecimalFormat


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

        binding.etPriceEdit.addTextChangedListener(object : TextWatcher {
            val decimalFormat = DecimalFormat("#,##0")

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etPriceEdit.removeTextChangedListener(this)
                try {
                    val value = s.toString().replace(".", "").replace(",", "")
                    val formatted = decimalFormat.format(value.toDouble())
                    binding.etPriceEdit.setText(formatted)
                    binding.etPriceEdit.setSelection(formatted.length)
                } catch (_: NumberFormatException) {
                }
                binding.etPriceEdit.addTextChangedListener(this)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etShippingEdit.addTextChangedListener(object : TextWatcher {
            val decimalFormat = DecimalFormat("#,##0")

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etShippingEdit.removeTextChangedListener(this)
                try {
                    val value = s.toString().replace(".", "").replace(",", "")
                    val formatted = decimalFormat.format(value.toDouble())
                    binding.etShippingEdit.setText(formatted)
                    binding.etShippingEdit.setSelection(formatted.length)
                } catch (_: NumberFormatException) {
                }
                binding.etShippingEdit.addTextChangedListener(this)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

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
                    binding.etPriceEdit.setText(post.getPrice()!!)
                    binding.etShippingEdit.setText(post.getShipping()!!)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun updateProductInfoOnly() {
        when {
            binding.etProductEdit.text.isNullOrEmpty() -> {
                binding.etProductEdit.error = "Nama sapi dibutuhkan!"
            }

            binding.etAgeEdit.text.isNullOrEmpty() -> {
                binding.etAgeEdit.error = "Umur sapi dibutuhkan!"
            }

            binding.etWeightEdit.text.isNullOrEmpty() -> {
                binding.etWeightEdit.error = "Bobot sapi dibutuhkan!"
            }

            binding.etColorEdit.text.isNullOrEmpty() -> {
                binding.etColorEdit.error = "Warna sapi dibutuhkan!"
            }

            binding.etGenderEdit.text.isNullOrEmpty() -> {
                binding.etGenderEdit.error = "Jenis kelamin sapi dibutuhkan!"
            }

            binding.etDescEdit.text.isNullOrEmpty() -> {
                binding.etDescEdit.error = "Deskripsi sapi dibutuhkan!"
            }

            binding.etPriceEdit.text.isNullOrEmpty() -> {
                binding.etPriceEdit.error = "Harga sapi dibutuhkan!"
            }

            binding.etShippingEdit.text.isNullOrEmpty() -> {
                binding.etShippingEdit.error = "Ongkos pengiriman sapi dibutuhkan!"
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
                Toast.makeText(this, "Silahkan pilih foto sapi anda!", Toast.LENGTH_LONG)
                    .show()
            }

            binding.etProductEdit.text.isNullOrEmpty() -> {
                binding.etProductEdit.error = "Nama sapi dibutuhkan!"
            }

            binding.etAgeEdit.text.isNullOrEmpty() -> {
                binding.etAgeEdit.error = "Umur sapi dibutuhkan!"
            }

            binding.etWeightEdit.text.isNullOrEmpty() -> {
                binding.etWeightEdit.error = "Bobot sapi dibutuhkan!"
            }

            binding.etColorEdit.text.isNullOrEmpty() -> {
                binding.etColorEdit.error = "Warna sapi dibutuhkan!"
            }

            binding.etGenderEdit.text.isNullOrEmpty() -> {
                binding.etGenderEdit.error = "Jenis kelamin sapi dibutuhkan!"
            }

            binding.etDescEdit.text.isNullOrEmpty() -> {
                binding.etDescEdit.error = "Deskripsi sapi dibutuhkan!"
            }

            binding.etPriceEdit.text.isNullOrEmpty() -> {
                binding.etPriceEdit.error = "Harga sapi dibutuhkan!"
            }

            binding.etShippingEdit.text.isNullOrEmpty() -> {
                binding.etShippingEdit.error = "Ongkos pengiriman sapi dibutuhkan!"
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Mengubah data sapi!")
                progressDialog.setMessage("Harap tunggu, kami sedang memperbarui data sapi anda...")
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
                        postMap["shipping"] = binding.etShippingEdit.text.toString()
                        postMap["postimage"] = myUrl

                        ref.child(postId).updateChildren(postMap)

                        Toast.makeText(
                            this,
                            "Data sapi berhasil diperbarui!",
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