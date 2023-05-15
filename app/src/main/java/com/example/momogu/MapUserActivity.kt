package com.example.momogu

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.momogu.databinding.ActivityMapUserBinding
import com.example.momogu.databinding.SeeLocationBinding
import com.example.momogu.utils.Constanta.productLatitude
import com.example.momogu.utils.Constanta.productLongitude
import com.example.momogu.utils.Helper.parseAddressLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapUserActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productLatitude = intent.getDoubleExtra("productLatitude",0.0)
        productLongitude = intent.getDoubleExtra("productLongitude",0.0)

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

        mMap.setInfoWindowAdapter(this)

        mMap.addMarker(MarkerOptions().position(productLocation))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(productLocation,15f))

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

    override fun getInfoWindow(marker: Marker): View {
        val bindingTooltips =
            SeeLocationBinding.inflate(LayoutInflater.from(this))
        bindingTooltips.location.text = parseAddressLocation(
            this,
            marker.position.latitude, marker.position.longitude
        )
        return bindingTooltips.root
    }

}