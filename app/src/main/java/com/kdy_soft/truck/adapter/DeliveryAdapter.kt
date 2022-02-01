package com.kdy_soft.truck.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.kdy_soft.truck.data.entity.Delivery
import com.kdy_soft.truck.data.model.DeliveryModel
import com.kdy_soft.truck.databinding.ListItemDeliveryBinding
import com.kdy_soft.truck.ui.home.DeliveryItemViewModel

class DeliveryAdapter(private val listener: OnItemClickListener) :
    ListAdapter<DeliveryModel, DeliveryAdapter.ViewHolder>(DIFF_ITEM_CALLBACK) {
    interface OnItemClickListener {
        fun onItemClick(model: DeliveryModel, position: LatLng)
    }

    inner class ViewHolder(private val binding: ListItemDeliveryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DeliveryModel) {
            itemView.setOnClickListener { listener.onItemClick(item, item.departure.latLng) }
            val viewModel = DeliveryItemViewModel(item)
            binding.itemViewModel = viewModel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemDeliveryBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_ITEM_CALLBACK = object : DiffUtil.ItemCallback<DeliveryModel>() {
            override fun areItemsTheSame(
                oldItem: DeliveryModel,
                newItem: DeliveryModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: DeliveryModel,
                newItem: DeliveryModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}