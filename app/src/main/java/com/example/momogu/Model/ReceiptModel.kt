package com.example.momogu.Model

class ReceiptModel {
    private var buyerid: String? = null
    private var sellerid: String? = null
    private var postid: String? = null
    private var status: String? = null
    private var dateTime: String? = null
    private var dtCancel: String? = null
    private var dtConfirm: String? = null
    private var dtProses: String? = null
    private var dtDelivery: String? = null
    private var dtFinish: String? = null

    constructor()

    constructor(
        buyerid: String?,
        sellerid: String?,
        postid: String?,
        status: String?,
        dateTime: String?,
        dtCancel: String?,
        dtConfirm: String?,
        dtProses: String?,
        dtDelivery: String?,
        dtFinish: String?
    ) {
        this.buyerid = buyerid
        this.sellerid = sellerid
        this.postid = postid
        this.status = status
        this.dateTime = dateTime
        this.dtCancel = dtCancel
        this.dtConfirm = dtConfirm
        this.dtProses = dtProses
        this.dtDelivery = dtDelivery
        this.dtFinish = dtFinish
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

    fun getdtCancel(): String? {
        return dtCancel
    }

    fun getdtConfirm(): String? {
        return dtConfirm
    }

    fun getdtProses(): String? {
        return dtProses
    }

    fun getdtDelivery(): String? {
        return dtDelivery
    }

    fun getdtFinish(): String? {
        return dtFinish
    }

}