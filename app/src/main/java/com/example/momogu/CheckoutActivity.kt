package com.example.momogu

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.UserModel
import com.example.momogu.databinding.ActivityCheckoutBinding
import com.example.momogu.utils.Constanta.userLatitude
import com.example.momogu.utils.Constanta.userLongitude
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private var postId: String = ""
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val preferences = this.getSharedPreferences("POST", Context.MODE_PRIVATE)
        if (preferences != null) {
            postId = preferences.getString("postid", "none")!!
        }

        retrievePosts()

        userInfo()

        binding.backCheckout.setOnClickListener {
            finish()
        }

        binding.btnCheckout.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Peringatan!")
                .setMessage("Apakah anda ingin memesan sapi ini?")
                .setCancelable(true)
                .setPositiveButton("Iya") { _, _ ->

                    retrieveReceipt()
                    startLoadingView()

                }.setNegativeButton("Tidak") { dialogInterface, _ ->
                    dialogInterface.cancel()
                }.show()
        }

        binding.cvPhone.setOnClickListener {
            phoneBuyer()
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
                        i.putExtra("productLatitude", userLatitude)
                        i.putExtra("productLongitude", userLongitude)
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

        binding.constraintLocation.setOnClickListener {
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
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
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

                    Picasso.get().load(post!!.getPostimage())
                        .placeholder(R.drawable.ic_momogu_text_bottom)
                        .into(binding.imagePost)
                    binding.tvProduct.text = post.getProduct()
                    binding.tvPrice.text = "Rp. ${post.getPrice()}"
                    binding.tvWeight.text = "${post.getWeight()} KG"
                    binding.tvPriceItems.text = "Rp. ${post.getPrice()}"
                    binding.tvPriceShipping.text = "Rp. ${post.getShipping()}"

                    val price = post.getPrice()
                    val getPrice = price?.replace(".", "")?.replace(",", "")?.toInt()

                    val shipping = post.getShipping()
                    val getShipping = shipping?.replace(".", "")?.replace(",", "")?.toInt()

                    val decimalFormat = DecimalFormat("#,##0")
                    val hasil = getPrice!! + getShipping!!
                    val formatNumber = hasil.toString().replace(".", "").replace(",", "")

                    val formatted = decimalFormat.format(formatNumber.toDouble())

                    binding.tvTotal.text = "Rp. $formatted"


                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun userInfo() {
        val usersRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UserModel::class.java)

                    binding.tvName.text = user!!.getFullname()
                    binding.tvAddress.text = user.getAddress()
                    binding.tvWhatsapp.text = user.getPhone()
                    userLatitude = user.getLatitude()!!
                    userLongitude = user.getLongitude()!!

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun phoneBuyer() {
        val usersRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
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

    private fun startLoadingView() {
        binding.layoutCheckoutView.visibility = View.VISIBLE
        binding.animCheckoutView.setAnimation("Checkout.json")
        binding.animCheckoutView.playAnimation()
        binding.btnCheckout.visibility = View.INVISIBLE
    }

    private fun retrieveReceipt() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(PostModel::class.java)

                    post?.getPublisher()?.let { addReceiptPost(it) }

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun addReceiptPost(publisherId: String) {
        val receiptPostRef = FirebaseDatabase.getInstance().reference.child("Receipt")
        val receiptPostMap = HashMap<String, Any>()

        receiptPostMap["buyerid"] = firebaseUser.uid
        receiptPostMap["sellerid"] = publisherId
        receiptPostMap["postid"] = postId
        receiptPostMap["status"] = "Menunggu konfirmasi!"
        receiptPostMap["dateTime"] = System.currentTimeMillis().toString()

        val currentTime = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000
        val newTime = currentTime + oneDay
        receiptPostMap["dtCancel"] = newTime.toString()

        receiptPostRef.child(postId).setValue(receiptPostMap)
    }

}