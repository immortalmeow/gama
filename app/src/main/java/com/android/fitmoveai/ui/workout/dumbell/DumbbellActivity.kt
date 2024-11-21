package com.android.fitmoveai.ui.workout.dumbell

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.fitmoveai.databinding.ActivityDumbbellBinding
import com.android.fitmoveai.ui.camera.dumbell.CameraDumbellActivity


class DumbbellActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDumbbellBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDumbbellBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Dumbbell"

        setAction()
    }

    private fun setAction(){
        binding.btnStarted.setOnClickListener {
            startActivity(Intent(this@DumbbellActivity, CameraDumbellActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}