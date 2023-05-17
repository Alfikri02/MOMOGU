@file:Suppress("DEPRECATION")

package com.example.momogu

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.momogu.Fragments.ProfileFragment
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.ReceiptModel
import com.example.momogu.Model.UserModel
import com.example.momogu.databinding.ActivityFavoriteBinding
import com.example.momogu.utils.Constanta.productLatitude
import com.example.momogu.utils.Constanta.productLongitude
import com.example.momogu.utils.Helper.getDate
import com.github.chrisbanes.photoview.PhotoView
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.*

class FavoriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteBinding
    private var postId: String = ""
    private lateinit var builder: AlertDialog.Builder
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        builder = AlertDialog.Builder(this)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val preferences = this.getSharedPreferences("POST", Context.MODE_PRIVATE)
        if (preferences != null) {
            postId = preferences.getString("postid", "none")!!
        }

        retrievePosts()
        soldVal()

        binding.closeDetail.setOnClickListener {
            finish()
        }

        binding.cvProfileDetail.setOnClickListener {
            startActivity(Intent(this, BreederActivity::class.java))
        }

        binding.btnBuyDetail.setOnClickListener {
            transVal()
        }

        binding.btnCallDetail.setOnClickListener {
            phonePost()
        }

        binding.cvSeeVideo.setOnClickListener {
            retrieveVideo()
        }

        binding.cvImageDetail.setOnClickListener {
            retrieveImage()
        }

        binding.removeFav.setOnClickListener {
            removeFav()
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
            dialog.setPositiveButton("OK") { d, _ ->
                d.dismiss()
                startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS))
            }
            dialog.setNegativeButton("NO") { d, _ ->
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
                    binding.tvProductDetail.text = "Rp. ${post.getShipping()}"
                    binding.tvDateDetail.text = getDate(post.getDateTime()!!.toLong(), "dd MMM yyyy")
                    binding.etWeightDetail.text = "${post.getWeight()} KG"
                    binding.etGenderDetail.text = post.getGender()
                    binding.etAgeDetail.text = "${post.getAge()} Bulan"
                    binding.etColorDetail.text = post.getColor()
                    binding.etDescDetail.setText(post.getDesc())
                    productLatitude = post.getLatitude()!!
                    productLongitude = post.getLongitude()!!
                    publisherInfo(post.getPublisher())
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun publisherInfo(publisherId: String?) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId!!)

        usersRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UserModel::class.java)

                    if (user!!.getImage().isNullOrEmpty()) {
                        binding.userProfileDetail.setImageResource(R.drawable.profile)
                    } else {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile)
                            .into(binding.userProfileDetail)
                    }

                    if (user.getStatusOn().equals("Aktif")) {
                        binding.tvStatusDetail.text = user.getStatusOn()
                        binding.dotStatusDetail.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                    } else {
                        val messages = TimeAgoMessages.Builder()
                            .withLocale(Locale("in")) // Set Indonesian locale
                            .build()
                        binding.tvStatusDetail.text =
                            "Aktif ${TimeAgo.using(user.getLastOnline()!!.toLong(), messages)}"
                        binding.dotStatusDetail.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                    }

                    binding.tvNameDetail.text = user.getFullname()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun removeFav() {
        builder.setTitle(getString(R.string.toast_warning_detail))
            .setMessage(getString(R.string.toast_remove_fav_detail))
            .setCancelable(true)
            .setPositiveButton("Iya") { _, _ ->
                binding.removeFav.setImageResource(R.drawable.ic_favorite_broder)

                val postRef =
                    FirebaseDatabase.getInstance().getReference("Saves").child(firebaseUser.uid)
                        .child(postId)
                postRef.removeValue()

                Toast.makeText(
                    this,
                    getString(R.string.toast_remove_fav_succeed_detail),
                    Toast.LENGTH_LONG
                ).show()

                finish()

                this.supportFragmentManager.beginTransaction().remove(ProfileFragment()).commit()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)


            }.setNegativeButton("Tidak") { dialogInterface, _ ->
                binding.removeFav.setImageResource(R.drawable.ic_favorite)
                dialogInterface.cancel()
            }.show()
    }

    private fun retrieveVideo() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(PostModel::class.java)

                    val mBuilder = AlertDialog.Builder(this@FavoriteActivity)
                    val mView = layoutInflater.inflate(R.layout.dialog_layout_video, null)

                    val videoView = mView.findViewById<PlayerView>(R.id.player_view)
                    val player = ExoPlayer.Builder(this@FavoriteActivity).build()
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

                    val mBuilder = AlertDialog.Builder(this@FavoriteActivity)
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

    private fun transVal() {
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
                    startActivity(Intent(applicationContext, CheckoutActivity::class.java))
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

    private fun phonePost() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(PostModel::class.java)
                    phonePublisher(post!!.getPublisher())
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun phonePublisher(publisherId: String?) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId!!)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UserModel::class.java)

                    val phoneNumber = user!!.getPhone()
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                    startActivity(intent)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

}