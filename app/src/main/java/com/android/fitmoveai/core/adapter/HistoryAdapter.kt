package com.android.fitmoveai.core.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.fitmoveai.core.model.History
import com.android.fitmoveai.databinding.ItemHistoryBinding

class HistoryAdapter: ListAdapter<History, HistoryAdapter.HistoryViewHolder>(DiffCallback) {


    class HistoryViewHolder(private val binding: ItemHistoryBinding):RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(history:History) {
            binding.apply {
                tvWorkoutName.text = history.workout
                tvWorkoutCount.text = "${history.count} reps"
                tvWorkoutTime.text = "Times: ${history.time}"
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }



    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DiffCallback: DiffUtil.ItemCallback<History> =
            object : DiffUtil.ItemCallback<History>() {

                override fun areItemsTheSame(oldItem: History, newItem: History): Boolean {
                    return oldItem.workout == newItem.workout
                }

                @SuppressLint("DiffUtilEquals")
                override fun areContentsTheSame(oldItem: History, newItem: History): Boolean {
                    return oldItem == newItem
                }
            }
    }


}