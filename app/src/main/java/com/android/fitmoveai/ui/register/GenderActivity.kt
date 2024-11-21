package com.android.fitmoveai.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.fitmoveai.databinding.ActivityGenderBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class GenderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenderBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedGender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        binding.maleGender.setOnClickListener {
            selectGender("male")
        }

        binding.femaleGender.setOnClickListener {
            selectGender("female")
        }

        binding.btnGenderNext.setOnClickListener {
            if (selectedGender != null) {
                saveGenderToFirestore(selectedGender!!)
            } else {
                Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectGender(gender: String) {
        selectedGender = gender

        // Reset the selection visuals
        binding.maleGender.alpha = if (gender == "male") 1.0f else 0.5f
        binding.femaleGender.alpha = if (gender == "female") 1.0f else 0.5f

        // Optionally, you can add a checkmark or other visual indicator here
    }

    private fun saveGenderToFirestore(gender: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("user").document(userId)
                .update("gender", gender)
                .addOnSuccessListener {
                    // Navigate to the next activity
                    val intent = Intent(this, InformationActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save gender: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
