package com.example.momogu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.momogu.Model.UserModel
import com.example.momogu.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Suppress("DEPRECATION")
class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        binding.animLoadingViewWelcome.setAnimation("welcome.json")
        binding.animLoadingViewWelcome.playAnimation()
        binding.animLoadingViewWelcome.loop(true)

        setOnline()
        setStatusDT()

        val usersRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UserModel::class.java)
                    binding.lblUsername.text = user?.getUsername().toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

        val signInIntent = Intent(this, SignInActivity::class.java)
        val timer = object : Thread() {
            override fun run() {
                try {
                    sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } finally {
                    startActivity(signInIntent)
                    finish()
                }
            }
        }
        timer.start()
    }

    private fun setOnline() {
        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        val map = HashMap<String, Any>()
        map["statusOn"] = "Aktif"
        ref.child(firebaseUser.uid).updateChildren(map)
    }

    private fun setStatusDT() {
        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        val map = HashMap<String, Any>()
        map["statusOn"] = "Tidak Aktif"
        map["lastOnline"] = System.currentTimeMillis().toString()
        ref.child(firebaseUser.uid).onDisconnect().updateChildren(map)
    }
}