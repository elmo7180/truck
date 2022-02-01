package com.kdy_soft.truck.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kdy_soft.truck.databinding.ListItemDeliveryBinding
import com.kdy_soft.truck.databinding.ListItemMonthlyPageBinding

class MonthlyRecordAdapter :
    ListAdapter<Any, MonthlyRecordAdapter.MonthlyHolder>(DIFF_ITEM_CALLBACK) {
    class MonthlyHolder(private val binding: ListItemMonthlyPageBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    companion object {
        private val DIFF_ITEM_CALLBACK = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                TODO("Not yet implemented")
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                TODO("Not yet implemented")
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthlyHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: MonthlyHolder, position: Int) {
        TODO("Not yet implemented")
    }
}