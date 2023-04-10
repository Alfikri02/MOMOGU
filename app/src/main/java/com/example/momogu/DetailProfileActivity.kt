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
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.momogu.Model.PostModel
import com.example.momogu.databinding.ActivityDetailProfileBinding
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class DetailProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailProfileBinding
    private var storagePostPicRef: StorageReference? = null
    private var postId: String = ""
    private lateinit var builder: AlertDialog.Builder

    private lateinit var locationManager: LocationManager
    var userLatitude = 0.0
    var userLongitude = 0.0
    var productLatitude = 0.0
    var productLongitude = 0.0

    @SuppressLint("DiscouragedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Post Pictures")

        val preferences = this.getSharedPreferences("POST", Context.MODE_PRIVATE)
        if (preferences != null) {
            postId = preferences.getString("postid", "none")!!
        }

        builder = AlertDialog.Builder(this)

        retrievePosts()

        binding.closeDetail.setOnClickListener {
            finish()
        }

        binding.menuDetail.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.edit_data -> {
                        startActivity(Intent(this, EditProductActivity::class.java))
                        true
                    }
                    R.id.delete_data -> {
                        builder.setTitle("Peringatan!")
                            .setMessage("Apakah anda ingin menghapus data sapi ini?")
                            .setCancelable(true)
                            .setPositiveButton("Iya") { _, _ ->
                                val postRef =
                                    FirebaseDatabase.getInstance().getReference("Posts")
                                        .child(postId)
                                postRef.removeValue()
                                val fileRef = storagePostPicRef!!.child("$postId.jpg")
                                fileRef.delete()

                                Toast.makeText(this, "Data sapi berhasil dihapus!", Toast.LENGTH_SHORT)
                                    .show()
                                finish()
                            }.setNegativeButton("Tidak") { dialogInterface, _ ->
                                dialogInterface.cancel()
                            }.show()

                        true
                    }
                    else -> false
                }
            }
            popupMenu.inflate(R.menu.profile_menu)

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

        val checkPermission  = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted->
            if (isGranted){
                if(checkGPS()){
                    val locationClient = LocationServices.getFusedLocationProviderClient(this)
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
                    locationClient.lastLocation
                        .addOnSuccessListener {location ->
                            if(location != null){
                                userLatitude = location.latitude
                                userLongitude = location.longitude
                                val i = Intent(this,MapUserActivity::class.java)
                                i.putExtra("productLatitude",productLatitude)
                                i.putExtra("productLongitude",productLongitude)
                                i.putExtra("userLatitude",userLatitude)
                                i.putExtra("userLongitude",userLongitude)
                                startActivity(i)
                            }
                        }
                }
            }else{
                Toast.makeText(this, "Please accept permission to view the location", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSeeLocation.setOnClickListener {
            checkPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    }

    private fun checkGPS():Boolean{
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