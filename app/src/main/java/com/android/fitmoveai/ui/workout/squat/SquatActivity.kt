package com.android.fitmoveai.ui.workout.squat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.fitmoveai.databinding.ActivitySquatctivityBinding
import com.android.fitmoveai.ui.camera.squat.CameraActivity


class SquatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySquatctivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySquatctivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Squat"

        setAction()

    }
    private fun setAction(){
        binding.btnStarted.setOnClickListener {
            startActivity(Intent(this@SquatActivity, CameraActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}