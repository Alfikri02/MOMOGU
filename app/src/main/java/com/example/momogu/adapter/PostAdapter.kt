package com.example.momogu.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.Gravity
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
import com.example.momogu.model.PostModel
import com.example.momogu.model.ReceiptModel
import com.example.momogu.model.UserModel
import com.example.momogu.R
import com.example.momogu.utils.Helper
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
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

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.post_image_home)
        val saveButton: ImageView = itemView.findViewById(R.id.post_save_comment_btn)
        val timeAgo: TextView = itemView.findViewById(R.id.lblTimeAgo)
        val product: TextView = itemView.findViewById(R.id.tv_product)
        val price: TextView = itemView.findViewById(R.id.tv_price)
        val weight: TextView = itemView.findViewById(R.id.tv_weight)
        val location: TextView = itemView.findViewById(R.id.tv_location)
        val cardPost: CardView = itemView.findViewById(R.id.cardPost)
        val soldView: RelativeLayout = itemView.findViewById(R.id.layoutSoldView)
        val soldTitle: TextView = itemView.findViewById(R.id.soldTitle)

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
        holder.soldView.visibility = View.GONE

        val post = mPost[position]
        Picasso.get().load(post.getPostimage()).into(holder.postImage)
        checkSavedStatus(post.getPostid()!!, holder.saveButton)
        locationInfo(holder.location, post.getPublisher())

        holder.product.text = post.getProduct()
        holder.price.text = "Rp. ${post.getPrice()}"
        holder.weight.text = "${post.getWeight()} KG"

        holder.timeAgo.text = TimeAgo.using(
            post.getDateTime()!!.toLong(),
            TimeAgoMessages.Builder().withLocale(Locale("in")).build()
        )

        holder.cardPost.setOnClickListener {
            postVal(position)
        }

        receiptVal(holder, position)

        holder.saveButton.setOnClickListener {
            savedStatus(holder, position)
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

    private fun savedStatus(holder: ViewHolder, position: Int) {
        val post = mPost[position]
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

    private fun checkSavedStatus(postid: String, imageView: ImageView) {
        val savesRef = FirebaseDatabase.getInstance().reference.child("Saves")
            .child(firebaseUser!!.uid)

        savesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(postid).exists()) {
                    imageView.setImageResource(R.drawable.ic_favorite)
                    imageView.tag = "Saved"
                } else {
                    imageView.setImageResource(R.drawable.ic_favorite_broder)
                    imageView.tag = "Save"
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun postVal(position: Int) {
        val post = mPost[position]

        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UserModel::class.java)
                    if (
                        user!!.getAddress().isNullOrEmpty() ||
                        user.getCity().isNullOrEmpty() ||
                        user.getImage().isNullOrEmpty()
                    ) {
                        Helper.showDialogInfo(
                            mContext,
                            "Tambahkan foto profil dan alamat lengkap untuk melanjutkan!",
                            Gravity.CENTER
                        )
                    } else if (post.getPublisher().equals(firebaseUser!!.uid)) {
                        Toast.makeText(mContext, "Sapi ini milik anda!", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        val editor =
                            mContext.getSharedPreferences("POST", Context.MODE_PRIVATE).edit()
                        editor.putString("postid", post.getPostid())
                        editor.apply()
                        mContext.startActivity(Intent(mContext, DetailPostActivity::class.java))
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun receiptVal(holder: ViewHolder, position: Int) {
        val post = mPost[position]

        val receiptRef =
            FirebaseDatabase.getInstance().reference.child("Receipt").child(post.getPostid()!!)
        receiptRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
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