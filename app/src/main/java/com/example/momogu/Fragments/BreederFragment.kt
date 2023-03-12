package com.example.momogu.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momogu.Adapter.MyImagesAdapter
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.UserModel
import com.example.momogu.R
import com.example.momogu.databinding.FragmentBreederBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList

class BreederFragment : Fragment() {

    private lateinit var binding: FragmentBreederBinding

    private var postId: String = ""

    var postList: List<PostModel>? = null
    var myImagesAdapter: MyImagesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBreederBinding.inflate(inflater)

        val preferences = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (preferences != null) {
            postId = preferences.getString("postId", "none")!!
        }


        //Recycler View for Uploaded Images
        val recyclerViewUploadImages: RecyclerView = binding.recyclerViewUploadPic
        recyclerViewUploadImages.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadImages.layoutManager = linearLayoutManager

        postList = ArrayList()
        myImagesAdapter = context?.let { MyImagesAdapter(it, postList as ArrayList<PostModel>) }
        recyclerViewUploadImages.adapter = myImagesAdapter

        retrievePosts()
        myPhotos()
        getTotalNumberOfPhotos()

        return binding.root
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(PostModel::class.java)

                    publisherInfo(post?.getPublisher())
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
                        .into(binding.proImageProfileFrag)
                    binding.profileFragmentUsername.text = user.getUsername()
                    binding.etFullnameProfile.text = user.getFullname()
                    binding.etCityProfile.text = user.getCity()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun myPhotos() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SuspiciousIndentation")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(PostModel::class.java)
                        (postList as ArrayList<PostModel>).sortByDescending { it.getDateTime() }
                        (postList as ArrayList<PostModel>).add(post!!)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun getTotalNumberOfPhotos() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    var postCounter = 0

                    for (snapShot in p0.children) {
                        postCounter++
                    }

                    binding.totalPosts.text = postCounter.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

}