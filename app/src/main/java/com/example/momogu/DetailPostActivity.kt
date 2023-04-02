package com.example.momogu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.UserModel
import com.example.momogu.databinding.ActivityDetailPostBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class DetailPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPostBinding
    private var postId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val preferences = this.getSharedPreferences("POST", Context.MODE_PRIVATE)
        if (preferences != null) {
            postId = preferences.getString("postid", "none")!!
        }

        retrievePosts()

        binding.closeDetail.setOnClickListener {
            finish()
        }

        binding.profileDetail.setOnClickListener {
            startActivity(Intent(this, BreederActivity::class.java))
            finish()
        }

        binding.btnBuy.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(PostModel::class.java)

                    Picasso.get().load(post!!.getPostimage()).placeholder(R.drawable.profile)
                        .into(binding.imagePost)
                    binding.productDetail.text = post.getProduct()
                    binding.priceDetail.text = "Rp. ${post.getPrice()}"
                    binding.dateDetail.text = getDate(post.getDateTime()!!.toLong(), "dd/MM/yyyy")
                    binding.etWeight.text = "${post.getWeight()} KG"
                    binding.etGender.text = post.getGender()
                    binding.etAge.text = "${post.getAge()} Bulan"
                    binding.etColor.text = post.getColor()
                    binding.etDesc.setText(post.getDesc())
                    publisherInfo(post.getPublisher())
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun publisherInfo(publisherId: String?) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId!!)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UserModel::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(binding.userProfile)
                    binding.tvFullnameDetail.text = user.getFullname()
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