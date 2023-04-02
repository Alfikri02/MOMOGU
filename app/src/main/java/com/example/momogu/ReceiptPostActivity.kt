package com.example.momogu

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.UserModel
import com.example.momogu.databinding.ActivityReceiptPostBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

class ReceiptPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiptPostBinding
    private var postId: String = ""
    private lateinit var profileId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptPostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val preferences = this.getSharedPreferences("POST", Context.MODE_PRIVATE)
        if (preferences != null) {
            postId = preferences.getString("postid", "none")!!
        }

        retrievePosts()

        val pref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "none").toString()
        }

        userInfo()

        binding.backCheckout.setOnClickListener {
            finish()
        }

    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(PostModel::class.java)

                    Picasso.get().load(post!!.getPostimage()).placeholder(R.drawable.momogu)
                        .into(binding.imagePost)
                    binding.tvProduct.text = post.getProduct()
                    binding.tvPrice.text = "Rp. ${post.getPrice()}"
                    binding.tvWeight.text = "${post.getWeight()} KG"
                    binding.tvPriceItems.text = "Rp. ${post.getPrice()}"
                    binding.tvPriceShipping.text = "Rp. ${post.getShipping()}"

                    val price = post.getPrice()
                    val getPrice = price?.replace(".", "")?.toInt()

                    val shipping = post.getShipping()
                    val getShipping = shipping?.replace(".", "")?.toInt()

                    val decimalFormat = DecimalFormat("#,##0")
                    val hasil = getPrice!! + getShipping!!
                    val formatNumber = hasil.toString().replace(".", "").replace(",", "")

                    val formatted = decimalFormat.format(formatNumber.toDouble())

                    binding.tvTotal.text = "Rp. $formatted"


                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UserModel::class.java)

                    binding.tvCity.text = user?.getCity()
                    binding.tvAddress.text = user?.getAddress()
                    binding.tvWhatsapp.text = user?.getWa()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}