package com.example.momogu

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.momogu.databinding.ActivityMapAdminBinding
import com.example.momogu.databinding.CustomTooltipPickLocationStoryBinding
import com.example.momogu.utils.Constanta
import com.example.momogu.utils.Constanta.PERMISSIONS_REQUEST_LOCATION
import com.example.momogu.utils.Constanta.coordinateLatitude
import com.example.momogu.utils.Constanta.coordinateLongitude
import com.example.momogu.utils.Constanta.isLocationPicked
import com.example.momogu.utils.Helper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapAdminActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private lateinit var mAdminMap: GoogleMap
    private lateinit var binding: ActivityMapAdminBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("DiscouragedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnCancel.setOnClickListener {
            isLocationPicked.postValue(false)
            finish()
        }

        binding.btnSelectLocation.setOnClickListener {
            if (isLocationPicked.value == true) {
                val intent = Intent()
                intent.putExtra(
                    Constanta.LocationPicker.IsPicked.name,
                    isLocationPicked.value
                )
                intent.putExtra(
                    Constanta.LocationPicker.Latitude.name,
                    coordinateLatitude
                )
                intent.putExtra(
                    Constanta.LocationPicker.Longitude.name,
                    coordinateLongitude
                )
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Helper.showDialogInfo(
                    this,
                    getString(R.string.UI_validation_maps_select_area)
                )
            }
        }

        binding.menuMaps.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.normal_type -> {
                        mAdminMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                        true
                    }
                    R.id.satellite_type -> {
                        mAdminMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        true
                    }
                    R.id.terrain_type -> {
                        mAdminMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                        true
                    }
                    R.id.hybrid_type -> {
                        mAdminMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                        true
                    }
                    else -> false
                }
            }
            popupMenu.inflate(R.menu.map_options)
            popupMenu.show()
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapAdmin) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mAdminMap = googleMap

        mAdminMap.uiSettings.isZoomControlsEnabled = true
        mAdminMap.uiSettings.isCompassEnabled = true
        mAdminMap.uiSettings.isMyLocationButtonEnabled = true

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_LOCATION)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                mAdminMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        }

        mAdminMap.setInfoWindowAdapter(this)

        mAdminMap.setOnInfoWindowClickListener { marker ->
            postLocationSelected(marker.position.latitude, marker.position.longitude)
            marker.hideInfoWindow()
        }
        mAdminMap.setOnMapClickListener {
            mAdminMap.clear()
            mAdminMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            it.latitude,
                            it.longitude
                        )
                    )
            )?.showInfoWindow()
        }
        mAdminMap.setOnPoiClickListener {
            Toast.makeText(this, it.name, Toast.LENGTH_SHORT).show()
            mAdminMap.clear()
            mAdminMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            it.latLng.latitude,
                            it.latLng.longitude
                        )
                    )
            )?.showInfoWindow()
        }

        getMyLocation()
    }

    private fun postLocationSelected(lat: Double, lon: Double) {
        val address =
            Helper.parseAddressLocation(
                this,
                lat,
                lon
            )
        binding.addressBar.text = address
        isLocationPicked.postValue(true)
        coordinateLatitude = lat
        coordinateLongitude = lon
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
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mAdminMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    /* while marker touched -> show custom view */
    override fun getInfoWindow(marker: Marker): View {
        val bindingTooltips =
            CustomTooltipPickLocationStoryBinding.inflate(LayoutInflater.from(this))
        bindingTooltips.location.text = Helper.parseAddressLocation(
            this,
            marker.position.latitude, marker.position.longitude
        )
        return bindingTooltips.root
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    onMapReady(mAdminMap)
                }
                return
            }
        }
    }

}