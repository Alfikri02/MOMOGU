package com.example.momogu.Model

class ReceiptModel {
    private var buyerid: String? = null
    private var sellerid: String? = null
    private var postid: String? = null
    private var status: String? = null
    private var dateTime: String? = null

    constructor()

    constructor(
        buyerid: String?,
        sellerid: String?,
        postid: String?,
        status: String?,
        dateTime: String?,
    ) {
        this.buyerid = buyerid
        this.sellerid = sellerid
        this.postid = postid
        this.status = status
        this.dateTime = dateTime
    }

    fun getBuyerId(): String? {
        return buyerid
    }

    fun getSellerId(): String? {
        return sellerid
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
}