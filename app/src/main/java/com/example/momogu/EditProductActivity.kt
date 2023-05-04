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

        binding.layoutImage.setOnClickListener {
            checker = true

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_POST_EDIT_IMAGE)
        }

        binding.saveNewPostBtn.setOnClickListener {
            if (checker) {
                uploadImageAndUpdateInfo()
            } else {
                updateProductInfoOnly()
            }
        }

        binding.closeAddPostBtn.setOnClickListener {
            finish()
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
            binding.imagePost.setImageURI(imageUri)
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

                    Picasso.get().load(post!!.getPostimage()).into(binding.imagePost)

                    binding.etProduct.setText(post.getProduct())
                    binding.etAge.setText(post.getAge())
                    binding.etWeight.setText(post.getWeight())
                    binding.etColor.setText(post.getColor())
                    binding.etGender.setText(post.getGender())
                    binding.etDesc.setText(post.getDesc())
                    binding.etPrice.setText(post.getPrice()!!)
                    binding.etShipping.setText(post.getShipping()!!)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun updateProductInfoOnly() {
        when {
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

            else -> {
                val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
                val postMap = HashMap<String, Any>()

                postMap["product"] = binding.etProduct.text.toString()
                postMap["age"] = binding.etAge.text.toString()
                postMap["weight"] = binding.etWeight.text.toString()
                postMap["color"] = binding.etColor.text.toString()
                postMap["gender"] = binding.etGender.text.toString()
                postMap["desc"] = binding.etDesc.text.toString()
                postMap["price"] = binding.etPrice.text.toString()
                postMap["shipping"] = binding.etShipping.text.toString()

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

                        postMap["product"] = binding.etProduct.text.toString()
                        postMap["age"] = binding.etAge.text.toString()
                        postMap["weight"] = binding.etWeight.text.toString()
                        postMap["color"] = binding.etColor.text.toString()
                        postMap["gender"] = binding.etGender.text.toString()
                        postMap["desc"] = binding.etDesc.text.toString()
                        postMap["price"] = binding.etPrice.text.toString()
                        postMap["shipping"] = binding.etShipping.text.toString()
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