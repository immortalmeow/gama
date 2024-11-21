package com.android.fitmoveai.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.android.fitmoveai.core.preference.LoginPreference
import com.android.fitmoveai.databinding.ActivityWelcomeBinding
import com.android.fitmoveai.ui.login.LoginActivity

@SuppressLint("CustomSplashScreen")
class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private var sharedPreference: LoginPreference? = null

    private val DURATION_TIME = 2000L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreference = LoginPreference(this)

        // Check if the user is already logged in
        Handler().postDelayed({
            val isLoggedIn = sharedPreference?.getPreferenceBoolean("isLoggedIn") ?: false
            if (isLoggedIn) {
                // Redirect to MainActivity if already logged in
                startActivity(Intent(this, MainActivity::class.java))
                finish()  // Close WelcomeActivity
            } else {
                // Proceed with WelcomeActivity setup
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            finish()
        }, DURATION_TIME)

    }


}
