package com.example.momogu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.momogu.adapter.PostAdapter
import com.example.momogu.model.PostModel
import com.example.momogu.databinding.ActivityMapsBinding
import com.example.momogu.databinding.MapsItemBinding
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter,
    GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var postList: MutableList<PostModel>? = null
    private var postAdapter: PostAdapter? = null
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        binding.closeMaps.setOnClickListener {
            finish()
        }

        binding.menuMaps.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.normal_type -> {
                        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                        true
                    }

                    R.id.satellite_type -> {
                        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        true
                    }

                    R.id.terrain_type -> {
                        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                        true
                    }

                    R.id.hybrid_type -> {
                        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                        true
                    }

                    else -> false
                }
            }
            popupMenu.inflate(R.menu.map_options)
            popupMenu.show()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.Maps) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        retrieveMaps()

        mMap.setInfoWindowAdapter(this)
        mMap.setOnInfoWindowClickListener(this)

        getMyLocation()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    @SuppressLint("SetTextI18n")
    override fun getInfoWindow(marker: Marker): View {
        val post = marker.tag as? PostModel ?: return View(this)
        val bindingMaps = MapsItemBinding.inflate(LayoutInflater.from(this))

        with(bindingMaps) {
            Picasso.get().load(post.getPostimage()).into(imageMaps)
            tvProductMaps.text = post.getProduct()
            tvPriceMaps.text = "Rp. ${post.getPrice()}"
            tvWeightMaps.text = "${post.getWeight()} KG"
            tvLocationMaps.text = post.getLocation()
            tvUploadMaps.text = TimeAgo.using(post.getDateTime()!!.toLong(),
                TimeAgoMessages.Builder().withLocale(Locale("in")).build())
        }

        return bindingMaps.root
    }



    override fun onInfoWindowClick(marker: Marker) {
        val post = marker.tag as? PostModel ?: return
        if (post.getPublisher().equals(firebaseUser.uid)) {
            Toast.makeText(this, "Sapi ini milik anda!", Toast.LENGTH_SHORT).show()
        } else {
            val editor = this.getSharedPreferences("POST", Context.MODE_PRIVATE).edit()
            editor.putString("postid", post.getPostid())
            editor.apply()

            startActivity(Intent(this, DetailPostActivity::class.java))
        }
    }

    private fun retrieveMaps() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList?.clear()
                val boundsBuilder = LatLngBounds.Builder()

                for (snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(PostModel::class.java)

                    val latlng = LatLng(post?.getLatitude()!!, post.getLongitude()!!)
                    val marker = mMap.addMarker(MarkerOptions().position(latlng))

                    boundsBuilder.include(latlng)

                    val bounds: LatLngBounds = boundsBuilder.build()
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            bounds,
                            resources.displayMetrics.widthPixels,
                            resources.displayMetrics.heightPixels,
                            300
                        )
                    )

                    marker?.tag = post

                    postAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

}