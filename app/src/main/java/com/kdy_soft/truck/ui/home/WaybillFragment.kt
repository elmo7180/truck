package com.kdy_soft.truck.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kdy_soft.truck.databinding.FragmentWaybillBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WaybillFragment : Fragment() {
    private var _binding: FragmentWaybillBinding? = null
    private lateinit var binding: FragmentWaybillBinding

    private val waybillViewModel by viewModels<WaybillViewModel>({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaybillBinding.inflate(inflater, container, false)
        binding = _binding!!
        return binding.root
    }
}