package com.example.momogu.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.momogu.adapter.PostAdapter
import com.example.momogu.MapsActivity
import com.example.momogu.model.PostModel
import com.example.momogu.model.ReceiptModel
import com.example.momogu.model.UserModel
import com.example.momogu.R
import com.example.momogu.databinding.FragmentHomeBinding
import com.example.momogu.utils.Helper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<PostModel> = ArrayList()
    private lateinit var firebaseUser: FirebaseUser

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private var postCache: MutableMap<String, PostModel> = HashMap()

    private val valueEventListener: ValueEventListener = object : ValueEventListener {
        @SuppressLint("NotifyDataSetChanged")
        override fun onDataChange(snapshot: DataSnapshot) {
            postList.clear()
            postCache.clear()

            for (childSnapshot in snapshot.children) {
                val post = childSnapshot.getValue(PostModel::class.java)
                post?.let {
                    postList.add(it)
                    postCache[childSnapshot.key.toString()] = it
                }
            }

            postList.sortByDescending { it.getDateTime() }
            postAdapter?.notifyDataSetChanged()
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle the error
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        //Recycler View Home
        val recyclerView: RecyclerView = binding.recyclerViewHome
        recyclerView.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(context, 2)
        recyclerView.layoutManager = layoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<PostModel>) }
        recyclerView.adapter = postAdapter

        retrievePosts()
        transVal()
        imageSlider()

        binding.search.queryHint = "Cari sapi yang anda inginkan!"
        binding.search.onActionViewExpanded()
        binding.search.clearFocus()
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(newText: String?): Boolean {
                coroutineScope.launch {
                    searchPost(newText.toString()) // Perform search based on the query
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        binding.cvMaps.setOnClickListener {
            coroutineScope.launch {
                mapsVal()
            }
        }

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun searchPost(input: String) {
        val query = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .orderByChild("product")
            .startAt(input)
            .endAt(input + "\uf8ff")

        try {
            val dataSnapshot = query.get().await()
            postList.clear()

            for (snapshot in dataSnapshot.children) {
                val post = snapshot.getValue(PostModel::class.java)

                if (post != null) {
                    val postTitle = post.javaClass.getDeclaredField("product").apply {
                        isAccessible = true
                    }.get(post) as String

                    if (postTitle.contains(input, ignoreCase = true)) {
                        postList.add(post)
                    }
                }
            }

            postAdapter?.notifyDataSetChanged()
        } catch (e: Exception) {
            // Handle the exception
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun retrievePosts() {
        if (postCache.isNotEmpty()) {
            // Data is already cached, use it
            postList.addAll(postCache.values)
            postList.sortByDescending { it.getDateTime() }
            postAdapter?.notifyDataSetChanged()
        } else {
            coroutineScope.launch {
                val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
                postsRef.addValueEventListener(valueEventListener)
            }
        }
    }


    private fun transVal() {
        val context = requireContext()
        val notificationRef = FirebaseDatabase.getInstance().reference.child("Receipt")

        coroutineScope.launch {
            try {
                val dataSnapshot = notificationRef.get().await()

                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val receipt = snapshot.getValue(ReceiptModel::class.java)

                        if (receipt!!.getSellerId().equals(firebaseUser.uid)) {
                            if (receipt.getStatus().equals("Menunggu konfirmasi!")) {
                                val currentTime = System.currentTimeMillis()
                                val timeCancel = receipt.getdtCancel()!!.toLong()

                                if (currentTime >= timeCancel) {
                                    Helper.showDialogInfo(
                                        context,
                                        "Transaksi dibatalkan otomatis, dikarenakan tidak ada konfirmasi oleh penjual!",
                                        Gravity.CENTER
                                    )
                                }
                            } else if (receipt.getStatus().equals("Sampai")) {
                                val currentTime = System.currentTimeMillis()
                                val timeFinishOto = receipt.getdtFinishOto()!!.toLong()

                                if (currentTime >= timeFinishOto) {
                                    Helper.showDialogInfo(
                                        context,
                                        "Transaksi diselesaikan otomatis, dikarenakan tidak diselesaikan oleh pembeli",
                                        Gravity.CENTER
                                    )
                                }
                            }
                        } else if (receipt.getBuyerId().equals(firebaseUser.uid)) {
                            if (receipt.getStatus().equals("Menunggu konfirmasi!")) {
                                val currentTime = System.currentTimeMillis()
                                val timeCancel = receipt.getdtCancel()!!.toLong()

                                if (currentTime >= timeCancel) {
                                    Helper.showDialogInfo(
                                        context,
                                        "Transaksi dibatalkan otomatis, dikarenakan tidak ada konfirmasi oleh penjual!",
                                        Gravity.CENTER
                                    )
                                }
                            } else if (receipt.getStatus().equals("Sampai")) {
                                val currentTime = System.currentTimeMillis()
                                val timeFinishOto = receipt.getdtFinishOto()!!.toLong()

                                if (currentTime >= timeFinishOto) {
                                    Helper.showDialogInfo(
                                        context,
                                        "Transaksi diselesaikan otomatis, dikarenakan tidak diselesaikan oleh pembeli",
                                        Gravity.CENTER
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }


    private fun mapsVal() {
        val usersRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        coroutineScope.launch {
            try {
                val dataSnapshot = usersRef.get().await()

                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(UserModel::class.java)
                    if (
                        user!!.getAddress().isNullOrEmpty() ||
                        user.getCity().isNullOrEmpty() ||
                        user.getImage().isNullOrEmpty()
                    ) {
                        Helper.showDialogInfo(
                            requireContext(),
                            "Tambahkan foto profil dan alamat lengkap untuk melanjutkan!",
                            Gravity.CENTER
                        )
                    } else {
                        startActivity(Intent(context, MapsActivity::class.java))
                    }
                }
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }

    private fun imageSlider() {
        val imageList = ArrayList<SlideModel>()

        imageList.add(SlideModel(R.drawable.flyer_satu, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.flyer_dua, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.flyer_tiga, ScaleTypes.CENTER_CROP))

        val imageSlider = binding.sliderHome
        imageSlider.setImageList(imageList)
    }

    override fun onStop() {
        super.onStop()
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postsRef.removeEventListener(valueEventListener)
    }

    override fun onPause() {
        super.onPause()
        coroutineScope.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

}