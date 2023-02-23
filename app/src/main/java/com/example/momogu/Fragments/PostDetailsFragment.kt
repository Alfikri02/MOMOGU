package com.example.momogu.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momogu.Adapter.PostAdapter
import com.example.momogu.MainActivity
import com.example.momogu.Model.PostModel
import com.example.momogu.R
import com.example.momogu.databinding.FragmentPostDetailsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPostDetailsBinding

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<PostModel>? = null
    private var postId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPostDetailsBinding.inflate(inflater)

        val preferences = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        if (preferences != null) {
            postId = preferences.getString("postId", "none")!!
        }

        val recyclerView: RecyclerView = binding.recyclerViewPostDetails
        recyclerView.setHasFixedSize(true)

        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<PostModel>) }
        recyclerView.adapter = postAdapter

        retrievePosts()

        binding.btnBackPostDetail.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                postList?.clear()
                val post = p0.getValue(PostModel::class.java)
                postList!!.add(post!!)
                postAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}