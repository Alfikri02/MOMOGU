package com.example.momogu.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.momogu.FavoriteActivity
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.ReceiptModel
import com.example.momogu.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class FavoriteAdapter(private val mContext: Context, mPost: List<PostModel>) :
    RecyclerView.Adapter<FavoriteAdapter.ViewHolder?>() {
    private var mPost: List<PostModel>? = null

    init {
        this.mPost = mPost
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var postImage: ImageView
        var soldView: RelativeLayout
        var soldTitle: TextView

        init {
            postImage = itemView.findViewById(R.id.post_image)
            soldView = itemView.findViewById(R.id.layoutSoldView)
            soldTitle = itemView.findViewById(R.id.soldTitle)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.images_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.soldView.visibility = View.GONE
        val post: PostModel = mPost!![position]

        Picasso.get().load(post.getPostimage()).into(holder.postImage)

        receiptVal(holder, position)

        holder.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("POST", Context.MODE_PRIVATE).edit()

            editor.putString("postid", post.getPostid())
            editor.apply()
            mContext.startActivity(Intent(mContext, FavoriteActivity::class.java))
        }
    }

    private fun receiptVal(holder: ViewHolder, position: Int) {
        val post = mPost!![position]

        val receiptRef =
            FirebaseDatabase.getInstance().reference.child("Receipt").child(post.getPostid()!!)
        receiptRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val receipt = p0.getValue(ReceiptModel::class.java)

                    if (receipt!!.getStatus().equals("Selesai")) {
                        holder.soldView.visibility = View.VISIBLE
                    } else {
                        holder.soldTitle.text = "Transaksi sedang berlangsung!"
                        holder.soldView.visibility = View.VISIBLE
                    }
                } else {
                    holder.soldView.visibility = View.GONE
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

}