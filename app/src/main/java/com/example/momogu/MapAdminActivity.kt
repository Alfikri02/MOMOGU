package com.example.momogu

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.momogu.databinding.ActivityMapAdminBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapAdminActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapAdminBinding
    private var latitudeUser = 0.0
    private var longitudeUser = 0.0
    private var latitudeProduct = 0.0
    private var longitudeProduct = 0.0

    @SuppressLint("DiscouragedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        latitudeUser = intent.getDoubleExtra("latitude", 0.0)
        longitudeUser = intent.getDoubleExtra("longitude", 0.0)

        binding.btnSaveLocation.setOnClickListener {
            if (latitudeProduct == 0.0 || longitudeProduct == 0.0) {
                latitudeProduct = latitudeUser
                longitudeProduct = longitudeUser
            }
            AddPostActivity.setProductLocation(latitudeProduct, longitudeProduct)
            finish()
        }

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
            .findFragmentById(R.id.mapAdmin) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(latitudeUser, longitudeUser),
                15f
            )
        )

        mMap.setOnMapClickListener { latlng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latlng).title("Product here"))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15f))
            latitudeProduct = latlng.latitude
            longitudeProduct = latlng.longitude
        }

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