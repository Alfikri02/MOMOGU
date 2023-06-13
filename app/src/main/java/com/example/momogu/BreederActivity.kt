@file:Suppress("DEPRECATION")

package com.example.momogu

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momogu.Adapter.PostImagesAdapter
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.UserModel
import com.example.momogu.databinding.ActivityBreederBinding
import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BreederActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBreederBinding

    private var postId: String = ""
    var postList: List<PostModel>? = null
    var postImagesAdapter: PostImagesAdapter? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreederBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val preferences = this.getSharedPreferences("POST", Context.MODE_PRIVATE)
        if (preferences != null) {
            postId = preferences.getString("postid", "none")!!
        }

        //Recycler View for Uploaded Images
        val recyclerViewUploadImages: RecyclerView = binding.recyclerViewUploadPic
        recyclerViewUploadImages.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(this, 3)
        recyclerViewUploadImages.layoutManager = linearLayoutManager

        postList = ArrayList()
        postImagesAdapter = PostImagesAdapter(this, postList as ArrayList<PostModel>)
        recyclerViewUploadImages.adapter = postImagesAdapter


        coroutineScope.launch {
            retrievePosts()
            retrieveImage()
        }

        binding.backBreeder.setOnClickListener {
            finish()
        }

        binding.proImageProfileFrag.setOnClickListener {
            coroutineScope.launch {
                retrievePublisherImage()
            }
        }

    }

    private suspend fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        try {
            val snapshot = postsRef.get().await()
            if (snapshot.exists()) {
                val post = snapshot.getValue(PostModel::class.java)
                post?.getPublisher()?.let { publisherInfo(it) }
            }
        } catch (e: Exception) {
            // Handle the exception
        }
    }

    private suspend fun publisherInfo(publisherId: String?) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId!!)

        try {
            val snapshot = usersRef.get().await()
            if (snapshot.exists()) {
                val user = snapshot.getValue(UserModel::class.java)

                withContext(Dispatchers.Main) {
                    if (user!!.getImage().isNullOrEmpty()) {
                        binding.proImageProfileFrag.setImageResource(R.drawable.profile)
                    } else {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile)
                            .into(binding.proImageProfileFrag)
                    }

                    if (user.getStatusOn().equals("Aktif")) {
                        binding.dotStatus.setColorFilter(resources.getColor(R.color.ijotua), PorterDuff.Mode.SRC_IN)
                    } else {
                        binding.dotStatus.setColorFilter(resources.getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN)
                    }

                    binding.etFullnameProfile.text = user.getFullname()
                    binding.etCityProfile.text = user.getCity()
                }
            }
        } catch (e: Exception) {
            // Handle the exception
        }
    }

    private fun retrieveImage() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(PostModel::class.java)

                    myPhotos(post?.getPublisher())
                    numberPhoto(post?.getPublisher())

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun myPhotos(publisherId: String?) {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (postList as ArrayList<PostModel>).clear()

                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(PostModel::class.java)

                        if (post!!.getPublisher().equals(publisherId)) {
                            (postList as ArrayList<PostModel>).sortByDescending { it.getDateTime() }
                            (postList as ArrayList<PostModel>).add(post)
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun numberPhoto(publisherId: String?) {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    var postCounter = 0

                    for (snapShot in p0.children) {
                        val post = snapShot.getValue(PostModel::class.java)

                        if (post?.getPublisher() == publisherId) {
                            postCounter++
                        }
                    }

                    binding.totalPosts.text = postCounter.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private suspend fun retrievePublisherImage() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        try {
            val snapshot = postsRef.get().await()
            if (snapshot.exists()) {
                val post = snapshot.getValue(PostModel::class.java)
                post?.getPublisher()?.let { profileImage(it) }
            }
        } catch (e: Exception) {
            // Handle the exception
        }
    }

    private suspend fun profileImage(publisherId: String?) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId!!)

        try {
            val snapshot = usersRef.get().await()
            if (snapshot.exists()) {
                val user = snapshot.getValue(UserModel::class.java)

                withContext(Dispatchers.Main) {
                    val mBuilder = AlertDialog.Builder(this@BreederActivity)
                    val mView = layoutInflater.inflate(R.layout.dialog_layout_image, null)

                    val imageView = mView.findViewById<PhotoView>(R.id.imageView)
                    Picasso.get().load(user!!.getImage()).into(imageView)

                    mBuilder.setView(mView)
                    val mDialog = mBuilder.create()
                    mDialog.show()
                }
            }
        } catch (e: Exception) {
            // Handle the exception
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}