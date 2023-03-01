package com.example.momogu.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momogu.Adapter.PostAdapter
import com.example.momogu.Model.PostModel
import com.example.momogu.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding

    private var recyclerView: RecyclerView? = null
    private var postAdapter: PostAdapter? = null
    private var mPost: List<PostModel>? = null
    private var resultNotFound: RelativeLayout? = null
    private var emptySearch: RelativeLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)

        recyclerView = binding.recyclerViewSearch
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mPost = ArrayList()
        postAdapter = context?.let { PostAdapter(it, mPost as List<PostModel>) }
        recyclerView?.adapter = postAdapter
        resultNotFound = binding.layoutSearchResultNotFound
        emptySearch = binding.layoutEmptySearch

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.searchEditText.text.toString() == "") {
                    //ToDo: Add Empty or no Result Screen
                } else {
                    recyclerView?.visibility = View.VISIBLE
                    resultNotFound?.visibility = View.GONE
                    emptySearch?.visibility = View.GONE

                    //retrieveUsers()
                    //searchUser(s.toString().lowercase())
                }
            }
        })

        return binding.root
    }

    /*
    private fun retrieveUsers() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (binding.searchEditText.text.toString() == "") {
                    mPost?.clear()

                    for (snapshot in p0.children) {
                        val user = snapshot.getValue(UserModel::class.java)

                        if (user != null) {
                            mPost?.add(user)
                        }
                    }

                    userAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

     */

    /*
    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().reference
            .child("Users")
            .orderByChild("fullname")
            .startAt(input)
            .endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            var isUserExist: Boolean? = false

            override fun onDataChange(p0: DataSnapshot) {
                mUser?.clear()

                for (snapshot in p0.children) {
                    val user = snapshot.getValue(UserModel::class.java)

                    if (user != null) {
                        isUserExist = true
                        mUser?.add(user)
                    }
                }

                if (isUserExist == false) {
                    recyclerView?.visibility = View.GONE
                    resultNotFound?.visibility = View.VISIBLE
                    emptySearch?.visibility = View.GONE
                }

                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
    */

}