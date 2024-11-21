package com.android.fitmoveai.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.android.fitmoveai.R
import com.android.fitmoveai.databinding.FragmentHomeBinding
import com.android.fitmoveai.ui.workout.dumbell.DumbbellActivity
import com.android.fitmoveai.ui.workout.pushup.PushupActivity
import com.android.fitmoveai.ui.workout.situp.SitupActivity
import com.android.fitmoveai.ui.workout.squat.SquatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        binding.apply {

            btnDumbbell.setOnClickListener {
                val activity = requireActivity()
                activity.startActivity(Intent(activity, SitupActivity::class.java))
            }

            btnPushup.setOnClickListener {
                val activity = requireActivity()
                activity.startActivity(Intent(activity, PushupActivity::class.java))
            }

            btnSquat.setOnClickListener {
                val activity = requireActivity()
                activity.startActivity(Intent(activity, SquatActivity::class.java))
            }

        }
        retrieveData()

    }

    @SuppressLint("SetTextI18n")
    private fun retrieveData(){

        val userId = auth.currentUser!!.uid
        val dataUser = db.collection("user").document(userId)
        dataUser.get().addOnSuccessListener {
            val name = it.get("firstName")

            binding.apply {
                tvUsernameWelcome.text = name.toString()


            }

        }

    }

}