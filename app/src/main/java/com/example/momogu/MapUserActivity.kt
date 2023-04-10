package com.example.momogu

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.momogu.databinding.ActivityMapUserBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapUserActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapUserBinding
    private var productLatitude = 0.0
    private var  productLongitude = 0.0
    private var userLatitude = 0.0
    private var userLongitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productLatitude = intent.getDoubleExtra("productLatitude",0.0)
        productLongitude = intent.getDoubleExtra("productLongitude",0.0)
        userLatitude = intent.getDoubleExtra("userLatitude",0.0)
        userLongitude = intent.getDoubleExtra("userLongitude",0.0)

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
            .findFragmentById(R.id.mapUser) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        val productLocation = LatLng(productLatitude,productLongitude)
        val userLocation = LatLng(userLatitude,userLongitude)

        mMap.addMarker(MarkerOptions().position(productLocation).title("Product here"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(productLocation,15f))

        mMap.addPolyline(
            PolylineOptions()
            .add(productLocation)
            .add(userLocation)
            .color(R.color.ijotua)
        )

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

}