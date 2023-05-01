package com.example.momogu.Model

class PostModel {
    private var postid: String? = null
    private var postimage: String? = null
    private var postvideo: String? = null
    private var publisher: String? = null
    private var product: String? = null
    private var age: String? = null
    private var weight: String? = null
    private var color: String? = null
    private var gender: String? = null
    private var desc: String? = null
    private var price: String? = null
    private var shipping: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var location: String? = null
    private var dateTime: String? = null

    constructor()

    constructor(
        postid: String?,
        postimage: String?,
        postvideo: String?,
        publisher: String?,
        product: String?,
        age: String?,
        weight: String?,
        color: String?,
        gender: String?,
        desc: String?,
        price: String?,
        shipping: String?,
        latitude: Double?,
        longitude: Double?,
        location: String?,
        dateTime: String?
    ) {
        this.postid = postid
        this.postimage = postimage
        this.postvideo = postvideo
        this.publisher = publisher
        this.product = product
        this.age = age
        this.weight = weight
        this.color = color
        this.gender = gender
        this.desc = desc
        this.price = price
        this.shipping = shipping
        this.latitude = latitude
        this.longitude = longitude
        this.location = location
        this.dateTime = dateTime
    }

    fun getPostid(): String? {
        return postid
    }

    fun getPostimage(): String? {
        return postimage
    }

    fun getPostvideo(): String? {
        return postvideo
    }

    fun getPublisher(): String? {
        return publisher
    }

    fun getProduct(): String? {
        return product
    }

    fun getAge(): String? {
        return age
    }

    fun getWeight(): String? {
        return weight
    }

    fun getColor(): String? {
        return color
    }

    fun getGender(): String? {
        return gender
    }

    fun getDesc(): String? {
        return desc
    }

    fun getPrice(): String? {
        return price
    }

    fun getShipping(): String? {
        return shipping
    }

    fun getLatitude(): Double? {
        return latitude
    }

    fun getLongitude(): Double? {
        return longitude
    }

    fun getLocation(): String? {
        return location
    }

    fun getDateTime(): String? {
        return dateTime
    }
}