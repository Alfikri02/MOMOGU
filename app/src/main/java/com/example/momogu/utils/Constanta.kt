package com.example.momogu.utils

import androidx.lifecycle.MutableLiveData

object Constanta {

    enum class LocationPicker {
        IsPicked, Latitude, Longitude
    }

    const val LOCATION_PERMISSION_CODE = 30
    const val REQUEST_POST_IMAGE = 100
    const val PERMISSIONS_REQUEST_LOCATION = 1

    val isLocationPicked = MutableLiveData(false)
    var coordinateLatitude = 0.0
    var coordinateLongitude = 0.0

    var productLatitude = 0.0
    var productLongitude = 0.0
}