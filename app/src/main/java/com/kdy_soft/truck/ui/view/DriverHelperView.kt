package com.kdy_soft.truck.ui.view

import android.content.Context
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import com.kdy_soft.truck.R
import com.kdy_soft.truck.databinding.CompoundViewDriverHelperBinding
import com.kdy_soft.truck.ui.viewModel.DriverHelperViewModel

class DriverHelperView(
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
    private val binding: CompoundViewDriverHelperBinding

    init {
        val layoutInflater = context
            .getSystemService(
                Context.LAYOUT_INFLATER_SERVICE
            ) as LayoutInflater

        binding = CompoundViewDriverHelperBinding.inflate(
            layoutInflater,
            this,
            true
        )

    }

    fun setViewModel(viewModel: DriverHelperViewModel?) {
        binding.viewModel = viewModel
    }

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner?) {
        binding.lifecycleOwner = lifecycleOwner
    }

    fun setOnCompleteListener(listener: OnClickListener) {
        binding.onCompleteListener = listener
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