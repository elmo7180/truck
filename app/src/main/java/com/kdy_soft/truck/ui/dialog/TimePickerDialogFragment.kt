package com.kdy_soft.truck.ui.dialog

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.kdy_soft.truck.ui.home.DeliveryDetailFragment
import com.kdy_soft.truck.ui.home.DeliveryDetailFragment.Companion.REQUEST_TIME
import com.kdy_soft.truck.ui.home.DeliveryDetailFragment.Companion.TIME_KEY
import com.kdy_soft.truck.ui.home.DeliveryDetailViewModel
import java.util.*

class TimePickerDialogFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    private lateinit var calendar: Calendar
    private val args by navArgs<TimePickerDialogFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        calendar = Calendar.getInstance().apply {
            timeInMillis = args.timeInMillis
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return TimePickerDialog(
            requireContext(),
            this,
            hourOfDay,
            minute,
            true
        )

    }

    override fun onTimeSet(p0: TimePicker?, hourOfDay: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)

        val timeInMillis = calendar.timeInMillis
        setFragmentResult(
            REQUEST_TIME,
            bundleOf(TIME_KEY to timeInMillis)
        )
    }
}