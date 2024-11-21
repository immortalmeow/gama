package com.android.fitmoveai.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.android.fitmoveai.R
import com.android.fitmoveai.core.model.User
import com.android.fitmoveai.core.utils.UserPreference
import com.android.fitmoveai.core.utils.ViewModelFactory
import com.android.fitmoveai.databinding.ActivityRegisterBinding
import com.android.fitmoveai.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Setting")

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        setupView()
        setupViewModel()
        animationPlay()
        register()

        binding.edtPasswordRegis.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                validationButton()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                validationButton()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.tvHaveAccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validationButton() {
        val result = binding.edtPasswordRegis.text
        binding.btnRegis.isEnabled =
            result != null && result.toString().isNotEmpty() && result.length >= 8
    }

    private fun register() {
        binding.btnRegis.setOnClickListener {
            val firstName = binding.edtFirstname.text.toString().trim()
            val lastName = binding.edtLastname.text.toString().trim()
            val email = binding.edtEmailRegis.text.toString().trim()
            val password = binding.edtPasswordRegis.text.toString().trim()

            // Validate input fields
            if (firstName.isEmpty()) {
                binding.edtFirstname.error = "Masukkan nama yang benar"
                return@setOnClickListener
            } else if (lastName.isEmpty()) {
                binding.edtLastname.error = "Masukkan nama yang benar"
                return@setOnClickListener
            } else if (email.isEmpty()) {
                binding.edtEmailRegis.error = "Masukkan Email yang benar"
                return@setOnClickListener
            } else if (password.isEmpty()) {
                binding.edtPasswordRegis.error = "Masukkan Password yang benar"
                return@setOnClickListener
            } else {
                // Show progress indicator
                binding.progressCircular.root.visibility = View.VISIBLE

                // Register user with Firebase
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val photoUrl =
                                "https://ui-avatars.com/api/?background=8692F7&color=fff&size=100&rounded=true&name=$firstName"
                            val userId = auth.currentUser!!.uid
                            val data = User(
                                firstName,
                                lastName,
                                email,

                                fotoProfil = photoUrl
                            )

                            // Save user data to Firestore
                            val userData = db.collection("user").document(userId)
                            userData.set(data)

                            // Navigate to next activity
                            startActivity(Intent(this, GenderActivity::class.java))
                            finish()

                            Toast.makeText(
                                this,
                                "Berhasil mendaftar, silahkan lanjut masuk",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.progressCircular.root.visibility = View.GONE

                        } else {
                            // Handle registration failure
                            Log.d("Register", task.exception?.message.toString())
                            Toast.makeText(
                                this,
                                "Gagal mendaftar, silahkan coba lagi",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.progressCircular.root.visibility = View.GONE
                        }
                    }
            }
        }
    }




    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupViewModel() {
        registerViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[RegisterViewModel::class.java]
    }

    private fun animationPlay() {
        val username = ObjectAnimator.ofFloat(binding.edtFirstname, View.ALPHA, 1f).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.edtEmailRegis, View.ALPHA, 1f).setDuration(500)
        val password = ObjectAnimator.ofFloat(binding.edtPasswordRegis, View.ALPHA, 1f).setDuration(500)
        val btnRegis = ObjectAnimator.ofFloat(binding.btnRegis, View.ALPHA, 1f).setDuration(500)
        val tvLogin = ObjectAnimator.ofFloat(binding.tvHaveAccount, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(username, email, password, btnRegis, tvLogin)
            start()
        }

    }

    companion object {
        const val TAG = "RegisterActivity"
    }
}