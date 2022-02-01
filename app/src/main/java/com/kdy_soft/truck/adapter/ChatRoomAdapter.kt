package com.kdy_soft.truck.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kdy_soft.truck.data.model.ChatRoomModel
import com.kdy_soft.truck.databinding.ListItemChatRoomBinding

class ChatRoomAdapter(private val lifecycleOwner: LifecycleOwner) :
    ListAdapter<ChatRoomModel, ChatRoomAdapter.ChatRoomHolder>(DIFF_ITEM_CALLBACK) {
    private var listener: ((String) -> Unit)? = null

    fun setItemClickListener(listener: (roomId: String) -> Unit) {
        this.listener = listener
    }

    inner class ChatRoomHolder(private val binding: ListItemChatRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatRoomModel, lifecycleOwner: LifecycleOwner) {
            binding.root.setOnClickListener { listener?.invoke(item.id) }
            binding.lifecycleOwner = lifecycleOwner
            binding.chatRoomModel = item
        }
    }

    companion object {
        private val DIFF_ITEM_CALLBACK = object : DiffUtil.ItemCallback<ChatRoomModel>() {
            override fun areItemsTheSame(oldItem: ChatRoomModel, newItem: ChatRoomModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ChatRoomModel,
                newItem: ChatRoomModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemChatRoomBinding.inflate(inflater, parent, false)
        return ChatRoomHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatRoomHolder, position: Int) {
        holder.bind(getItem(position), lifecycleOwner)
    }
}