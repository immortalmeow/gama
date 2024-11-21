package com.android.fitmoveai.ui.workout.pushup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.fitmoveai.databinding.ActivityPushupBinding
import com.android.fitmoveai.ui.camera.pushup.CameraPushupActivity

class PushupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPushupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPushupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Push Up"

        setAction()
    }

    private fun setAction(){
        binding.btnStarted.setOnClickListener {
            startActivity(Intent(this@PushupActivity, CameraPushupActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}