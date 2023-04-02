@file:Suppress("DEPRECATION")

package com.example.momogu.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momogu.EditProfileActivity
import com.example.momogu.Adapter.PostImagesAdapter
import com.example.momogu.AddPostActivity
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.UserModel
import com.example.momogu.R
import com.example.momogu.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser
    var postList: List<PostModel>? = null
    var postImagesAdapter: PostImagesAdapter? = null
    var postListSaved: List<PostModel>? = null
    var postImagesAdapterSavedImg: PostImagesAdapter? = null
    var mySavedImg: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "none").toString()
        }

        //Recycler View for Uploaded Images
        val recyclerViewUploadImages: RecyclerView = binding.recyclerViewUploadPic
        recyclerViewUploadImages.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadImages.layoutManager = linearLayoutManager

        postList = ArrayList()
        postImagesAdapter = context?.let { PostImagesAdapter(it, postList as ArrayList<PostModel>) }
        recyclerViewUploadImages.adapter = postImagesAdapter

        //Recycler View for Saved Images
        val recyclerViewSaveImages: RecyclerView = binding.recyclerViewSavedPic
        recyclerViewSaveImages.setHasFixedSize(true)
        val linearLayoutManager2: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewSaveImages.layoutManager = linearLayoutManager2

        postListSaved = ArrayList()
        postImagesAdapterSavedImg =
            context?.let { PostImagesAdapter(it, postListSaved as ArrayList<PostModel>) }
        recyclerViewSaveImages.adapter = postImagesAdapterSavedImg

        recyclerViewSaveImages.visibility = View.GONE
        recyclerViewUploadImages.visibility = View.VISIBLE

        userInfo()
        numberPhoto()
        numberFavorite()
        myPhotos()
        mySaves()

        val uploadImagesBtn: ImageButton = binding.imagesGridViewBtn
        val savedImagesBtn: ImageButton = binding.imagesSaveBtn

        uploadImagesBtn.setOnClickListener {
            uploadImagesBtn.setColorFilter(resources.getColor(R.color.blackColor))
            savedImagesBtn.setColorFilter(resources.getColor(R.color.colorBlack))
            recyclerViewSaveImages.visibility = View.GONE
            recyclerViewUploadImages.visibility = View.VISIBLE
            binding.totalFavorite.visibility = View.GONE
            binding.totalPosts.visibility = View.VISIBLE
            binding.btnAdd.visibility = View.VISIBLE
        }

        savedImagesBtn.setOnClickListener {
            uploadImagesBtn.setColorFilter(resources.getColor(R.color.colorBlack))
            savedImagesBtn.setColorFilter(resources.getColor(R.color.blackColor))
            recyclerViewSaveImages.visibility = View.VISIBLE
            recyclerViewUploadImages.visibility = View.GONE
            binding.totalFavorite.visibility = View.VISIBLE
            binding.totalPosts.visibility = View.GONE
            binding.btnAdd.visibility = View.GONE
        }

        binding.editAccountSettingsBtn.setOnClickListener {
            startActivity(Intent(context, EditProfileActivity::class.java))
        }
        
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(context, AddPostActivity::class.java))
        }

        binding.receipt.setOnClickListener {
            (context as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ReceiptFragment()).commit()
        }

        return binding.root
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UserModel::class.java)

                    Picasso.get().load(user?.getImage()).placeholder(R.drawable.profile)
                        .into(binding.proImageProfileFrag)
                    binding.profileFragmentUsername.text = user?.getUsername()
                    binding.etFullnameProfile.text = user?.getFullname()
                    binding.etCityProfile.text = user?.getCity()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun myPhotos() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (postList as ArrayList<PostModel>).clear()

                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(PostModel::class.java)

                        if (post?.getPublisher().equals(profileId)) {
                            (postList as ArrayList<PostModel>).sortByDescending { it.getDateTime() }
                            (postList as ArrayList<PostModel>).add(post!!)
                        }
                        postImagesAdapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun numberPhoto() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    var postCounter = 0

                    for (snapShot in p0.children) {
                        val post = snapShot.getValue(PostModel::class.java)

                        if (post?.getPublisher() == profileId) {
                            postCounter++
                        }
                    }

                    binding.totalPosts.text = postCounter.toString()//" $postCounter"
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun numberFavorite() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Saves").child(profileId)

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    var favoriteCounter = 0

                    for (snapShot in p0.children) {
                            favoriteCounter++
                    }

                    binding.totalFavorite.text = favoriteCounter.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun mySaves() {
        mySavedImg = ArrayList()

        val savedRef =
            FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)

        savedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for (snapshot in p0.children) {
                        (mySavedImg as ArrayList<String>).add(snapshot.key!!)
                    }

                    readSavedImagesData()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun readSavedImagesData() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (postListSaved as ArrayList<PostModel>).clear()

                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(PostModel::class.java)

                        for (key in mySavedImg!!) {
                            if (post?.getPostid() == key) {
                                (postListSaved as ArrayList<PostModel>).sortByDescending { it.getDateTime() }
                                (postListSaved as ArrayList<PostModel>).add(post)
                            }
                        }
                    }

                    postImagesAdapterSavedImg?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }


    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }
}