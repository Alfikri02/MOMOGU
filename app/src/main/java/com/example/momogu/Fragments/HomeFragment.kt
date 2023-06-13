package com.example.momogu.Fragments

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
import com.example.momogu.Adapter.PostAdapter
import com.example.momogu.MapsActivity
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.ReceiptModel
import com.example.momogu.Model.UserModel
import com.example.momogu.R
import com.example.momogu.databinding.FragmentHomeBinding
import com.example.momogu.utils.Helper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<PostModel>? = null
    private lateinit var firebaseUser: FirebaseUser

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

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
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                coroutineScope.launch {
                    postSearch()
                    searchPost(newText.toString())
                }
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
    private suspend fun postSearch() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Posts")

        try {
            val dataSnapshot = userRef.get().await()

            if (binding.search.toString() == "") {
                postList?.clear()

                for (snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(PostModel::class.java)

                    if (post != null) {
                        postList?.add(post)
                    }
                }

                postAdapter?.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            // Handle the exception
        }
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
            postList?.clear()

            for (snapshot in dataSnapshot.children) {
                val post = snapshot.getValue(PostModel::class.java)

                if (post != null) {
                    postList?.add(post)
                }
            }

            postAdapter?.notifyDataSetChanged()
        } catch (e: Exception) {
            // Handle the exception
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        coroutineScope.launch {
            try {
                val dataSnapshot = postsRef.get().await()
                postList?.clear()

                for (snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(PostModel::class.java)
                    postList?.sortByDescending { it.getDateTime() }
                    post?.let { postList!!.add(it) }
                }

                postAdapter?.notifyDataSetChanged()
            } catch (e: Exception) {
                // Handle the exception
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

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the coroutine scope to avoid leaks
        coroutineScope.cancel()
    }

}