package com.example.momogu.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.example.momogu.adapter.ReceiptAdapter
import com.example.momogu.model.ReceiptModel
import com.example.momogu.databinding.FragmentReceiptBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.ArrayList

class ReceiptFragment : Fragment() {

    private lateinit var binding: FragmentReceiptBinding

    private var receiptList: MutableList<ReceiptModel>? = null
    private var receiptAdapter: ReceiptAdapter? = null
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentReceiptBinding.inflate(inflater)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val recyclerView: RecyclerView = binding.recyclerViewNotifications
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        receiptList = ArrayList()
        receiptAdapter =
            ReceiptAdapter(requireContext(), receiptList as ArrayList<ReceiptModel>)
        recyclerView.adapter = receiptAdapter

        myReceipt()

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun myReceipt() {
        val receiptRef = FirebaseDatabase.getInstance().reference.child("Receipt")

        receiptRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val tempList = ArrayList<ReceiptModel>()

                    for (snapshot in p0.children) {
                        val notification = snapshot.getValue(ReceiptModel::class.java)

                        if (notification?.getSellerId().equals(firebaseUser.uid)) {
                            tempList.add(notification!!)
                        } else if (notification?.getBuyerId().equals(firebaseUser.uid)) {
                            tempList.add(notification!!)
                        }
                    }

                    tempList.sortByDescending { it.getDateTime() }

                    receiptList?.clear()
                    receiptList?.addAll(tempList)
                    receiptAdapter?.notifyDataSetChanged()

                    if (tempList.isEmpty()) {
                        startAnimation()
                    }
                }else{
                    startAnimation()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun startAnimation() {
        binding.animLoadingViewNotification.visibility = View.VISIBLE
        binding.emptyNotification.visibility = View.VISIBLE
        binding.recyclerViewNotifications.visibility = View.GONE
        binding.animLoadingViewNotification.setAnimation("empty.json")
        binding.animLoadingViewNotification.playAnimation()
        binding.animLoadingViewNotification.repeatCount = LottieDrawable.INFINITE
    }

}