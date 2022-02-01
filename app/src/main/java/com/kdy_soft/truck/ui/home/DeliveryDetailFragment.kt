package com.kdy_soft.truck.ui.home

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.kdy_soft.truck.R
import com.kdy_soft.truck.adapter.carKindsAdapter
import com.kdy_soft.truck.data.entity.VehicleKinds
import com.kdy_soft.truck.data.model.Address
import com.kdy_soft.truck.databinding.FragmentDeliveryDetailBinding
import com.kdy_soft.truck.ui.LocationViewModel
import com.kdy_soft.truck.ui.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "DeliveryDetailFragment"

@AndroidEntryPoint
class DeliveryDetailFragment : Fragment() {
    private var _binding: FragmentDeliveryDetailBinding? = null
    private lateinit var binding: FragmentDeliveryDetailBinding
    private val args by navArgs<DeliveryDetailFragmentArgs>()
    private val captureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { updatePhotoView() }

    private val detailViewModel: DeliveryDetailViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val deliveryListener = { view: View ->
        detailViewModel.doDelivery(findNavController())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deliveryId = args.id

        detailViewModel.id.value = deliveryId
        detailViewModel.location =
            locationViewModel.position.value ?: LatLng(0.0, 0.0)

        setFragmentResultListener(REQUEST_DATE) { _, bundle ->
            val timeInMillis = bundle.getLong(DATE_KEY, 0)
            if (timeInMillis != 0L) {
                detailViewModel.deadline.value = timeInMillis
            }
        }

        setFragmentResultListener(REQUEST_TIME) { _, bundle ->
            val time = bundle.getLong(TIME_KEY, 0)
            if (time != 0L) {
                detailViewModel.deadline.value = time
            }

        }
        setFragmentResultListener(REQUEST_DESTINATION) { _, bundle ->
            val dest = bundle.get(DESTINATION_KEY) as Address
            detailViewModel.destination.value = dest
            Log.d(TAG, "dest:$dest")
        }

        setFragmentResultListener(REQUEST_DEPARTURE) { _, bundle ->
            val departure = bundle.get(DEPARTURE_KEY) as Address
            detailViewModel.departure.value = departure
            Log.d(TAG, "departure:$departure")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeliveryDetailBinding.inflate(
            inflater,
            container,
            false
        )
        binding = _binding!!
        binding.viewModel = detailViewModel
        userViewModel.user.value?.vehicleKinds?.let {
            detailViewModel.myCarKinds = VehicleKinds.valueOf(it)
        }
        binding.deliveryButton.setOnClickListener(deliveryListener)
        binding.departure.setOnClickListener {
            val dir = DeliveryDetailFragmentDirections
                .actionDestDeliveryDetailToDestSearchAddress(
                    resources.getInteger(R.integer.search_address_departure)
                )
            findNavController().navigate(dir)
        }
        binding.deadlineTime.setOnClickListener {
            val dir = DeliveryDetailFragmentDirections
                .actionDestDeliveryDetailToDestTimePicker(
                    detailViewModel.deadline.value ?: 0L
                )
            findNavController().navigate(dir)
        }

        binding.deadlineDate.setOnClickListener {
            Log.d(TAG, "deadline: ${detailViewModel.deadline.value}")
            val dir = DeliveryDetailFragmentDirections
                .actionDestDeliveryDetailToDestDatePicker(
                    detailViewModel.deadline.value ?: 0L
                )
            findNavController().navigate(dir)
        }

        binding.destination.setOnClickListener {
            val dir = DeliveryDetailFragmentDirections
                .actionDestDeliveryDetailToDestSearchAddress(
                    resources.getInteger(R.integer.search_address_destination)
                )
            findNavController().navigate(dir)
        }

        binding.productImage.setOnClickListener {
            val packageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, detailViewModel.photoUri)

            val compName = captureImage.resolveActivity(packageManager)

            if (compName == null) {
                Log.d(TAG, "compName == null")
            }

            val cameraActivities = packageManager.queryIntentActivities(
                captureImage, PackageManager.MATCH_DEFAULT_ONLY
            )
            for (cameraActivity in cameraActivities) {
                requireActivity().grantUriPermission(
                    cameraActivity.activityInfo.packageName,
                    detailViewModel.photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }

            captureLauncher.launch(captureImage)
        }

        binding.spinnerCarKinds.adapter = carKindsAdapter()
        binding.spinnerCarKinds.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    detailViewModel.vehicleKinds.value = position
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }
        setupToolbar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        subscribeUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        detailViewModel.photoUri?.let { uri ->
            requireActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

    }

    private fun updatePhotoView() {
        Log.d(TAG, "updatePhotoView")
        detailViewModel.deliveryPhotoFile?.let { file ->
            Log.d(TAG, "file:$file")
            if (file.exists()) {
                Log.d(TAG, "file is exists")

                Glide.with(binding.productImage)
                    .load(file)
                    .into(binding.productImage)
            }
        }
    }

    private fun setupToolbar() {
        val navController = findNavController()
        binding.toolbar.setupWithNavController(navController)
    }

    private fun subscribeUi() {
        detailViewModel.editing.observe(viewLifecycleOwner) {
            binding.spinnerCarKinds.isEnabled = it
        }
    }

    companion object {
        const val REQUEST_DEPARTURE = "request_departure"
        const val REQUEST_DESTINATION = "request_destination"
        const val REQUEST_DATE = "request_date"
        const val REQUEST_TIME = "request_time"
        const val TIME_KEY = "time"
        const val DEPARTURE_KEY = "departure"
        const val DESTINATION_KEY = "destination"
        const val DATE_KEY = "date"
    }
}