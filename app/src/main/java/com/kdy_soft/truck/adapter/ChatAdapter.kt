package com.kdy_soft.truck.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kdy_soft.truck.data.model.ChatModel
import com.kdy_soft.truck.databinding.ListItemChatBinding
import com.kdy_soft.truck.databinding.ListItemChatRoomBinding

class ChatAdapter :
    ListAdapter<ChatModel, ChatAdapter.ChatHolder>(DIFF_ITEM_CALLBACK) {
    class ChatHolder(private val binding: ListItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: ChatModel) {
            binding.chatModel = model

            val align = if (model.isMe) {
                View.TEXT_ALIGNMENT_VIEW_END
            } else {
                View.TEXT_ALIGNMENT_VIEW_START
            }

            binding.msg.textAlignment = align
            binding.msgDate.textAlignment = align

            if(model.isMe){
                binding.profileLayout.visibility = View.GONE
            }else {
                binding.profileLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemChatBinding.inflate(inflater, parent, false)

        return ChatHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_ITEM_CALLBACK = object : DiffUtil.ItemCallback<ChatModel>() {
            override fun areItemsTheSame(oldItem: ChatModel, newItem: ChatModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ChatModel, newItem: ChatModel): Boolean {
                return oldItem == newItem
            }


        }
    }
}