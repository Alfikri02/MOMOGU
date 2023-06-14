package com.example.momogu.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.momogu.*
import com.example.momogu.model.PostModel
import com.example.momogu.model.ReceiptModel
import com.example.momogu.utils.Helper.getDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.DecimalFormat
import java.util.*

class ReceiptAdapter(
    private val mContext: Context,
    private val mReceipt: List<ReceiptModel>
) : RecyclerView.Adapter<ReceiptAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.post_image_receipt)
        val date: TextView = itemView.findViewById(R.id.date_receipt)
        val status: TextView = itemView.findViewById(R.id.tv_status)
        val product: TextView = itemView.findViewById(R.id.product_receipt)
        val total: TextView = itemView.findViewById(R.id.tv_total_receipt)
        val cvStatus: CardView = itemView.findViewById(R.id.cv_status)
        val menu: ImageView = itemView.findViewById(R.id.menu_receipt)
        val cvBtnStatus: CardView = itemView.findViewById(R.id.cv_BtnStatus)
        val tvBtnStatus: TextView =  itemView.findViewById(R.id.btn_status)
        val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.receipt_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mReceipt.size
    }

    @SuppressLint("DiscouragedPrivateApi", "SetTextI18n", "ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receipt = mReceipt[position]

        when {
            receipt.getStatus()
                .equals("Dikonfirmasi") -> {
                holder.status.text = "Pesanan Dikonfirmasi!"
                holder.status.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.white
                    )
                )
                holder.cvStatus.setCardBackgroundColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.pastel
                    )
                )
            }

            receipt.getStatus()
                .equals("Diproses") -> {
                holder.status.text = "Pesanan Diproses!"
                holder.status.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.white
                    )
                )
                holder.cvStatus.setCardBackgroundColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.ijomuda
                    )
                )
            }

            receipt.getStatus()
                .equals("Pengantaran") -> {
                holder.status.text = "Dalam Pengantaran!"
                holder.status.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.ijotua
                    )
                )
                holder.cvStatus.setCardBackgroundColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.tea
                    )
                )
            }

            receipt.getStatus()
                .equals("Sampai") -> {
                holder.status.text = "Telah Sampai!"
                holder.status.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.ijotua
                    )
                )
                holder.cvStatus.setCardBackgroundColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.tea
                    )
                )
            }

            receipt.getStatus()
                .equals("Selesai") -> {
                holder.status.text = "Selesai"
                holder.status.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.white
                    )
                )
                holder.cvStatus.setCardBackgroundColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.ijotua
                    )
                )
                holder.tvBtnStatus.text = "Beli Lagi"
            }

            else -> {
                holder.status.text = receipt.getStatus()
            }
        }

        holder.date.text = getDate(receipt.getDateTime()!!.toLong(), "dd MMM yyyy")
        getPostImage(holder.postImage, holder.product, holder.total, receipt.getPostId()!!)

        if (receipt.getSellerId().equals(holder.firebaseUser.uid)) {
            holder.menu.visibility = View.VISIBLE
        } else {
            holder.menu.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (receipt.getStatus().equals("Selesai")) {
                val editorPost = mContext.getSharedPreferences("POST", Context.MODE_PRIVATE).edit()
                editorPost.putString("postid", receipt.getPostId())
                editorPost.apply()

                mContext.startActivity(Intent(mContext, ReceiptPostActivity::class.java))

            } else if (receipt.getSellerId().equals(holder.firebaseUser.uid)) {
                val editorPost = mContext.getSharedPreferences("POST", Context.MODE_PRIVATE).edit()
                editorPost.putString("postid", receipt.getPostId())
                editorPost.apply()

                mContext.startActivity(Intent(mContext, ReceiptUserActivity::class.java))
            } else {
                val editorPost = mContext.getSharedPreferences("POST", Context.MODE_PRIVATE).edit()
                editorPost.putString("postid", receipt.getPostId())
                editorPost.apply()

                mContext.startActivity(Intent(mContext, ReceiptPostActivity::class.java))
            }
        }

        holder.cvBtnStatus.setOnClickListener {
            if (receipt.getStatus().equals("Selesai")){
                mContext.startActivity(Intent(mContext, MainActivity::class.java))
            } else {
                val editorPost = mContext.getSharedPreferences("POST", Context.MODE_PRIVATE).edit()
                editorPost.putString("postid", receipt.getPostId())
                editorPost.apply()

                mContext.startActivity(Intent(mContext, StatusActivity::class.java))
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

                    Picasso.get().load(post?.getPostimage()).placeholder(R.drawable.ic_momogu_text_bottom)
                        .into(imageView)

                    product.text = post?.getProduct()

                    val price = post?.getPrice()
                    val getPrice = price?.replace(".", "")?.replace(",", "")?.toInt()

                    val shipping = post?.getShipping()
                    val getShipping = shipping?.replace(".", "")?.replace(",", "")?.toInt()

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

}