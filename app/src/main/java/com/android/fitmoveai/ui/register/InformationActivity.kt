package com.android.fitmoveai.ui.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.fitmoveai.core.model.User
import com.android.fitmoveai.databinding.ActivityFillInformationBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class InformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFillInformationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFillInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        binding.btnSimpan.setOnClickListener {

            val usia = binding.edtAge.text.toString().trim()
            val tinggi = binding.edtHeight.text.toString().trim()
            val berat = binding.edtWeight.text.toString().trim()

            // Validate input fields
            if (usia.isEmpty()) {
                binding.edtAge.error = "Masukkan usia yang benar"
                return@setOnClickListener
            } else if (tinggi.isEmpty()) {
                binding.edtHeight.error = "Masukkan tinggi yang benar"
                return@setOnClickListener
            } else if (berat.isEmpty()) {
                binding.edtWeight.error = "Masukkan berat yang benar"
                return@setOnClickListener
            } else {
                // Show progress indicator
                binding.progressCircular.root.visibility = View.VISIBLE

                // Get current user ID from Firebase Auth
                val userId = auth.currentUser?.uid

                if (userId != null) {
                    // Prepare the data to upload
                    val data = mapOf(
                        "usia" to usia,
                        "tinggi" to tinggi,
                        "berat" to berat
                    )

                    // Save user data to Firestore
                    db.collection("user").document(userId)
                        .update(data)
                        .addOnSuccessListener {
                            // Navigate to the next activity
                            startActivity(Intent(this, AssessResultActivity::class.java))
                            finish()

                            Toast.makeText(
                                this,
                                "Data berhasil disimpan",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Log.d("UploadData", e.message.toString())
                            Toast.makeText(
                                this,
                                "Gagal menyimpan data, silahkan coba lagi",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .addOnCompleteListener {
                            binding.progressCircular.root.visibility = View.GONE
                        }
                } else {
                    Toast.makeText(
                        this,
                        "Gagal mendapatkan user ID, silahkan coba lagi",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.progressCircular.root.visibility = View.GONE
                }
            }
        }
    }


}



