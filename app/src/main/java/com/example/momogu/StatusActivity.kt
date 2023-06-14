@file:Suppress("DEPRECATION")

package com.example.momogu

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.momogu.model.ReceiptModel
import com.example.momogu.databinding.ActivityStatusBinding
import com.example.momogu.utils.Helper.getDate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatusBinding
    private var postId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val preferences = this.getSharedPreferences("POST", Context.MODE_PRIVATE)
        if (preferences != null) {
            postId = preferences.getString("postid", "none")!!
        }

        retrieveStatus()

        binding.backStatus.setOnClickListener {
            finish()
        }

        binding.icConfirm.setOnClickListener {
            Toast.makeText(this, "Pesanan Dikonfirmasi!", Toast.LENGTH_SHORT).show()
        }

        binding.icProcess.setOnClickListener {
            Toast.makeText(this, "Pesanan Diproses!", Toast.LENGTH_SHORT).show()
        }

        binding.icShipping.setOnClickListener {
            Toast.makeText(this, "Pesanan Dalam Pengantaran!", Toast.LENGTH_SHORT).show()
        }
        binding.icFinish.setOnClickListener {
            Toast.makeText(this, "Pesanan Selesai!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun retrieveStatus() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Receipt").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("ResourceAsColor", "SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val receipt = p0.getValue(ReceiptModel::class.java)

                    binding.waitConfirm.text = "Pembeli - ${getDate(receipt!!.getDateTime()!!.toLong(), "dd MMM yyyy")}"
                    binding.timeWaitConfirm.text = "${getDate(receipt.getDateTime()!!.toLong(), "HH:mm")} WIB"

                    when {
                        receipt.getStatus()
                            .equals("Dikonfirmasi") -> {
                            binding.tvStatus.text = "Pesanan Dikonfirmasi!"
                            binding.icConfirm.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.checkConfirm.setImageResource(R.drawable.ic_check)
                            binding.checkConfirm.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)

                            //status Detail
                            binding.dotWaitConfirm.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.dotConfirm.visibility = View.VISIBLE
                            binding.dotConfirm.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.confirm.visibility = View.VISIBLE
                            binding.confirm.text = "Penjual - ${getDate(receipt.getdtConfirm()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeConfirm.visibility = View.VISIBLE
                            binding.timeConfirm.text = "${getDate(receipt.getdtConfirm()!!.toLong(), "HH:mm")} WIB"
                            binding.detailConfirm.visibility = View.VISIBLE
                            binding.lineConfirm.setBackgroundColor(resources.getColor(R.color.ijotua))

                        }

                        receipt.getStatus()
                            .equals("Diproses") -> {
                            binding.tvStatus.text = "Pesanan Diproses!"

                            binding.icConfirm.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.checkConfirm.setImageResource(R.drawable.ic_check)
                            binding.checkConfirm.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)

                            binding.icProcess.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.checkProcess.setImageResource(R.drawable.ic_check)
                            binding.checkProcess.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.line1.setBackgroundColor(resources.getColor(R.color.ijotua))

                            //status Detail
                            binding.dotWaitConfirm.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.dotConfirm.visibility = View.VISIBLE
                            binding.dotConfirm.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.confirm.visibility = View.VISIBLE
                            binding.confirm.text = "Penjual - ${getDate(receipt.getdtConfirm()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeConfirm.visibility = View.VISIBLE
                            binding.timeConfirm.text = "${getDate(receipt.getdtConfirm()!!.toLong(), "HH:mm")} WIB"
                            binding.detailConfirm.visibility = View.VISIBLE
                            binding.lineConfirm.visibility = View.VISIBLE
                            binding.lineConfirm.setBackgroundColor(resources.getColor(R.color.colorBlack))

                            binding.dotProcess.visibility = View.VISIBLE
                            binding.dotProcess.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.process.visibility = View.VISIBLE
                            binding.process.text = "Penjual - ${getDate(receipt.getdtProcess()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeProcess.visibility = View.VISIBLE
                            binding.timeProcess.text = "${getDate(receipt.getdtProcess()!!.toLong(), "HH:mm")} WIB"
                            binding.detailProcess.visibility = View.VISIBLE
                            binding.lineProcess.visibility = View.VISIBLE
                            binding.lineProcess.setBackgroundColor(resources.getColor(R.color.ijotua))
                        }

                        receipt.getStatus()
                            .equals("Pengantaran") -> {
                            binding.tvStatus.text = "Dalam Pengantaran!"

                            binding.icConfirm.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.checkConfirm.setImageResource(R.drawable.ic_check)
                            binding.checkConfirm.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)

                            binding.icProcess.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.checkProcess.setImageResource(R.drawable.ic_check)
                            binding.checkProcess.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.line1.setBackgroundColor(resources.getColor(R.color.ijotua))

                            binding.icShipping.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.checkShipping.setImageResource(R.drawable.ic_check)
                            binding.checkShipping.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.line2.setBackgroundColor(resources.getColor(R.color.ijotua))

                            //status Detail
                            binding.dotWaitConfirm.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.dotConfirm.visibility = View.VISIBLE
                            binding.dotConfirm.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.confirm.visibility = View.VISIBLE
                            binding.confirm.text = "Penjual - ${getDate(receipt.getdtConfirm()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeConfirm.visibility = View.VISIBLE
                            binding.timeConfirm.text = "${getDate(receipt.getdtConfirm()!!.toLong(), "HH:mm")} WIB"
                            binding.detailConfirm.visibility = View.VISIBLE
                            binding.lineConfirm.visibility = View.VISIBLE
                            binding.lineConfirm.setBackgroundColor(resources.getColor(R.color.colorBlack))

                            binding.dotProcess.visibility = View.VISIBLE
                            binding.dotProcess.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.process.visibility = View.VISIBLE
                            binding.process.text = "Penjual - ${getDate(receipt.getdtProcess()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeProcess.visibility = View.VISIBLE
                            binding.timeProcess.text = "${getDate(receipt.getdtProcess()!!.toLong(), "HH:mm")} WIB"
                            binding.detailProcess.visibility = View.VISIBLE
                            binding.lineProcess.visibility = View.VISIBLE
                            binding.lineProcess.setBackgroundColor(resources.getColor(R.color.colorBlack))

                            binding.dotShipping.visibility = View.VISIBLE
                            binding.dotShipping.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.shipping.visibility = View.VISIBLE
                            binding.shipping.text = "Penjual - ${getDate(receipt.getdtDelivery()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeShipping.visibility = View.VISIBLE
                            binding.timeShipping.text = "${getDate(receipt.getdtDelivery()!!.toLong(), "HH:mm")} WIB"
                            binding.detailShipping.visibility = View.VISIBLE
                            binding.lineShipping.visibility = View.VISIBLE
                            binding.lineShipping.setBackgroundColor(resources.getColor(R.color.ijotua))
                        }

                        receipt.getStatus()
                            .equals("Sampai") -> {
                            binding.tvStatus.text = "Telah Sampai!"

                            binding.icConfirm.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.checkConfirm.setImageResource(R.drawable.ic_check)
                            binding.checkConfirm.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)

                            binding.icProcess.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.checkProcess.setImageResource(R.drawable.ic_check)
                            binding.checkProcess.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.line1.setBackgroundColor(resources.getColor(R.color.ijotua))

                            binding.icShipping.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.checkShipping.setImageResource(R.drawable.ic_check)
                            binding.checkShipping.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.line2.setBackgroundColor(resources.getColor(R.color.ijotua))
                            binding.line3.setBackgroundColor(resources.getColor(R.color.ijotua))
                            binding.icFinish.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)

                            //status Detail
                            binding.dotWaitConfirm.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.dotConfirm.visibility = View.VISIBLE
                            binding.dotConfirm.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.confirm.visibility = View.VISIBLE
                            binding.confirm.text = "Penjual - ${getDate(receipt.getdtConfirm()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeConfirm.visibility = View.VISIBLE
                            binding.timeConfirm.text = "${getDate(receipt.getdtConfirm()!!.toLong(), "HH:mm")} WIB"
                            binding.detailConfirm.visibility = View.VISIBLE
                            binding.lineConfirm.visibility = View.VISIBLE
                            binding.lineConfirm.setBackgroundColor(resources.getColor(R.color.colorBlack))

                            binding.dotProcess.visibility = View.VISIBLE
                            binding.dotProcess.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.process.visibility = View.VISIBLE
                            binding.process.text = "Penjual - ${getDate(receipt.getdtProcess()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeProcess.visibility = View.VISIBLE
                            binding.timeProcess.text = "${getDate(receipt.getdtProcess()!!.toLong(), "HH:mm")} WIB"
                            binding.detailProcess.visibility = View.VISIBLE
                            binding.lineProcess.visibility = View.VISIBLE
                            binding.lineProcess.setBackgroundColor(resources.getColor(R.color.colorBlack))

                            binding.dotShipping.visibility = View.VISIBLE
                            binding.dotShipping.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.shipping.visibility = View.VISIBLE
                            binding.shipping.text = "Penjual - ${getDate(receipt.getdtDelivery()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeShipping.visibility = View.VISIBLE
                            binding.timeShipping.text = "${getDate(receipt.getdtDelivery()!!.toLong(), "HH:mm")} WIB"
                            binding.detailShipping.visibility = View.VISIBLE
                            binding.lineShipping.visibility = View.VISIBLE
                            binding.lineShipping.setBackgroundColor(resources.getColor(R.color.colorBlack))

                            binding.dotArrived.visibility = View.VISIBLE
                            binding.dotArrived.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.arrived.visibility = View.VISIBLE
                            binding.arrived.text = "Penjual - ${getDate(receipt.getdtArrived()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeArrived.visibility = View.VISIBLE
                            binding.timeArrived.text = "${getDate(receipt.getdtArrived()!!.toLong(), "HH:mm")} WIB"
                            binding.detailArrived.visibility = View.VISIBLE
                            binding.lineArrived.visibility = View.VISIBLE
                            binding.lineArrived.setBackgroundColor(resources.getColor(R.color.ijotua))
                        }

                        receipt.getStatus()
                            .equals("Selesai") -> {
                            binding.tvStatus.text = "Selesai"

                            binding.icConfirm.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.checkConfirm.setImageResource(R.drawable.ic_check)
                            binding.checkConfirm.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)

                            binding.icProcess.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.checkProcess.setImageResource(R.drawable.ic_check)
                            binding.checkProcess.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.line1.setBackgroundColor(resources.getColor(R.color.ijotua))

                            binding.icShipping.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.checkShipping.setImageResource(R.drawable.ic_check)
                            binding.checkShipping.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.line2.setBackgroundColor(resources.getColor(R.color.ijotua))

                            binding.icFinish.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.checkFinish.setImageResource(R.drawable.ic_check)
                            binding.checkFinish.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.line3.setBackgroundColor(resources.getColor(R.color.ijotua))

                            //status Detail
                            binding.dotWaitConfirm.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.dotConfirm.visibility = View.VISIBLE
                            binding.dotConfirm.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.confirm.visibility = View.VISIBLE
                            binding.confirm.text = "Penjual - ${getDate(receipt.getdtConfirm()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeConfirm.visibility = View.VISIBLE
                            binding.timeConfirm.text = "${getDate(receipt.getdtConfirm()!!.toLong(), "HH:mm")} WIB"
                            binding.detailConfirm.visibility = View.VISIBLE
                            binding.lineConfirm.visibility = View.VISIBLE
                            binding.lineConfirm.setBackgroundColor(resources.getColor(R.color.colorBlack))

                            binding.dotProcess.visibility = View.VISIBLE
                            binding.dotProcess.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.process.visibility = View.VISIBLE
                            binding.process.text = "Penjual - ${getDate(receipt.getdtProcess()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeProcess.visibility = View.VISIBLE
                            binding.timeProcess.text = "${getDate(receipt.getdtProcess()!!.toLong(), "HH:mm")} WIB"
                            binding.detailProcess.visibility = View.VISIBLE
                            binding.lineProcess.visibility = View.VISIBLE
                            binding.lineProcess.setBackgroundColor(resources.getColor(R.color.colorBlack))

                            binding.dotShipping.visibility = View.VISIBLE
                            binding.dotShipping.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.shipping.visibility = View.VISIBLE
                            binding.shipping.text = "Penjual - ${getDate(receipt.getdtDelivery()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeShipping.visibility = View.VISIBLE
                            binding.timeShipping.text = "${getDate(receipt.getdtDelivery()!!.toLong(), "HH:mm")} WIB"
                            binding.detailShipping.visibility = View.VISIBLE
                            binding.lineShipping.visibility = View.VISIBLE
                            binding.lineShipping.setBackgroundColor(resources.getColor(R.color.colorBlack))

                            binding.dotArrived.visibility = View.VISIBLE
                            binding.dotArrived.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.arrived.visibility = View.VISIBLE
                            binding.arrived.text = "Penjual - ${getDate(receipt.getdtArrived()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeArrived.visibility = View.VISIBLE
                            binding.timeArrived.text = "${getDate(receipt.getdtArrived()!!.toLong(), "HH:mm")} WIB"
                            binding.detailArrived.visibility = View.VISIBLE
                            binding.lineArrived.visibility = View.VISIBLE
                            binding.lineArrived.setBackgroundColor(resources.getColor(R.color.colorBlack))

                            binding.dotBuyerFinish.visibility = View.VISIBLE
                            binding.dotBuyerFinish.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                            binding.buyerFinish.visibility = View.VISIBLE
                            binding.buyerFinish.text = "Pembeli - ${getDate(receipt.getdtFinish()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeBuyerFinish.visibility = View.VISIBLE
                            binding.timeBuyerFinish.text = "${getDate(receipt.getdtFinish()!!.toLong(), "HH:mm")} WIB"
                            binding.detailBuyerFinish.visibility = View.VISIBLE
                            binding.lineBuyerFinish.visibility = View.VISIBLE
                            binding.lineBuyerFinish.setBackgroundColor(resources.getColor(R.color.colorBlack))

                            binding.dotFinish.visibility = View.VISIBLE
                            binding.dotFinish.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                            binding.finish.visibility = View.VISIBLE
                            binding.finish.text = "Sistem Otomatis - ${getDate(receipt.getdtFinish()!!.toLong(), "dd MMM yyyy")}"
                            binding.timeFinish.visibility = View.VISIBLE
                            binding.timeFinish.text = "${getDate(receipt.getdtFinish()!!.toLong(), "HH:mm")} WIB"
                            binding.detailFinish.visibility = View.VISIBLE
                            binding.lineFinish.visibility = View.VISIBLE
                            binding.lineFinish.setBackgroundColor(resources.getColor(R.color.ijotua))
                        }

                        else -> {
                            binding.tvStatus.text = receipt.getStatus()
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}