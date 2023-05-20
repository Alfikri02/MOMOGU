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
import com.example.momogu.Model.UserModel
import com.example.momogu.R
import com.example.momogu.databinding.FragmentHomeBinding
import com.example.momogu.utils.Helper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<PostModel>? = null
    private lateinit var firebaseUser: FirebaseUser

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
        imageSlider()

        binding.search.queryHint = "Cari sapi yang anda inginkan!"
        binding.search.onActionViewExpanded()
        binding.search.clearFocus()
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                postSearch()
                searchPost(newText.toString())
                return true
            }

        })

        binding.cvMaps.setOnClickListener {
            mapsVal()
        }

        return binding.root
    }

    private fun postSearch() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Posts")

        userRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                if (binding.search.toString() == "") {
                    postList?.clear()

                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(PostModel::class.java)

                        if (post != null) {
                            postList?.add(post)
                        }
                    }

                    postAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun searchPost(input: String) {
        val query = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .orderByChild("product")
            .startAt(input)
            .endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            var isUserExist: Boolean? = false

            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                postList?.clear()

                for (snapshot in p0.children) {
                    val post = snapshot.getValue(PostModel::class.java)

                    if (post != null) {
                        isUserExist = true
                        postList?.add(post)
                    }
                }

                postAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                postList?.clear()
                for (snapshot in p0.children) {
                    val post = snapshot.getValue(PostModel::class.java)
                    postList?.sortByDescending { it.getDateTime() }
                    post?.let { postList!!.add(it) }
                    postAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun mapsVal(){
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

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
                            requireContext(),
                            "Tambahkan foto profil dan alamat lengkap untuk melanjutkan!",
                            Gravity.CENTER
                        )
                    } else {
                        startActivity(Intent(context, MapsActivity::class.java))
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun imageSlider(){
        val imageList = ArrayList<SlideModel>()

        imageList.add(SlideModel(R.drawable.flyer_satu, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.flyer_dua, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.flyer_tiga, ScaleTypes.CENTER_CROP))

        val imageSlider = binding.sliderHome
        imageSlider.setImageList(imageList)
    }

}