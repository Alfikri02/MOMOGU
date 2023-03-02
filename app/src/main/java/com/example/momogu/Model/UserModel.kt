package com.example.momogu.Model

class UserModel
{
    private var username: String? = null
    private var fullname: String? = null
    private var wa: String? = null
    private var city: String? = null
    private var address: String? = null
    private var image: String? = null
    private var uid: String? = null

    constructor()

    constructor(username: String, fullname: String, wa: String, city: String, address: String, image: String, uid: String)
    {
        this.username = username
        this.fullname = fullname
        this.wa = wa
        this.city = city
        this.address = address
        this.image = image
        this.uid = uid
    }

    fun getUsername(): String?
    {
        return username
    }

    fun getFullname(): String?
    {
        return fullname
    }

    fun getWa(): String?
    {
        return wa
    }

    fun getCity(): String?
    {
        return city
    }

    fun getAddress(): String?
    {
        return address
    }

    fun getImage(): String?
    {
        return image
    }

    fun getUID(): String?
    {
        return uid
    }
}