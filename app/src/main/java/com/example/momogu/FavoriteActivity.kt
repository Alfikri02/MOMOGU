package com.example.momogu

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.ReceiptModel
import com.example.momogu.Model.UserModel
import com.example.momogu.databinding.ActivityFavoriteBinding
import com.example.momogu.utils.Constanta.productLatitude
import com.example.momogu.utils.Constanta.productLongitude
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
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

        binding.profileDetail.setOnClickListener {
            startActivity(Intent(this, BreederActivity::class.java))
            finish()
        }

        binding.btnBuy.setOnClickListener {
            transVal()
        }

        binding.lineFavorite.setOnClickListener {
            builder.setTitle("Peringatan!")
                .setMessage("Apakah anda ingin menghapus sapi ini dari daftar favorit?")
                .setCancelable(true)
                .setPositiveButton("Iya") { _, _ ->
                    val postRef =
                        FirebaseDatabase.getInstance().getReference("Saves").child(firebaseUser.uid)
                            .child(postId)
                    postRef.removeValue()

                    Toast.makeText(
                        this,
                        "Berhasil menghapus dari daftar favorit! \nSilahkan buka halaman profil kembali!",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    finish()

                }.setNegativeButton("Tidak") { dialogInterface, _ ->
                    dialogInterface.cancel()
                }.show()
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
                        "Please accept permission to view the location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        binding.btnSeeLocation.setOnClickListener {
            checkPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun checkGPS(): Boolean {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true
        } else {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("GPS isn't enabled !")
            dialog.setMessage("Enable it to see the distance between you and the product.")
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

                    Picasso.get().load(post!!.getPostimage()).placeholder(R.drawable.profile)
                        .into(binding.imagePost)
                    binding.productDetail.text = post.getProduct()
                    binding.priceDetail.text = "Rp. ${post.getPrice()}"
                    binding.dateDetail.text = getDate(post.getDateTime()!!.toLong(), "dd/MM/yyyy")
                    binding.etWeight.text = "${post.getWeight()} KG"
                    binding.etGender.text = post.getGender()
                    binding.etAge.text = "${post.getAge()} Bulan"
                    binding.etColor.text = post.getColor()
                    binding.etDesc.setText(post.getDesc())
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
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UserModel::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(binding.userProfile)
                    binding.tvFullnameDetail.text = user.getFullname()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun transVal(){
        val receiptRef = FirebaseDatabase.getInstance().reference.child("Receipt").child(postId)
        receiptRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val receipt = dataSnapshot.getValue(ReceiptModel::class.java)

                    if (receipt!!.getStatus().equals("Selesai")) {
                        Toast.makeText(
                            applicationContext,
                            "Sapi telah terjual!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Transaksi telah berlangsung!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    startActivity(Intent(applicationContext, CheckoutActivity::class.java))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
    }

    private fun soldVal(){
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

    @SuppressLint("SimpleDateFormat")
    private fun getDate(milliSeconds: Long, dateFormat: String?): String? {
        val formatter = SimpleDateFormat(dateFormat)
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds

        return formatter.format(calendar.time)
    }
}