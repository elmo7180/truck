package com.kdy_soft.truck.adapter

import android.content.Context
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.kdy_soft.truck.R

fun Fragment.carKindsAdapter(): ArrayAdapter<CharSequence> {
    return ArrayAdapter.createFromResource(
        requireContext(), R.array.car_kinds, android.R.layout.simple_spinner_item
    ).also { adapter ->
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
}