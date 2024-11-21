package com.android.fitmoveai.ui.history

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.fitmoveai.core.adapter.HistoryAdapter
import com.android.fitmoveai.core.model.History
import com.android.fitmoveai.databinding.FragmentHistoryBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore


class HistoryFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentHistoryBinding
    private val historyAdapter = HistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadHistoryData()
    }

    private fun setupRecyclerView() {
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = historyAdapter
        }
    }

    private fun loadHistoryData() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("user").document(userId).collection("workouts")
                .get()
                .addOnSuccessListener { documents ->
                    val historyList = mutableListOf<History>()
                    for (document in documents) {
                        val history = document.toObject(History::class.java)
                        historyList.add(history)
                    }
                    historyAdapter.submitList(historyList)
                }
                .addOnFailureListener { e ->
                    Log.w("HistoryFragment", "Error loading history", e)
                    Toast.makeText(requireContext(), "Failed to load workout history.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }
}