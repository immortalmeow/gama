package com.android.fitmoveai.ui.register

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.fitmoveai.databinding.ActivityAssessResultBinding
import com.android.fitmoveai.ui.MainActivity
import com.android.fitmoveai.ui.login.LoginActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class AssessResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssessResultBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssessResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        receiveData()

        binding.button.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun receiveData() {
        val userId = auth.currentUser!!.uid
        val dataUser = db.collection("user").document(userId)
        dataUser.get().addOnSuccessListener {
            val usia = it.get("usia")?.toString() ?: "N/A"
            val berat = it.get("berat")?.toString()?.toDoubleOrNull() ?: 0.0
            val tinggi = it.get("tinggi")?.toString()?.toDoubleOrNull() ?: 0.0

            // Calculate BMI and classify it
            val bmi = calculateBMI(berat, tinggi)
            val bmiClassification = classifyBMI(bmi)

            // Update UI
            binding.apply {
                tvAsesUsia.text = "$usia tahun"
                tvAssesTinggi.text = "$tinggi cm"
                tvAsesBerat.text = "$berat kg"
                tvAsesBMI.text = String.format("%.2f", bmi) // Display BMI with two decimal places
                tvKlasifikasiBMI.text = bmiClassification
            }
        }.addOnFailureListener {
            // Handle any errors here
            Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateBMI(weight: Double, height: Double): Double {
        val heightInMeters = height / 100
        return weight / (heightInMeters * heightInMeters)
    }

    private fun classifyBMI(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Kurus (Underweight)"
            bmi in 18.5..24.9 -> "Normal (Normal Weight)"
            bmi in 25.0..29.9 -> "Berat Badan Lebih (Overweight)"
            bmi in 30.0..34.9 -> "Obesitas Kelas I (Obesity Class I)"
            bmi in 35.0..39.9 -> "Obesitas Kelas II (Obesity Class II)"
            bmi >= 40.0 -> "Obesitas Kelas III (Obesity Class III)"
            else -> "BMI Tidak Valid (Invalid BMI)"
        }
    }
}
