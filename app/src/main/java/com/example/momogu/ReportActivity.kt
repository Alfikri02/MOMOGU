@file:Suppress("DEPRECATION")

package com.example.momogu

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.momogu.Model.ReceiptModel
import com.example.momogu.databinding.ActivityReportBinding
import com.example.momogu.utils.Constanta
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private var postId: String = ""
    private var storageReport: StorageReference? = null
    private var myUrl = ""
    private var myVideoUrl = ""
    private var imageUri: Uri? = null
    private var videoUri: Uri? = null

    @SuppressLint("IntentReset", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        storageReport = FirebaseStorage.getInstance().reference.child("Report")

        val preferences = this.getSharedPreferences("POST", Context.MODE_PRIVATE)
        if (preferences != null) {
            postId = preferences.getString("postid", "none")!!
        }

        dropdownItem()

        binding.closeReport.setOnClickListener {
            finish()
        }

        binding.layoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, Constanta.REQUEST_REPORT_IMAGE)
        }

        binding.btnClearImage.setOnClickListener {
            binding.imagePost.setImageURI(null)
            binding.cvImage.visibility = View.GONE
            binding.btnClearImage.visibility = View.GONE
        }

        binding.layoutVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.type = "video/*"
            startActivityForResult(intent, Constanta.REQUEST_REPORT_VIDEO)
        }

        binding.btnClearVideo.setOnClickListener {
            binding.tvMaxVideo.visibility = View.VISIBLE
            binding.tvFileVideo.visibility = View.GONE
            binding.btnClearVideo.visibility = View.GONE
        }

        binding.etKop.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem == "Sapi Tidak Sesuai Deskripsi") {
                    binding.layoutVideo.visibility = View.VISIBLE
                    binding.layoutImage.visibility = View.VISIBLE
                    binding.tvChoose.text = "Pilih Gambar Sapi Anda!"
                } else if (selectedItem == "Biaya Transaksi Tidak Sesuai") {
                    binding.layoutImage.visibility = View.VISIBLE
                    binding.layoutVideo.visibility = View.GONE
                    binding.tvChoose.text = "Pilih Foto Bukti Pembayaran!"
                } else {
                    binding.layoutVideo.visibility = View.GONE
                    binding.layoutImage.visibility = View.GONE
                }
            }

        binding.saveReport.setOnClickListener {
            val selectedItem = binding.etKop.text.toString()
            if (selectedItem == "Sapi Tidak Sesuai Deskripsi") {
                uploadVideoImage()
            } else if (selectedItem == "Biaya Transaksi Tidak Sesuai") {
                uploadImage()
            } else {
                uploadText()
            }
        }


    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constanta.REQUEST_REPORT_IMAGE) {
            val uri = data?.data
            CropImage.activity(uri)
                .setAspectRatio(4, 3)
                .start(this)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            binding.imagePost.setImageURI(imageUri)
            binding.cvImage.visibility = View.VISIBLE
            binding.btnClearImage.visibility = View.VISIBLE
        }

        if (requestCode == Constanta.REQUEST_REPORT_VIDEO && resultCode == RESULT_OK && data != null) {
            videoUri = data.data
            videoUri?.let {
                val videoName = getVideoName(videoUri!!)

                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(this, it)

                val duration =
                    mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLong()

                if (duration != null && duration > 30000) {
                    Toast.makeText(this, "Video lebih dari 30 detik!", Toast.LENGTH_LONG).show()
                } else {
                    binding.tvFileVideo.text = videoName
                    binding.tvMaxVideo.visibility = View.GONE
                    binding.tvFileVideo.visibility = View.VISIBLE
                    binding.btnClearVideo.visibility = View.VISIBLE
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
        val report = resources.getStringArray(R.array.Report)
        val arrayGender = ArrayAdapter(this, R.layout.dropdown_item, report)
        binding.etKop.setAdapter(arrayGender)
    }

    private fun uploadText() {
        when {

            binding.etKop.text.isNullOrEmpty() -> {
                binding.etKop.error = "Jenis Masalah Dibutuhkan!"
            }

            binding.etDesc.text.isNullOrEmpty() -> {
                binding.etDesc.error = "Keterangan Masalah Dibutuhkan!"
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Menambahkan Laporan!")
                progressDialog.setMessage("Silahkan tunggu, sementara kami sedang menambahkan laporan anda!")
                progressDialog.show()

                val postsRef =
                    FirebaseDatabase.getInstance().reference.child("Receipt").child(postId)
                postsRef.addValueEventListener(object : ValueEventListener {
                    @SuppressLint("SetTextI18n")
                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            val receipt = p0.getValue(ReceiptModel::class.java)

                            val ref = FirebaseDatabase.getInstance().reference.child("Report")
                            val reportMap = HashMap<String, Any>()
                            reportMap["buyerid"] = receipt!!.getBuyerId().toString()
                            reportMap["sellerid"] = receipt.getSellerId().toString()
                            reportMap["postid"] = receipt.getPostId().toString()
                            reportMap["dateTime"] = System.currentTimeMillis().toString()
                            reportMap["title"] = binding.etKop.text.toString()
                            reportMap["description"] = binding.etDesc.text.toString()
                            reportMap["image"] = ""
                            reportMap["video"] = ""
                            ref.child(postId).updateChildren(reportMap)

                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {}
                })

                Toast.makeText(this, "Laporan berhasil ditambahkan!", Toast.LENGTH_LONG)
                    .show()

                finish()
                progressDialog.dismiss()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun uploadVideoImage() {
        when {

            imageUri == null -> {
                Toast.makeText(this, "Silahkan pilih foto sapi anda!", Toast.LENGTH_LONG).show()
            }

            videoUri == null -> {
                Toast.makeText(this, "Silahkan pilih video sapi anda!", Toast.LENGTH_LONG).show()
            }

            binding.etKop.text.isNullOrEmpty() -> {
                binding.etKop.error = "Jenis Masalah Dibutuhkan!"
            }

            binding.etDesc.text.isNullOrEmpty() -> {
                binding.etDesc.error = "Keterangan Masalah Dibutuhkan!"
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Menambahkan Sapi!")
                progressDialog.setMessage("Silahkan tunggu, sementara kami sedang menambahkan postingan sapi anda!")
                progressDialog.show()

                val fileRef = storageReport!!.child("$postId.jpg")
                val fileVideoRef = storageReport!!.child("$postId.mp4")

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

                        val postsRef =
                            FirebaseDatabase.getInstance().reference.child("Receipt").child(postId)
                        postsRef.addValueEventListener(object : ValueEventListener {
                            @SuppressLint("SetTextI18n")
                            override fun onDataChange(p0: DataSnapshot) {
                                if (p0.exists()) {
                                    val receipt = p0.getValue(ReceiptModel::class.java)

                                    val ref =
                                        FirebaseDatabase.getInstance().reference.child("Report")
                                    val reportMap = HashMap<String, Any>()
                                    reportMap["buyerid"] = receipt!!.getBuyerId().toString()
                                    reportMap["sellerid"] = receipt.getSellerId().toString()
                                    reportMap["postid"] = receipt.getPostId().toString()
                                    reportMap["dateTime"] = System.currentTimeMillis().toString()
                                    reportMap["title"] = binding.etKop.text.toString()
                                    reportMap["description"] = binding.etDesc.text.toString()
                                    reportMap["image"] = myUrl
                                    reportMap["video"] = myVideoUrl
                                    ref.child(postId).updateChildren(reportMap)

                                }
                            }

                            override fun onCancelled(p0: DatabaseError) {}
                        })

                        Toast.makeText(this, "Laporan berhasil ditambahkan!", Toast.LENGTH_LONG)
                            .show()

                        finish()
                        progressDialog.dismiss()
                    }.addOnFailureListener {
                        progressDialog.dismiss()
                    }
            }
        }
    }

    private fun uploadImage() {
        when {
            imageUri == null -> {
                Toast.makeText(this, "Silahkan pilih foto sapi anda!", Toast.LENGTH_LONG).show()
            }

            binding.etKop.text.isNullOrEmpty() -> {
                binding.etKop.error = "Jenis Masalah Dibutuhkan!"
            }

            binding.etDesc.text.isNullOrEmpty() -> {
                binding.etDesc.error = "Keterangan Masalah Dibutuhkan!"
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Post")
                progressDialog.setMessage("Please wait, while we are adding your new post...")
                progressDialog.show()

                val fileRef = storageReport!!.child("$postId.jpg")

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

                        val postsRef =
                            FirebaseDatabase.getInstance().reference.child("Receipt").child(postId)
                        postsRef.addValueEventListener(object : ValueEventListener {
                            @SuppressLint("SetTextI18n")
                            override fun onDataChange(p0: DataSnapshot) {
                                if (p0.exists()) {
                                    val receipt = p0.getValue(ReceiptModel::class.java)

                                    val ref =
                                        FirebaseDatabase.getInstance().reference.child("Report")
                                    val reportMap = HashMap<String, Any>()
                                    reportMap["buyerid"] = receipt!!.getBuyerId().toString()
                                    reportMap["sellerid"] = receipt.getSellerId().toString()
                                    reportMap["postid"] = receipt.getPostId().toString()
                                    reportMap["dateTime"] = System.currentTimeMillis().toString()
                                    reportMap["title"] = binding.etKop.text.toString()
                                    reportMap["description"] = binding.etDesc.text.toString()
                                    reportMap["image"] = myUrl
                                    reportMap["video"] = ""
                                    ref.child(postId).updateChildren(reportMap)

                                }
                            }

                            override fun onCancelled(p0: DatabaseError) {}
                        })

                        Toast.makeText(this, "Laporan berhasil ditambahkan!", Toast.LENGTH_LONG)
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