package com.example.momogu.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.momogu.DetailPostActivity
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.ReceiptModel
import com.example.momogu.Model.UserModel
import com.example.momogu.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.*


class PostAdapter(private val mContext: Context, private val mPost: List<PostModel>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    private var firebaseUser: FirebaseUser? = null
    private val secondMillis = 1000
    private val minuteMillis = 60 * secondMillis
    private val hourMillis = 60 * minuteMillis
    private val dayMillis = 24 * hourMillis

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var postImage: ImageView
        var saveButton: ImageView
        var timeAgo: TextView
        var product: TextView
        var price: TextView
        var weight: TextView
        var location: TextView
        var cardPost: CardView
        var soldView: RelativeLayout

        init {
            postImage = itemView.findViewById(R.id.post_image_home)
            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            timeAgo = itemView.findViewById(R.id.lblTimeAgo)
            product = itemView.findViewById(R.id.tv_product)
            price = itemView.findViewById(R.id.tv_price)
            weight = itemView.findViewById(R.id.tv_weight)
            location = itemView.findViewById(R.id.tv_location)
            cardPost = itemView.findViewById(R.id.cardPost)
            soldView = itemView.findViewById(R.id.layoutSoldView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]
        Picasso.get().load(post.getPostimage()).into(holder.postImage)
        checkSavedStatus(post.getPostid()!!, holder.saveButton)
        locationInfo(holder.location, post.getPublisher())

        holder.product.text = post.getProduct()
        holder.price.text = "Rp. ${post.getPrice()}"
        holder.weight.text = "${post.getWeight()} KG"
        holder.timeAgo.text = getTimeAgo(post.getDateTime()!!.toLong())

        holder.cardPost.setOnClickListener {
            if (post.getPublisher().equals(firebaseUser!!.uid)) {
                Toast.makeText(mContext, "Sapi ini milik anda!", Toast.LENGTH_SHORT).show()
            } else {
                val editor = mContext.getSharedPreferences("POST", Context.MODE_PRIVATE).edit()
                editor.putString("postid", post.getPostid())
                editor.apply()
                mContext.startActivity(Intent(mContext, DetailPostActivity::class.java))
            }
        }

        val receiptRef = FirebaseDatabase.getInstance().reference.child("Receipt").child(post.getPostid()!!)
        receiptRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val receipt = p0.getValue(ReceiptModel::class.java)

                    if (receipt!!.getStatus().equals("Selesai")) {
                        holder.soldView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })



        holder.saveButton.setOnClickListener {
            if (post.getPublisher().equals(firebaseUser!!.uid)) {
                Toast.makeText(mContext, "Sapi ini milik anda!", Toast.LENGTH_SHORT).show()
            } else if (holder.saveButton.tag == "Save") {
                FirebaseDatabase.getInstance().reference.child("Saves")
                    .child(firebaseUser!!.uid)
                    .child(post.getPostid()!!)
                    .setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference.child("Saves")
                    .child(firebaseUser!!.uid)
                    .child(post.getPostid()!!)
                    .removeValue()
            }
        }
    }

    private fun getTimeAgo(time: Long): String? {
        var tempTime = time
        val now = System.currentTimeMillis()

        if (tempTime < 1000000000000L) {
            tempTime *= 1000
        }

        if (tempTime > now || tempTime <= 0) {
            return null
        }

        val diff: Int = now.toInt() - tempTime.toInt()

        return when {
            diff < minuteMillis -> {
                "just now"
            }

            diff < 2 * minuteMillis -> {
                "a minute ago"
            }

            diff < 50 * minuteMillis -> {
                val temp = diff / minuteMillis
                "$temp minutes ago"
            }

            diff < 90 * minuteMillis -> {
                "an hour ago"
            }

            diff < 24 * hourMillis -> {
                val temp = diff / hourMillis
                "$temp hours ago"
            }

            diff < 48 * hourMillis -> {
                "yesterday"
            }

            else -> {
                val temp = diff / dayMillis
                "$temp days ago"
            }
        }
    }

    private fun locationInfo(location: TextView, publisherId: String?) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId!!)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UserModel::class.java)

                    location.text = user!!.getCity()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun checkSavedStatus(postid: String, imageView: ImageView) {
        val savesRef = FirebaseDatabase.getInstance().reference.child("Saves")
            .child(firebaseUser!!.uid)

        savesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(postid).exists()) {
                    imageView.setImageResource(R.drawable.favorite)
                    imageView.tag = "Saved"
                } else {
                    imageView.setImageResource(R.drawable.favorite_broder)
                    imageView.tag = "Save"
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}