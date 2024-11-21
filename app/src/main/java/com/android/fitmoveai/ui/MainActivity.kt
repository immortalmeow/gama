package com.android.fitmoveai.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.fragment.app.Fragment
import com.android.fitmoveai.R
import com.android.fitmoveai.databinding.ActivityMainBinding
import com.android.fitmoveai.ui.history.HistoryFragment
import com.android.fitmoveai.ui.profile.ProfileFragment

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(HomeFragment())

        binding.navView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.Home -> replaceFragment(HomeFragment())
                R.id.History -> replaceFragment(HistoryFragment())
                R.id.Profile -> replaceFragment(ProfileFragment())

                else -> {}
            }
            true
        }

        auth = Firebase.auth
        val firebaseUser = auth.currentUser
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment,fragment)
        fragmentTransaction.commit()
    }


}