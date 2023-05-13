package com.example.momogu.Model

class ReceiptModel {
    private var buyerid: String? = null
    private var sellerid: String? = null
    private var postid: String? = null
    private var status: String? = null
    private var dateTime: String? = null
    private var dtCancel: String? = null
    private var dtConfirm: String? = null
    private var dtProcess: String? = null
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
        dtProcess: String?,
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
        this.dtProcess = dtProcess
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

    fun getdtProcess(): String? {
        return dtProcess
    }

    fun getdtDelivery(): String? {
        return dtDelivery
    }

    fun getdtFinish(): String? {
        return dtFinish
    }

}