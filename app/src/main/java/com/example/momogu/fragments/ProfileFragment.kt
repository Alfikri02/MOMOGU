@file:Suppress("DEPRECATION")

package com.example.momogu.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momogu.adapter.FavoriteAdapter
import com.example.momogu.adapter.PostImagesAdapter
import com.example.momogu.AddPostActivity
import com.example.momogu.EditProfileActivity
import com.example.momogu.model.PostModel
import com.example.momogu.model.UserModel
import com.example.momogu.databinding.FragmentProfileBinding
import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import com.example.momogu.R
import com.example.momogu.utils.Helper

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var firebaseUser: FirebaseUser
    private var postList: MutableList<PostModel>? = null
    private var postImagesAdapter: PostImagesAdapter? = null
    private var postListSaved: MutableList<PostModel>? = null
    private var favoriteAdapter: FavoriteAdapter? = null
    private var mySavedImg: List<String>? = null
    private var isFragmentAttached = false

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

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
        favoriteAdapter =
            context?.let { FavoriteAdapter(it, postListSaved as ArrayList<PostModel>) }
        recyclerViewSaveImages.adapter = favoriteAdapter

        recyclerViewSaveImages.visibility = View.GONE
        recyclerViewUploadImages.visibility = View.VISIBLE

        coroutineScope.launch {
            userInfo()
            numberPhoto()
            numberFavorite()
            myPhotos()
            mySaves()
        }

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
            coroutineScope.launch {
                addVal()
            }
        }

        binding.receipt.setOnClickListener {
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ReceiptFragment()).commit()
        }

        binding.proImageProfileFrag.setOnClickListener {
            coroutineScope.launch {
                profileImage()
            }
        }

        return binding.root
    }

    private suspend fun addVal() {
        if (!isFragmentAttached) {
            return
        }

        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        val dataSnapshot = usersRef.get().await()

        val user = dataSnapshot.getValue(UserModel::class.java)
        if (user?.getAddress().isNullOrEmpty() ||
            user?.getCity().isNullOrEmpty() ||
            user?.getImage().isNullOrEmpty()
        ) {
            activity?.let {
                Helper.showDialogInfo(
                    it,
                    "Tambahkan foto profil dan alamat lengkap untuk melanjutkan!",
                    Gravity.CENTER
                )
            }
        } else {
            activity?.let { startActivity(Intent(it, AddPostActivity::class.java)) }
        }
    }

    private suspend fun profileImage() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        val dataSnapshot = usersRef.get().await()

        if (dataSnapshot.exists()) {
            val user = dataSnapshot.getValue(UserModel::class.java)

            withContext(Dispatchers.Main) {
                val mBuilder = AlertDialog.Builder(requireContext())
                val mView = layoutInflater.inflate(R.layout.dialog_layout_image, null)

                val imageView = mView.findViewById<PhotoView>(R.id.imageView)
                Picasso.get().load(user!!.getImage()).into(imageView)

                mBuilder.setView(mView)
                val mDialog = mBuilder.create()
                mDialog.show()
            }
        }
    }

    private suspend fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        val dataSnapshot = usersRef.get().await()

        if (dataSnapshot.exists()) {
            val user = dataSnapshot.getValue(UserModel::class.java)

            withContext(Dispatchers.Main) {
                if (user!!.getImage().isNullOrEmpty()) {
                    binding.proImageProfileFrag.setImageResource(R.drawable.profile)
                } else {
                    Picasso.get().load(user.getImage()).placeholder(R.drawable.profile)
                        .into(binding.proImageProfileFrag)
                }

                binding.etFullnameProfile.text = user.getFullname()
                binding.etCityProfile.text = user.getCity()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun myPhotos() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        coroutineScope.launch {
            try {
                val dataSnapshot = postRef.get().await()
                val tempList = ArrayList<PostModel>()

                for (snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(PostModel::class.java)

                    if (post != null && post.getPublisher() == firebaseUser.uid) {
                        tempList.add(post)
                    }

                    tempList.sortedByDescending { it.getDateTime() }

                    withContext(Dispatchers.Main) {
                        postList?.clear()
                        postList?.addAll(tempList)
                        postImagesAdapter?.notifyDataSetChanged()
                    }
                }
            }catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    private suspend fun numberPhoto() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        val dataSnapshot = postsRef.get().await()

        if (dataSnapshot.exists()) {
            val postCounter = dataSnapshot.children.count { it.getValue(PostModel::class.java)?.getPublisher() == firebaseUser.uid }

            withContext(Dispatchers.Main) {
                binding.totalPosts.text = postCounter.toString()
            }
        }
    }

    private suspend fun numberFavorite() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)
        val dataSnapshot = postsRef.get().await()

        if (dataSnapshot.exists()) {
            val favoriteCounter = dataSnapshot.children.count()

            withContext(Dispatchers.Main) {
                binding.totalFavorite.text = favoriteCounter.toString()
            }
        }
    }

    private fun mySaves() {
        mySavedImg = ArrayList()

        val savedRef =
            FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)

        savedRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for (snapshot in p0.children) {
                        (mySavedImg as ArrayList<String>).add(snapshot.key!!)
                    }

                    readSavedImagesData()
                    favoriteAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun readSavedImagesData() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        coroutineScope.launch {
            try {
                val dataSnapshot = postsRef.get().await()
                val tempList = ArrayList<PostModel>()

                for (snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(PostModel::class.java)

                    for (key in mySavedImg!!) {
                        if (post?.getPostid() == key) {
                            tempList.add(post)
                        }
                    }
                }

                tempList.sortedByDescending { it.getDateTime() }

                withContext(Dispatchers.Main) {
                    postListSaved?.clear()
                    postListSaved?.addAll(tempList)
                    favoriteAdapter?.notifyDataSetChanged()
                }
            }catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()

        coroutineScope.cancel()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()

        coroutineScope.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()

        coroutineScope.cancel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isFragmentAttached = true
    }

    override fun onDetach() {
        super.onDetach()
        isFragmentAttached = false
    }
}
