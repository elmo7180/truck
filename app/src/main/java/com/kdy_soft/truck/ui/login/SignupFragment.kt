package com.kdy_soft.truck.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kdy_soft.truck.adapter.carKindsAdapter
import com.kdy_soft.truck.data.entity.VehicleKinds
import com.kdy_soft.truck.databinding.FragmentSignupBinding
import com.kdy_soft.truck.ui.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "SignupFragment"

@AndroidEntryPoint
class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private lateinit var binding: FragmentSignupBinding

    private val signupViewModel: SignupViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        binding = _binding!!
        binding.viewModel = signupViewModel
        binding.spinnerCarKinds.adapter = carKindsAdapter()
        binding.spinnerCarKinds.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    signupViewModel.carKinds.value = VehicleKinds.values()[position]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        subscribeUi()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun subscribeUi() {
        signupViewModel
            .isSuccess
            .observe(viewLifecycleOwner) { isSuccess ->
                if (isSuccess) {
                    userViewModel.onAdded()
                    val dir = SignupFragmentDirections.actionDestSignupToDestDeliveryList()
                    findNavController().navigate(dir)
                }
            }
    }
}