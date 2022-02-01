package com.kdy_soft.truck.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kdy_soft.truck.data.model.RecordModel
import com.kdy_soft.truck.databinding.ListItemRecordBinding
import com.kdy_soft.truck.ui.record.DeliveryRecordFragmentDirections

class RecordAdapter : ListAdapter<RecordModel, RecordAdapter.RecordHolder>(DIFF_ITEM_CALLBACK) {
    class RecordHolder(private val binding: ListItemRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecordModel){
            binding.recordModel = item
            binding.root.setOnClickListener {
                val dir = DeliveryRecordFragmentDirections
                    .actionDestRecordToDestRecordDetail(item.id)
                it.findNavController().navigate(dir)
            }
        }
    }

    companion object {
        private val DIFF_ITEM_CALLBACK = object : DiffUtil.ItemCallback<RecordModel>()
        {
            override fun areItemsTheSame(oldItem: RecordModel, newItem: RecordModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: RecordModel, newItem: RecordModel): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemRecordBinding.inflate(inflater, parent , false)
        return RecordHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordHolder, position: Int) {
        holder.bind(getItem(position))
    }
}