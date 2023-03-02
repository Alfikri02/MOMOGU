package com.example.momogu

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.momogu.Fragments.HomeFragment
import com.example.momogu.Fragments.ProfileFragment
import com.example.momogu.Fragments.ScalesFragment

class MainActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        routeToFragment(HomeFragment())
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId)
        {

            R.id.nav_scales ->
            {
                routeToFragment(ScalesFragment())
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_home ->
            {
                routeToFragment(HomeFragment())
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_profile ->
            {
                routeToFragment(ProfileFragment())
                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }

    private fun routeToFragment(fragment: Fragment)
    {
        val routeFrag = supportFragmentManager.beginTransaction()
        routeFrag.replace(R.id.fragment_container, fragment)
        routeFrag.commit()
    }

    override fun onBackPressed()
    {
        //Don't do anything
    }
}