package com.example.momogu.Fragments

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.example.momogu.Adapter.ReceiptAdapter
import com.example.momogu.Model.ReceiptModel
import com.example.momogu.databinding.FragmentReceiptBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Collections.reverse
import kotlin.collections.ArrayList

class ReceiptFragment : Fragment() {

    private lateinit var binding: FragmentReceiptBinding

    private var receiptList: List<ReceiptModel>? = null
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

        readNotifications()

        return binding.root
    }

    private fun readNotifications() {
        val notificationRef = FirebaseDatabase.getInstance().reference.child("Receipt")

        notificationRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (receiptList as ArrayList<ReceiptModel>).clear()

                    for (snapshot in p0.children) {
                        val notification = snapshot.getValue(ReceiptModel::class.java)

                        if (notification?.getSellerId().equals(firebaseUser.uid)) {
                            (receiptList as ArrayList<ReceiptModel>).sortByDescending { it.getDateTime() }
                            (receiptList as ArrayList<ReceiptModel>).add(notification!!)
                        } else if (notification?.getBuyerId().equals(firebaseUser.uid)) {
                            (receiptList as ArrayList<ReceiptModel>).sortByDescending { it.getDateTime() }
                            (receiptList as ArrayList<ReceiptModel>).add(notification!!)
                        }
                    }

                    binding.animLoadingViewNotification.visibility = View.GONE
                    binding.emptyNotification.visibility = View.GONE
                    binding.recyclerViewNotifications.visibility = View.VISIBLE

                    receiptList?.let { reverse(it) }
                    receiptAdapter!!.notifyDataSetChanged()
                } else {
                    binding.animLoadingViewNotification.visibility = View.VISIBLE
                    binding.emptyNotification.visibility = View.VISIBLE
                    binding.recyclerViewNotifications.visibility = View.GONE
                    binding.animLoadingViewNotification.setAnimation("empty.json")
                    binding.animLoadingViewNotification.playAnimation()
                    binding.animLoadingViewNotification.repeatCount = LottieDrawable.INFINITE
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

}