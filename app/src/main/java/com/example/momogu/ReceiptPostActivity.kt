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
import com.example.momogu.Model.ReceiptModel
import com.example.momogu.Model.UserModel
import com.example.momogu.databinding.ActivityReceiptPostBinding
import com.example.momogu.utils.Constanta.userLatitude
import com.example.momogu.utils.Constanta.userLongitude
import com.example.momogu.utils.Helper.getDate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

class ReceiptPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiptPostBinding
    private var postId: String = ""
    private lateinit var locationManager: LocationManager
    private lateinit var builder: AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptPostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        builder = AlertDialog.Builder(this)

        val preferences = this.getSharedPreferences("POST", Context.MODE_PRIVATE)
        if (preferences != null) {
            postId = preferences.getString("postid", "none")!!
        }

        retrievePosts()
        retrieveBuyer()
        cancelOtomation()
        finishOtomation()

        binding.backCheckout.setOnClickListener {
            finish()
        }

        binding.btnDone.setOnClickListener {
            setStatusFinish()
        }

        binding.btnReport.setOnClickListener {
            reportTrans()
        }

        binding.tvStatus.setOnClickListener{
            startActivity(Intent(this, StatusActivity::class.java))
        }

        binding.constraintProduct.setOnClickListener {
            startActivity(Intent(this, DetailPostActivity::class.java))
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

                    Picasso.get().load(post!!.getPostimage()).placeholder(R.drawable.ic_momogu_text_bottom)
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

    private fun retrieveBuyer() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Receipt").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val receipt = p0.getValue(ReceiptModel::class.java)

                    binding.tvInvoice.text = receipt!!.getPostId()
                    binding.tvDate.text =
                        "${getDate(receipt.getDateTime()!!.toLong(), "dd MMM yyyy, HH:mm")} WIB"
                    binding.tvDateCancel.text =
                        "${getDate(receipt.getdtCancel()!!.toLong(), "dd MMM yyyy, HH:mm")} WIB"

                    when {
                        receipt.getStatus()
                            .equals("Dikonfirmasi") -> {
                            binding.tvStatus.text = "Pesanan Dikonfirmasi!"
                            binding.cancel.visibility = View.GONE
                            binding.tvDateCancel.visibility = View.GONE
                            binding.finishOto.visibility = View.GONE
                            binding.tvDateFinishOto.visibility = View.GONE
                        }

                        receipt.getStatus()
                            .equals("Diproses") -> {
                            binding.tvStatus.text = "Pesanan Diproses!"
                            binding.cancel.visibility = View.GONE
                            binding.tvDateCancel.visibility = View.GONE
                            binding.finishOto.visibility = View.GONE
                            binding.tvDateFinishOto.visibility = View.GONE
                        }

                        receipt.getStatus()
                            .equals("Pengantaran") -> {
                            binding.tvStatus.text = "Dalam Pengantaran!"
                            binding.cancel.visibility = View.GONE
                            binding.tvDateCancel.visibility = View.GONE
                            binding.finishOto.visibility = View.GONE
                            binding.tvDateFinishOto.visibility = View.GONE
                        }

                        receipt.getStatus()
                            .equals("Sampai") -> {
                            binding.tvStatus.text = "Telah Sampai!"
                            binding.tvDateFinishOto.text = "${
                                getDate(
                                    receipt.getdtFinishOto()!!.toLong(),
                                    "dd MMM yyyy, HH:mm"
                                )
                            } WIB"
                            binding.finishOto.visibility = View.VISIBLE
                            binding.tvDateFinishOto.visibility = View.VISIBLE
                            binding.cancel.visibility = View.GONE
                            binding.tvDateCancel.visibility = View.GONE
                        }

                        receipt.getStatus()
                            .equals("Selesai") -> {
                            binding.tvStatus.text = "Selesai"
                            binding.finish.text = "Selesai"
                            binding.tvDateFinish.text = "${
                                getDate(
                                    receipt.getdtFinish()!!.toLong(),
                                    "dd MMM yyyy, HH:mm"
                                )
                            } WIB"
                            binding.cvMenu.visibility = View.GONE
                            binding.cancel.visibility = View.GONE
                            binding.tvDateCancel.visibility = View.GONE
                            binding.finishOto.visibility = View.GONE
                            binding.tvDateFinishOto.visibility = View.GONE
                            binding.finish.visibility = View.VISIBLE
                            binding.tvDateFinish.visibility = View.VISIBLE
                        }

                        else -> {
                            binding.tvStatus.text = receipt.getStatus()
                            binding.finishOto.visibility = View.GONE
                            binding.tvDateFinishOto.visibility = View.GONE
                        }
                    }

                    userInfo(receipt.getBuyerId())

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun userInfo(buyerId: String?) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(buyerId!!)

        usersRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UserModel::class.java)

                    if (user != null) {
                        binding.tvName.text = user.getFullname()
                        binding.tvAddress.text = user.getAddress()
                        binding.tvWhatsapp.text = "Telp: ${user.getPhone()}"
                        userLatitude = user.getLatitude()!!
                        userLongitude = user.getLongitude()!!
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun phoneBuyer() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Receipt").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val receipt = p0.getValue(ReceiptModel::class.java)

                    phoneInfo(receipt!!.getBuyerId())

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun phoneInfo(buyerId: String?) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(buyerId!!)

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

    private fun setStatusFinish() {

        val postsRef = FirebaseDatabase.getInstance().reference.child("Receipt").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val receipt = p0.getValue(ReceiptModel::class.java)

                    val arrived = receipt!!.getStatus().equals("Sampai")

                    if (arrived) {
                        builder.setTitle("Peringatan!")
                            .setMessage("Apakah anda ingin menyelesaikan pesanan ini?\nJika ya, pesanan akan dinyatakan selesai dan anda tidak dapat merubah apapun lagi.")
                            .setCancelable(true)
                            .setPositiveButton("Iya") { _, _ ->

                                val ref = FirebaseDatabase.getInstance().reference.child("Receipt")
                                val receiptMap = HashMap<String, Any>()
                                receiptMap["status"] = "Selesai"
                                receiptMap["dtFinish"] = System.currentTimeMillis().toString()
                                ref.child(postId).updateChildren(receiptMap)

                                finish()
                                Toast.makeText(applicationContext, "Pesanan telah diselesaikan!", Toast.LENGTH_SHORT)
                                    .show()

                            }.setNegativeButton("Tidak") { dialogInterface, _ ->
                                dialogInterface.cancel()
                            }.show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Transaksi hanya dapat diselesaikan setelah sapi sampai di tujuan!",
                            Toast.LENGTH_LONG
                        ).show()
                    }


                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun reportTrans() {

        val postsRef = FirebaseDatabase.getInstance().reference.child("Receipt").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val receipt = p0.getValue(ReceiptModel::class.java)

                    val arrived = receipt!!.getStatus().equals("Sampai")
                    val delivery = receipt.getStatus().equals("Pengantaran")

                    if (delivery || arrived) {
                        val intent = Intent(this@ReceiptPostActivity, ReportActivity::class.java)
                        this@ReceiptPostActivity.startActivity(intent)
                    }else {
                        Toast.makeText(
                            applicationContext,
                            "Transaksi hanya dapat dilaporkan setelah sapi sampai di tujuan!",
                            Toast.LENGTH_LONG
                        ).show()
                    }


                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun cancelOtomation() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Receipt").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val receipt = p0.getValue(ReceiptModel::class.java)

                    if (receipt!!.getStatus().equals("Menunggu konfirmasi!")) {
                        val currentTime = System.currentTimeMillis()
                        val timeCancel = receipt.getdtCancel()!!.toLong()

                        if (currentTime >= timeCancel) {
                            val postRef =
                                FirebaseDatabase.getInstance().getReference("Receipt")
                                    .child(postId)
                            postRef.removeValue()
                        }
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun finishOtomation() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Receipt").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val receipt = p0.getValue(ReceiptModel::class.java)

                    if (receipt!!.getStatus().equals("Sampai")) {
                        val currentTime = System.currentTimeMillis()
                        val timeArrived = receipt.getdtFinishOto()!!.toLong()

                        if (currentTime >= timeArrived) {
                            val ref = FirebaseDatabase.getInstance().reference.child("Receipt")
                            val receiptMap = HashMap<String, Any>()
                            receiptMap["status"] = "Selesai"
                            receiptMap["dtFinish"] = System.currentTimeMillis().toString()
                            ref.child(postId).updateChildren(receiptMap)
                        }
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}