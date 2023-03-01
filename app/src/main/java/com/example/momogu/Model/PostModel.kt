package com.example.momogu.Model

class PostModel {
    private var postid: String? = null
    private var postimage: String? = null
    private var publisher: String? = null
    private var product: String? = null
    private var age: String? = null
    private var weight: String? = null
    private var gender: String? = null
    private var desc: String? = null
    private var price: String? = null
    private var dateTime: String? = null

    constructor()

    constructor(
        postid: String?,
        postimage: String?,
        publisher: String?,
        product: String?,
        age: String?,
        weight: String?,
        gender: String?,
        desc: String?,
        price: String?,
        dateTime: String?
    ) {
        this.postid = postid
        this.postimage = postimage
        this.publisher = publisher
        this.product = product
        this.age = age
        this.weight = weight
        this.gender = gender
        this.desc = desc
        this.price = price
        this.dateTime = dateTime
    }

    fun getPostid(): String? {
        return postid
    }

    fun getPostimage(): String? {
        return postimage
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

    fun getGender(): String? {
        return gender
    }

    fun getDesc(): String? {
        return desc
    }

    fun getPrice(): String? {
        return price
    }

    fun getDateTime(): String? {
        return dateTime
    }
}