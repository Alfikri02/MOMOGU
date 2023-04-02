package com.example.momogu.Model

class ReceiptModel {
    private var userid: String? = null
    private var postid: String? = null
    private var status: String? = null
    private var dateTime: String? = null
    private var ispost = false

    constructor()

    constructor(
        userid: String?,
        postid: String?,
        status: String?,
        dateTime: String?,
        ispost: Boolean
    ) {
        this.userid = userid
        this.postid = postid
        this.status = status
        this.dateTime = dateTime
        this.ispost = ispost
    }

    fun getUserId(): String? {
        return userid
    }

    fun getPostId(): String? {
        return postid
    }

    fun getStatus(): String? {
        return status
    }

    fun getDateTime(): String? {
        return dateTime
    }

    fun getIsPost(): Boolean {
        return ispost
    }
}