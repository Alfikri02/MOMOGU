package com.example.momogu.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momogu.Adapter.NotificationAdapter
import com.example.momogu.Model.NotificationModel
import com.example.momogu.databinding.FragmentNotificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Collections.reverse
import kotlin.collections.ArrayList

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding

    private var notificationList: List<NotificationModel>? = null
    private var notificationAdapter: NotificationAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNotificationBinding.inflate(inflater)

        val recyclerView: RecyclerView = binding.recyclerViewNotifications
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        notificationList = ArrayList()
        notificationAdapter =
            NotificationAdapter(requireContext(), notificationList as ArrayList<NotificationModel>)
        recyclerView.adapter = notificationAdapter

        readNotifications()

        return binding.root
    }

    private fun readNotifications() {
        val notificationRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        notificationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (notificationList as ArrayList<NotificationModel>).clear()

                    for (snapshot in p0.children) {
                        val notification = snapshot.getValue(NotificationModel::class.java)

                        (notificationList as ArrayList<NotificationModel>).add(notification!!)
                    }

                    binding.animLoadingViewNotification.visibility = View.GONE
                    binding.emptyNotification.visibility = View.GONE
                    binding.recyclerViewNotifications.visibility = View.VISIBLE

                    reverse(notificationList)
                    notificationAdapter!!.notifyDataSetChanged()
                } else {
                    binding.animLoadingViewNotification.visibility = View.VISIBLE
                    binding.emptyNotification.visibility = View.VISIBLE
                    binding.recyclerViewNotifications.visibility = View.GONE
                    binding.animLoadingViewNotification.setAnimation("13525-empty.json")
                    binding.animLoadingViewNotification.playAnimation()
                    binding.animLoadingViewNotification.loop(true)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}