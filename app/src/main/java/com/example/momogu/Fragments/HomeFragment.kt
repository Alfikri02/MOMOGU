package com.example.momogu.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.models.SlideModel
import com.example.momogu.Adapter.PostAdapter
import com.example.momogu.MapsActivity
import com.example.momogu.Model.PostModel
import com.example.momogu.R
import com.example.momogu.databinding.FragmentHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<PostModel>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater)

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

        binding.mapsHome.setOnClickListener {
            startActivity(Intent(context, MapsActivity::class.java))
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
                        val user = snapshot.getValue(PostModel::class.java)

                        if (user != null) {
                            postList?.add(user)
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

                if (postList.isNullOrEmpty()) {
                    startAnimation(true)
                    binding.recyclerViewHome.visibility = View.GONE
                } else {
                    startAnimation(false)
                    binding.recyclerViewHome.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun imageSlider(){
        val imageList = ArrayList<SlideModel>()

        imageList.add(SlideModel(R.drawable.satu))
        imageList.add(SlideModel(R.drawable.dua))
        imageList.add(SlideModel(R.drawable.tiga))

        val imageSlider = binding.sliderSclaes
        imageSlider.setImageList(imageList)
    }

    private fun startAnimation(isStartAnim: Boolean) {
        if (isStartAnim) {
            binding.animLoadingViewHome.visibility = View.VISIBLE
            binding.animLoadingViewHome.setAnimation("empty.json")
            binding.animLoadingViewHome.playAnimation()
            binding.animLoadingViewHome.repeatMode
        }
    }

}