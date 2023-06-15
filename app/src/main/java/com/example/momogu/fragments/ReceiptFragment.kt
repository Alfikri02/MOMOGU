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
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

class ReceiptFragment : Fragment() {

    private lateinit var binding: FragmentReceiptBinding

    private var receiptList: MutableList<ReceiptModel>? = null
    private var receiptAdapter: ReceiptAdapter? = null
    private lateinit var firebaseUser: FirebaseUser

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

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

        coroutineScope.launch {
            try {
                val dataSnapshot = receiptRef.get().await()
                val tempList = ArrayList<ReceiptModel>()

                for (snapshot in dataSnapshot.children) {
                    val notification = snapshot.getValue(ReceiptModel::class.java)

                    if (notification?.getSellerId().equals(firebaseUser.uid)) {
                        tempList.add(notification!!)
                    } else if (notification?.getBuyerId().equals(firebaseUser.uid)) {
                        tempList.add(notification!!)
                    }

                    tempList.sortedByDescending { it.getDateTime() }
                }

                withContext(Dispatchers.Main) {
                    receiptList?.clear()
                    receiptList?.addAll(tempList)
                    receiptAdapter?.notifyDataSetChanged()

                    if (tempList.isEmpty()) {
                        startAnimation()
                    }
                }
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }


    private fun startAnimation(){
        binding.animLoadingViewNotification.visibility = View.VISIBLE
        binding.emptyNotification.visibility = View.VISIBLE
        binding.recyclerViewNotifications.visibility = View.GONE
        binding.animLoadingViewNotification.setAnimation("empty.json")
        binding.animLoadingViewNotification.playAnimation()
        binding.animLoadingViewNotification.repeatCount = LottieDrawable.INFINITE
    }

    override fun onPause() {
        super.onPause()
        coroutineScope.cancel()
    }

    override fun onStop() {
        super.onStop()
        coroutineScope.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

}