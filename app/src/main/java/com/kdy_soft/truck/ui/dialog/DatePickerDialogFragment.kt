package com.kdy_soft.truck.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.kdy_soft.truck.R
import com.kdy_soft.truck.ui.home.DeliveryDetailFragment
import com.kdy_soft.truck.ui.home.DeliveryDetailViewModel
import com.kdy_soft.truck.util.TruckUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

private const val TAG = "DatePickerFragment"

@AndroidEntryPoint
class DatePickerDialogFragment : DialogFragment(),
    DatePickerDialog.OnDateSetListener {
    private lateinit var calendar: Calendar
    private val args by navArgs<DatePickerDialogFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        calendar = Calendar.getInstance().apply {
            timeInMillis = args.timeInMillis
        }

        Log.d(TAG, "timeInMillis${calendar.timeInMillis}")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
            requireContext(),
            this,
            year,
            month,
            dayOfMonth
        ).apply{
            this.datePicker.minDate = TruckUtils.tomorrow()
        }
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        val timeInMillis = calendar.timeInMillis
        setFragmentResult(DeliveryDetailFragment.REQUEST_DATE, bundleOf(
            DeliveryDetailFragment.DATE_KEY to timeInMillis
        ))
    }
}