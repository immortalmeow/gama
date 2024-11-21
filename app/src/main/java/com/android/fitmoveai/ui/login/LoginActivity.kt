package com.android.fitmoveai.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.fitmoveai.R
import com.android.fitmoveai.core.preference.LoginPreference
import com.android.fitmoveai.core.utils.UserModel
import com.android.fitmoveai.core.utils.UserPreference
import com.android.fitmoveai.core.utils.ViewModelFactory
import com.android.fitmoveai.databinding.ActivityLoginBinding
import com.android.fitmoveai.ui.MainActivity

import com.android.fitmoveai.ui.register.RegisterActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Setting")

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var user: UserModel
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var sharedPreference: LoginPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        setupView()
        setupViewModel()
        animationPlay()
        binding.edtPasswordLog.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                validationPassword()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                validationPassword()
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        binding.tvHaventAccount.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

//        binding.btnGoogleSignin.setOnClickListener {
//            signInGoogle()
//        }

        sharedPreference = LoginPreference(this)

        // Check if the user is already logged in
        val isLoggedIn = sharedPreference?.getPreferenceBoolean("isLoggedIn") ?: false
        if (isLoggedIn) {
            startAppropriateActivity()
        } else {
            login()

        }

    }



    private fun login() {
        binding.btnLogin.setOnClickListener {

            if (TextUtils.isEmpty(binding.edtEmailLog.text.toString())) {
                binding.edtEmailLog.error = "Masukkan email yang benar"
                return@setOnClickListener
            } else if (TextUtils.isEmpty(binding.edtPasswordLog.text.toString())) {
                binding.edtPasswordLog.error = "Masukkan kata sandi yang benar"
            } else {
                binding.progressCircular.root.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(
                    binding.edtEmailLog.text.toString().trim(),
                    binding.edtPasswordLog.text.toString()
                )
                    .addOnCompleteListener { it ->
                        binding.progressCircular.root.visibility = View.GONE
                        if (it.isSuccessful) {
                            // Save login state in SharedPreferences
                            sharedPreference?.saveBoolean("isLoggedIn", true)
                            startAppropriateActivity()
                        } else {
                            Toast.makeText(
                                this,
                                "Login gagal, silahkan coba lagi",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }
    }

    private fun startAppropriateActivity() {
        val userId = auth.currentUser?.uid
        val dataUser = db.collection("user").document(userId!!)
        dataUser.get()
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }

    private fun validationPassword() {
        val result = binding.edtPasswordLog.text
        binding.btnLogin.isEnabled =
            result != null && result.toString().isNotEmpty() && result.length >= 8
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

    private fun signInGoogle() {
        val credentialManager = CredentialManager.create(this)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                // Display a Toast message if no Google accounts are registered
                Toast.makeText(
                    this@LoginActivity,
                    "No Google accounts found. Please add a Google account to your device.",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("Error", e.message.toString())
            }
        }
    }




    private fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(ContentValues.TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e(ContentValues.TAG, "Unexpected type of credential")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(ContentValues.TAG, "Unexpected type of credential")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "signInWithCredential:success")
                    val user: FirebaseUser? = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }

    private fun setupViewModel() {
        loginViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[LoginViewModel::class.java]

        loginViewModel.getUser().observe(this) { user ->
            this.user = user
        }
    }

    private fun animationPlay() {
        val email = ObjectAnimator.ofFloat(binding.edtEmailLog, View.ALPHA, 1f).setDuration(700)
        val pass = ObjectAnimator.ofFloat(binding.edtPasswordLog, View.ALPHA, 1f).setDuration(700)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(700)
        val tvReg = ObjectAnimator.ofFloat(binding.tvHaventAccount, View.ALPHA, 1f).setDuration(700)

        AnimatorSet().apply {
            playSequentially(email, pass, btnLogin, tvReg)
            start()
        }

    }

    companion object {
        const val TAG = "LoginActivity"
    }

}