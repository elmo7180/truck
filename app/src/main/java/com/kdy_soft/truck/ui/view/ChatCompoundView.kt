package com.kdy_soft.truck.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import com.kdy_soft.truck.adapter.ChatAdapter
import com.kdy_soft.truck.data.model.ChatModel
import com.kdy_soft.truck.databinding.CompoundViewChatBinding

class ChatCompoundView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {
    private val binding: CompoundViewChatBinding
    private val adapter = ChatAdapter()
    private var sendMessageCallback: ((String) -> Unit)? = null

    init {
        val layoutInflater = context
            .getSystemService(
                Context.LAYOUT_INFLATER_SERVICE
            ) as LayoutInflater

        binding = CompoundViewChatBinding.inflate(
            layoutInflater,
            this,
            true
        )

        binding.chatList.adapter = adapter
        binding.send.setOnClickListener { sendMessage() }
    }

    fun setReadOnly(){
        binding.msg.visibility = View.GONE
        binding.send.visibility = View.GONE
    }

    fun setSendMessageCallback(callback: (String) -> Unit) {
        sendMessageCallback = callback
    }

    fun submitList(list: List<ChatModel>?) {
        adapter.submitList(list)
        binding.chatList.scrollToPosition(adapter.itemCount - 1)
    }

    private fun sendMessage() {
        val message = binding.msg.text.toString()
        sendMessageCallback?.invoke(message)
        binding.msg.setText("")
    }

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner?) {
        binding.lifecycleOwner = lifecycleOwner
    }


    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : this(context, attrs, defStyleAttr, 0)

}