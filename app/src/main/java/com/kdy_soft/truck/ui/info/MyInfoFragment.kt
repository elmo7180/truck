package com.kdy_soft.truck.ui.info

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.kdy_soft.truck.R
import com.kdy_soft.truck.adapter.carKindsAdapter
import com.kdy_soft.truck.data.entity.VehicleKinds
import com.kdy_soft.truck.databinding.FragmentInfoBinding
import com.kdy_soft.truck.ui.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyInfoFragment : Fragment() {
    private var _binding: FragmentInfoBinding? = null
    private lateinit var binding: FragmentInfoBinding

    private val infoViewModel: MyInfoViewModel by viewModels()
    private lateinit var profileLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.let { intent ->
                    val photoUri = intent.data ?: return@registerForActivityResult
                    infoViewModel.uploadProfile(photoUri)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        binding = _binding!!

        binding.viewModel = infoViewModel
        binding.spinnerCarKinds.adapter = carKindsAdapter()

        val userViewModel = activityViewModels<UserViewModel>().value
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                infoViewModel.user.value = user
            }
        }

        binding.addData.setOnClickListener {
            infoViewModel.addDataForAdmin()
        }

        binding.spinnerCarKinds.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    infoViewModel.carKinds.value = position
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }

        binding.profile.setOnClickListener {
            findProfileImage()
        }

        toolbarSetup(binding.toolbar)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun toolbarSetup(toolbar: Toolbar) {
        val config = AppBarConfiguration(
            setOf(
                R.id.nav_main,
                R.id.nav_info,
                R.id.nav_record
            )
        )

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit -> {
                    infoViewModel.editing.value = true
                    true
                }
                R.id.action_settings -> {
                    findNavController().navigate(R.id.dest_settings)
                    true
                }
                R.id.action_done -> {
                    infoViewModel.apply(binding.root)
                    true
                }
                R.id.action_signout -> {
                    infoViewModel.signOut(binding.root)
                    true
                }
                else -> false
            }
        }
        toolbar.setupWithNavController(findNavController(), config)
    }

    private fun findProfileImage() {
        val findImage = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }

        profileLauncher.launch(findImage)
    }
}