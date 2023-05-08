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
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.ReceiptModel
import com.example.momogu.Model.UserModel
import com.example.momogu.databinding.ActivityReceiptUserBinding
import com.example.momogu.utils.Constanta.productLatitude
import com.example.momogu.utils.Constanta.productLongitude
import com.example.momogu.utils.Helper.getDate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

class ReceiptUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiptUserBinding
    private var postId: String = ""
    private lateinit var builder: AlertDialog.Builder
    private lateinit var locationManager: LocationManager

    @SuppressLint("DiscouragedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptUserBinding.inflate(layoutInflater)
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
        deleteOtomation()

        binding.backCheckout.setOnClickListener {
            finish()
        }

        binding.menuReceipt.setOnClickListener {
            menuReceipt(it)
        }

        binding.btnConfirm.setOnClickListener{
            setStatusConfirm()
        }

        binding.btnProses.setOnClickListener{
            setStatusProses()
        }

        binding.btnDelivery.setOnClickListener{
            setStatusDelivery()
        }

        binding.btnDone.setOnClickListener{
            setStatusDone()
        }

        binding.btnCancel.setOnClickListener {
            cancelReceipt()
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

                    Picasso.get().load(post!!.getPostimage()).placeholder(R.drawable.momogu)
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
                    binding.tvDate.text = "${getDate(receipt.getDateTime()!!.toLong(), "dd MMM yyyy, HH:mm")} WIB"
                    binding.tvDateCancel.text = "${getDate(receipt.getdtCancel()!!.toLong(),"dd MMM yyyy, HH:mm")} WIB"

                    when {
                        receipt.getStatus()
                            .equals("Dikonfirmasi") -> {
                            binding.tvStatus.text = "Pesanan Dikonfirmasi!"
                            binding.btnConfirm.visibility = View.GONE
                            binding.btnProses.visibility = View.VISIBLE
                        }

                        receipt.getStatus()
                            .equals("Diproses") -> {
                            binding.tvStatus.text = "Pesanan Diproses!"
                            binding.btnProses.visibility = View.GONE
                            binding.btnDelivery.visibility = View.VISIBLE
                        }

                        receipt.getStatus()
                            .equals("Pengantaran") -> {
                            binding.tvStatus.text = "Dalam Pengantaran!"
                            binding.btnDelivery.visibility = View.GONE
                            binding.btnDone.visibility = View.VISIBLE
                        }

                        receipt.getStatus()
                            .equals("Selesai") -> {
                            binding.tvStatus.text = "Selesai"
                        }

                        else -> {
                            binding.tvStatus.text = receipt.getStatus()
                            binding.btnConfirm.visibility = View.VISIBLE
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
                        binding.tvWhatsapp.text = "Telp: ${user.getWa()}"
                        productLatitude = user.getLatitude()!!
                        productLongitude = user.getLongitude()!!
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

                    val phoneNumber = user!!.getWa()
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                    startActivity(intent)

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun deleteOtomation() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Receipt").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val receipt = p0.getValue(ReceiptModel::class.java)

                    val waitConfirm = receipt!!.getStatus().equals("Menunggu konfirmasi!")
                    val currentTime = System.currentTimeMillis()
                    val timeCancel = receipt.getdtCancel()!!.toLong()

                    if (waitConfirm && currentTime == timeCancel){
                        val postRef =
                            FirebaseDatabase.getInstance().getReference("Receipt")
                                .child(postId)
                        postRef.removeValue()
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun menuReceipt(it: View) {
        val popupMenu = PopupMenu(this, it)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.confirm_receipt -> {
                    setStatusConfirm()
                    true
                }

                R.id.proses_receipt -> {
                    setStatusProses()
                    true
                }

                R.id.order_receipt -> {
                    setStatusDelivery()

                    true
                }

                R.id.done_receipt -> {
                    setStatusDone()
                    true
                }

                R.id.cancel -> {
                    cancelReceipt()
                    true
                }

                else -> false
            }
        }
        popupMenu.inflate(R.menu.receipt_menu)

        try {
            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val mPopup = fieldMPopup.get(popupMenu)
            mPopup.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)
        } catch (e: Exception) {
            Log.e("Main", "Error showing menu icons.", e)
        } finally {
            popupMenu.show()
        }
    }

    private fun setStatusConfirm() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Peringatan!")
            .setMessage("Apakah anda ingin mengkonfirmasi pesanan ini?")
            .setCancelable(true)
            .setPositiveButton("Iya") { _, _ ->

                val ref = FirebaseDatabase.getInstance().reference.child("Receipt")
                val receiptMap = HashMap<String, Any>()
                receiptMap["status"] = "Dikonfirmasi"
                ref.child(postId).updateChildren(receiptMap)

                finish()
                Toast.makeText(this, "Pesanan telah dikonfirmasi!", Toast.LENGTH_SHORT)
                    .show()

            }.setNegativeButton("Tidak") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.show()

    }

    private fun setStatusProses() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Peringatan!")
            .setMessage("Apakah anda ingin memproses pesanan ini?")
            .setCancelable(true)
            .setPositiveButton("Iya") { _, _ ->

                val ref = FirebaseDatabase.getInstance().reference.child("Receipt")
                val receiptMap = HashMap<String, Any>()
                receiptMap["status"] = "Diproses"
                ref.child(postId).updateChildren(receiptMap)

                finish()
                Toast.makeText(this, "Pesanan telah diproses!", Toast.LENGTH_SHORT)
                    .show()

            }.setNegativeButton("Tidak") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.show()

    }

    private fun setStatusDelivery() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Peringatan!")
            .setMessage("Apakah anda ingin mengantar pesanan ini?")
            .setCancelable(true)
            .setPositiveButton("Iya") { _, _ ->

                val ref = FirebaseDatabase.getInstance().reference.child("Receipt")
                val receiptMap = HashMap<String, Any>()
                receiptMap["status"] = "Pengantaran"
                ref.child(postId).updateChildren(receiptMap)

                finish()
                Toast.makeText(
                    this,
                    "Pesanan sedang dalam pengantaran!",
                    Toast.LENGTH_SHORT
                ).show()

            }.setNegativeButton("Tidak") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.show()

    }

    private fun setStatusDone() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Peringatan!")
            .setMessage("Apakah anda ingin menyelesaikan pesanan ini?\nJika ya, pesanan akan dinyatakan selesai dan anda tidak dapat merubah apapun lagi.")
            .setCancelable(true)
            .setPositiveButton("Iya") { _, _ ->

                val ref = FirebaseDatabase.getInstance().reference.child("Receipt")
                val receiptMap = HashMap<String, Any>()
                receiptMap["status"] = "Selesai"
                ref.child(postId).updateChildren(receiptMap)

                finish()
                Toast.makeText(this, "Pesanan telah diselesaikan!", Toast.LENGTH_SHORT)
                    .show()

            }.setNegativeButton("Tidak") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.show()

    }

    private fun cancelReceipt() {
        builder.setTitle("Peringatan!")
            .setMessage("Apakah anda ingin membatalkan transaksi ini?")
            .setCancelable(true)
            .setPositiveButton("Iya") { _, _ ->

                val postRef =
                    FirebaseDatabase.getInstance().getReference("Receipt")
                        .child(postId)
                postRef.removeValue()

                Toast.makeText(
                    this,
                    "Transaksi berhasil dibatalkan",
                    Toast.LENGTH_SHORT
                )
                    .show()
                finish()
            }.setNegativeButton("Tidak") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.show()
    }

}