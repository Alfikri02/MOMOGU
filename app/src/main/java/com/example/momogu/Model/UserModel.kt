package com.example.momogu.Model

class UserModel {
    private var username: String? = null
    private var fullname: String? = null
    private var wa: String? = null
    private var city: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var address: String? = null
    private var image: String? = null
    private var uid: String? = null
    private var statusOn: String? = null
    private var lastOnline: String? = null


    constructor()

    constructor(
        username: String,
        fullname: String,
        wa: String,
        city: String,
        latitude: Double?,
        longitude: Double?,
        address: String,
        image: String,
        uid: String,
        statusOn: String,
        lastOnline: String
    ) {
        this.username = username
        this.fullname = fullname
        this.wa = wa
        this.city = city
        this.latitude = latitude
        this.longitude = longitude
        this.address = address
        this.image = image
        this.uid = uid
        this.statusOn = statusOn
        this.lastOnline = lastOnline
    }

    fun getUsername(): String? {
        return username
    }

    fun getFullname(): String? {
        return fullname
    }

    fun getWa(): String? {
        return wa
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

    fun getUID(): String? {
        return uid
    }

    fun getStatusOn(): String? {
        return statusOn
    }

    fun getLastOnline(): String? {
        return lastOnline
    }

}