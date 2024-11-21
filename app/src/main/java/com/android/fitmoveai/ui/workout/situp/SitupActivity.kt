package com.android.fitmoveai.ui.workout.situp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.fitmoveai.databinding.ActivitySitupBinding
import com.android.fitmoveai.ui.camera.situp.CameraSitupActivity

class SitupActivity:AppCompatActivity() {

    private lateinit var binding: ActivitySitupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySitupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sit Up"

        setAction()

    }
    private fun setAction(){
        binding.btnStarted.setOnClickListener {
            startActivity(Intent(this@SitupActivity, CameraSitupActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}