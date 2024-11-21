package com.android.fitmoveai.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.android.fitmoveai.core.preference.LoginPreference
import com.android.fitmoveai.core.utils.loadImageUrl
import com.android.fitmoveai.databinding.FragmentProfileBinding
import com.android.fitmoveai.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentProfileBinding
    private var sharedPreference: LoginPreference? = null
    private lateinit var storage: StorageReference
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance().reference.child("photo/")
        sharedPreference = LoginPreference(requireContext())



        retrieveData()
        onAction()
    }



    private fun onAction() {
        binding.btnLogout.setOnClickListener {
            showExitConfirmationDialog()
        }

        binding.apply {
            imgProfile.setOnClickListener {
                resultLauncher.launch("image/*")
            }

            // Set the listener for saving profile changes
            btnEditProfile.setOnClickListener {
                updateData()
            }

            fabImg.setOnClickListener {
                if (imageUri != null) {
                    uploadImage()
                } else {
                    updateData() // Update data even if no image is selected
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun retrieveData() {
        val userId = auth.currentUser!!.uid
        val dataUser = db.collection("user").document(userId)
        dataUser.get().addOnSuccessListener {
            val firstName = it.get("firstName")
            val lastName = it.get("lastName")
            val fotoProfil = it.get("fotoProfil")
            val gender = it.get("gender")
            val berat = it.get("berat")
            val tinggi = it.get("tinggi")
            val usia = it.get("usia")

            binding.apply {
                tvUsername.text = "$firstName $lastName"
                imgProfile.loadImageUrl(fotoProfil.toString(), requireContext())
                edtGender.setText(gender.toString())
                edtHeightProfile.setText(tinggi.toString())
                edtWeightProfile.setText(berat.toString())
                edtAgeProfile.setText(usia.toString())

            }
        }
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                binding.progressBarDialog.root.visibility = View.VISIBLE
                // Clear the shared preference for login status
                sharedPreference?.saveBoolean("isLoggedIn", false)
                // Logout from Firebase
                auth.signOut()
                // Redirect to LoginActivity
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                binding.progressBarDialog.root.visibility = View.GONE
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){
        imageUri = it
        binding.imgProfile.setImageURI(it)
    }

    private fun uploadImage() {
        binding.progressCircular.root.visibility = View.VISIBLE // Show progress before starting

        if (imageUri != null) {
            val userId = auth.currentUser!!.uid
            storage = storage.child(userId)

            // Compress image before uploading
            val bitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(imageUri!!))
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos)
            val data = baos.toByteArray()

            storage.putBytes(data)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        storage.downloadUrl.addOnSuccessListener { uri ->
                            val nFotoProfil = uri.toString()
                            val updateData = mapOf(
                                "fotoProfil" to nFotoProfil,
                                "usia" to binding.edtAgeProfile.text.toString(),
                                "gender" to binding.edtGender.text.toString(),
                                "tinggi" to binding.edtHeightProfile.text.toString(),
                                "berat" to binding.edtWeightProfile.text.toString()
                            )

                            db.collection("user").document(userId).update(updateData)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        Toast.makeText(requireContext(), "Foto profile tersimpan", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(requireContext(), "Gagal Menyimpan Data", Toast.LENGTH_SHORT).show()
                                    }
                                    binding.progressCircular.root.visibility = View.GONE
                                }
                        }.addOnFailureListener { e ->
                            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                            binding.progressCircular.root.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gagal mengunggah foto", Toast.LENGTH_SHORT).show()
                        binding.progressCircular.root.visibility = View.GONE
                    }
                }
        }
    }

    private fun updateData() {
        binding.progressCircular.root.visibility = View.VISIBLE // Show progress before starting

        val userId = auth.currentUser!!.uid
        val updateData = mapOf(
            "usia" to binding.edtAgeProfile.text.toString(),
            "gender" to binding.edtGender.text.toString(),
            "tinggi" to binding.edtHeightProfile.text.toString(),
            "berat" to binding.edtWeightProfile.text.toString()
        )

        db.collection("user").document(userId).update(updateData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Sukses Menyimpan Data", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal Menyimpan Data", Toast.LENGTH_SHORT).show()
                }
                binding.progressCircular.root.visibility = View.GONE
            }
    }

}


