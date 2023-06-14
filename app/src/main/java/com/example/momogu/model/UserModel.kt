package com.example.momogu.model

class UserModel {
    private var fullname: String? = null
    private var phone: String? = null
    private var city: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var address: String? = null
    private var image: String? = null
    private var statusOn: String? = null
    private var lastOnline: String? = null


    constructor()

    constructor(
        fullname: String,
        phone: String,
        city: String,
        latitude: Double?,
        longitude: Double?,
        address: String,
        image: String,
        statusOn: String,
        lastOnline: String
    ) {
        this.fullname = fullname
        this.phone = phone
        this.city = city
        this.latitude = latitude
        this.longitude = longitude
        this.address = address
        this.image = image
        this.statusOn = statusOn
        this.lastOnline = lastOnline
    }

    fun getFullname(): String? {
        return fullname
    }

    fun getPhone(): String? {
        return phone
    }

    fun getCity(): String? {
        return city
    }

    fun getLatitude(): Double? {
        return latitude
    }

    fun getLongitude(): Double? {
        return longitude
    }

    fun getAddress(): String? {
        return address
    }

    fun getImage(): String? {
        return image
    }

    fun getStatusOn(): String? {
        return statusOn
    }

    fun getLastOnline(): String? {
        return lastOnline
    }

}