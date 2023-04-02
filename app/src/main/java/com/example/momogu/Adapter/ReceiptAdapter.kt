package com.example.momogu.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.momogu.*
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.ReceiptModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class ReceiptAdapter(
    private val mContext: Context,
    private val mReceipt: List<ReceiptModel>
) : RecyclerView.Adapter<ReceiptAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var postImage: ImageView
        var date: TextView
        var status: TextView
        var product: TextView
        var total: TextView

        init {
            postImage = itemView.findViewById(R.id.post_image_receipt)
            date = itemView.findViewById(R.id.date_receipt)
            status = itemView.findViewById(R.id.tv_status)
            product = itemView.findViewById(R.id.product_receipt)
            total = itemView.findViewById(R.id.tv_total_receipt)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.receipt_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mReceipt.size
    }

    @SuppressLint("DiscouragedPrivateApi", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receipt = mReceipt[position]

        when {
            receipt.getStatus()
                .equals("Dikonfirmasi") -> {
                holder.status.text = "Dikonfirmasi!"
            }

            receipt.getStatus()
                .equals("Diproses") -> {
                holder.status.text = "Diproses!"
            }

            receipt.getStatus()
                .equals("Dalam Pengantaran") -> {
                holder.status.text = "Dalam Pengantaran!"
            }

            receipt.getStatus()
                .equals("Selesai") -> {
                holder.status.text = "Selesai"
            }

            else -> {
                holder.status.text = receipt.getStatus()
            }
        }

        holder.date.text = getDate(receipt.getDateTime()!!.toLong(), "dd/MM/yyyy")
        getPostImage(holder.postImage, holder.product, holder.total, receipt.getPostId()!!)

        holder.itemView.setOnClickListener {
            if (receipt.getIsPost()) {
                val editor = mContext.getSharedPreferences("POST", Context.MODE_PRIVATE).edit()

                editor.putString("postid", receipt.getPostId())
                editor.apply()
                mContext.startActivity(Intent(mContext, ReceiptPostActivity::class.java))
            } else {
                val editor = mContext.getSharedPreferences("POST", Context.MODE_PRIVATE).edit()

                editor.putString("postid", receipt.getPostId())
                editor.apply()
                mContext.startActivity(Intent(mContext, ReceiptUserActivity::class.java))
            }
        }
    }

    private fun getPostImage(
        imageView: ImageView,
        product: TextView,
        total: TextView,
        postId: String
    ) {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(PostModel::class.java)

                    Picasso.get().load(post?.getPostimage()).placeholder(R.drawable.momogu)
                        .into(imageView)

                    product.text = post?.getProduct()

                    val price = post?.getPrice()
                    val getPrice = price?.replace(".", "")?.toInt()

                    val shipping = post?.getShipping()
                    val getShipping = shipping?.replace(".", "")?.toInt()

                    val decimalFormat = DecimalFormat("#,##0")
                    val hasil = getPrice!! + getShipping!!
                    val formatNumber = hasil.toString().replace(".", "").replace(",", "")

                    val formatted = decimalFormat.format(formatNumber.toDouble())

                    total.text = "Rp. $formatted"

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDate(milliSeconds: Long, dateFormat: String?): String? {
        val formatter = SimpleDateFormat(dateFormat)
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds

        return formatter.format(calendar.time)
    }

}