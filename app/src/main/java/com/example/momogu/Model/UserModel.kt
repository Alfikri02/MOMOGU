package com.example.momogu.Model

class UserModel
{
    private var username: String? = null
    private var fullname: String? = null
    private var bio: String? = null
    private var image: String? = null
    private var uid: String? = null

    constructor()

    constructor(username: String, fullname: String, bio: String, image: String, uid: String)
    {
        this.username = username
        this.fullname = fullname
        this.bio = bio
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

    fun getBio(): String?
    {
        return bio
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