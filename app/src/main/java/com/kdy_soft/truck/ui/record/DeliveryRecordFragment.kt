package com.kdy_soft.truck.ui.record

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.kdy_soft.truck.R
import com.kdy_soft.truck.adapter.MonthlyRecordAdapter
import com.kdy_soft.truck.adapter.RecordAdapter
import com.kdy_soft.truck.databinding.FragmentRecordBinding
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "DeliveryRecordFragment"

@AndroidEntryPoint
class DeliveryRecordFragment : Fragment() {
    private var _binding: FragmentRecordBinding? = null
    private lateinit var binding: FragmentRecordBinding

    private val recordViewModel: DeliveryRecordViewModel by viewModels()

    private val recordAdapter = RecordAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        binding = _binding!!

        val config = AppBarConfiguration(setOf(R.id.dest_record))
        binding.toolbar.setupWithNavController(findNavController(), config)

        binding.recordList.adapter = recordAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun subscribeUi() {
        recordViewModel.recordList.observe(viewLifecycleOwner) {
            Log.d(TAG, "recordModels:${it}")
            recordAdapter.submitList(it)
            binding.recordList.scrollToPosition(recordAdapter.itemCount - 1)
        }
    }
}