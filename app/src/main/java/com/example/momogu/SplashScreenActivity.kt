package com.example.momogu

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.example.momogu.databinding.ActivitySplashScreenBinding
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val myAnim = AnimationUtils.loadAnimation(this, R.anim.mytransition)

        binding.imgSplashLogo.startAnimation(myAnim)
        binding.lblPowerBy.startAnimation(myAnim)
        binding.lblDesmond.startAnimation(myAnim)

        val signInIntent = Intent(this, SignInActivity::class.java)
        val welcomeIntent = Intent(this, WelcomeActivity::class.java)
        val timer = object : Thread() {
            override fun run() {
                try {
                    sleep(1500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } finally {
                    if (FirebaseAuth.getInstance().currentUser != null) {
                        startActivity(welcomeIntent)
                    } else {
                        startActivity(signInIntent)
                    }

                    finish()
                }
            }
        }
        timer.start()
    }
}