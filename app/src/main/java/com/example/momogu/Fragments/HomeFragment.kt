package com.example.momogu.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momogu.Adapter.PostAdapter
import com.example.momogu.Model.PostModel
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
        recyclerView.layoutManager = GridLayoutManager(context,2)

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<PostModel>) }
        recyclerView.adapter = postAdapter

        retrievePosts()

        return binding.root
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                postList?.clear()

                for (snapshot in p0.children) {
                    val post = snapshot.getValue(PostModel::class.java)
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

    private fun startAnimation(isStartAnim: Boolean) {
        if (isStartAnim) {
            binding.animLoadingViewHome.visibility = View.VISIBLE
            binding.animLoadingViewHome.setAnimation("13525-empty.json")
            binding.animLoadingViewHome.playAnimation()
            binding.animLoadingViewHome.repeatMode
        }
    }

}