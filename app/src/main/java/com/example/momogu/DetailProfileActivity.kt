@file:Suppress("DEPRECATION")

package com.example.momogu

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.ReceiptModel
import com.example.momogu.databinding.ActivityDetailProfileBinding
import com.example.momogu.utils.Constanta.productLatitude
import com.example.momogu.utils.Constanta.productLongitude
import com.example.momogu.utils.Helper.getDate
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.*

class DetailProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailProfileBinding
    private var storagePostPicRef: StorageReference? = null
    private var storagePostVideoRef: StorageReference? = null
    private var postId: String = ""
    private lateinit var builder: AlertDialog.Builder

    private lateinit var locationManager: LocationManager

    @SuppressLint("DiscouragedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Post Pictures")
        storagePostVideoRef = FirebaseStorage.getInstance().reference.child("Post Video")

        val preferences = this.getSharedPreferences("POST", Context.MODE_PRIVATE)
        if (preferences != null) {
            postId = preferences.getString("postid", "none")!!
        }

        builder = AlertDialog.Builder(this)

        retrievePosts()
        soldVal()

        binding.closeDetail.setOnClickListener {
            finish()
        }

        binding.cvSeeVideo.setOnClickListener {
            retrieveVideo()
        }

        binding.cvImageDetail.setOnClickListener {
            retrieveImage()
        }

        binding.deleteDetail.setOnClickListener {
            valDelete()
        }

        val checkPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    if (checkGPS()) {
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return@registerForActivityResult
                        }
                        val i = Intent(this, MapUserActivity::class.java)
                        i.putExtra("productLatitude", productLatitude)
                        i.putExtra("productLongitude", productLongitude)
                        startActivity(i)
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_permission_location_detail),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        binding.cvSeeLocation.setOnClickListener {
            checkPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    }

    private fun checkGPS(): Boolean {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true
        } else {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle(getString(R.string.toast_gps_detail))
            dialog.setMessage(getString(R.string.toast_gps_on_detail))
            dialog.setCancelable(true)
            dialog.setIcon(R.drawable.ic_location_off)
            dialog.setPositiveButton("Iya") { d, _ ->
                d.dismiss()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            dialog.setNegativeButton("Tidak") { d, _ ->
                d.dismiss()
            }
            dialog.show()
        }
        return false
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(PostModel::class.java)

                    Picasso.get().load(post!!.getPostimage()).into(binding.imageDetail)

                    binding.tvProductDetail.text = post.getProduct()
                    binding.tvPriceDetail.text = "Rp. ${post.getPrice()}"
                    binding.tvPriceShippingDetail.text = "Rp. ${post.getShipping()}"
                    binding.tvDateDetail.text = getDate(post.getDateTime()!!.toLong(), "dd MMM yyyy")
                    binding.etWeightDetail.text = "${post.getWeight()} KG"
                    binding.etGenderDetail.text = post.getGender()
                    binding.etAgeDetail.text = "${post.getAge()} Bulan"
                    binding.etColorDetail.text = post.getColor()
                    binding.etDescDetail.setText(post.getDesc())
                    productLatitude = post.getLatitude()!!
                    productLongitude = post.getLongitude()!!
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun retrieveVideo() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(PostModel::class.java)

                    val mBuilder = AlertDialog.Builder(this@DetailProfileActivity)
                    val mView = layoutInflater.inflate(R.layout.dialog_layout_video, null)

                    val videoView = mView.findViewById<PlayerView>(R.id.player_view)
                    val player = ExoPlayer.Builder(this@DetailProfileActivity).build()
                    videoView.player = player
                    val mediaItem = MediaItem.fromUri(post!!.getPostvideo()!!)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.playWhenReady = false

                    mBuilder.setView(mView)
                    val mDialog = mBuilder.create()

                    mDialog.setOnDismissListener {
                        player.stop()
                    }
                    mDialog.show()

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun retrieveImage() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(PostModel::class.java)

                    val mBuilder = AlertDialog.Builder(this@DetailProfileActivity)
                    val mView = layoutInflater.inflate(R.layout.dialog_layout_image, null)

                    val imageView = mView.findViewById<PhotoView>(R.id.imageView)
                    Picasso.get().load(post!!.getPostimage()).into(imageView)

                    mBuilder.setView(mView)
                    val mDialog = mBuilder.create()
                    mDialog.show()

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun valDelete() {
        val receiptRef = FirebaseDatabase.getInstance().reference.child("Receipt").child(postId)
        receiptRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val receipt = dataSnapshot.getValue(ReceiptModel::class.java)

                    if (receipt!!.getStatus().equals("Selesai")) {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.toast_sell_detail),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.toast_was_taken_detail),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    builder.setTitle(getString(R.string.toast_warning_detail))
                        .setMessage(getString(R.string.toast_delete_detail))
                        .setCancelable(true)
                        .setPositiveButton("Iya") { _, _ ->
                            val postRef =
                                FirebaseDatabase.getInstance().getReference("Posts")
                                    .child(postId)
                            postRef.removeValue()
                            val imageRef = storagePostPicRef!!.child("$postId.jpg")
                            imageRef.delete()

                            val videoRef = storagePostVideoRef!!.child("$postId.mp4")
                            videoRef.delete()

                            Toast.makeText(
                                this@DetailProfileActivity,
                                getString(R.string.toast_delete_succeed_detail),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            finish()
                        }.setNegativeButton("Tidak") { dialogInterface, _ ->
                            dialogInterface.cancel()
                        }.show()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun soldVal() {
        val receiptRef = FirebaseDatabase.getInstance().reference.child("Receipt").child(postId)
        receiptRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val receipt = p0.getValue(ReceiptModel::class.java)

                    if (receipt!!.getStatus().equals("Selesai")) {
                        binding.layoutSoldView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

}