package com.kdy_soft.truck.ui.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.work.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.kdy_soft.truck.R
import com.kdy_soft.truck.adapter.ChatAdapter
import com.kdy_soft.truck.adapter.ChatRoomAdapter
import com.kdy_soft.truck.adapter.DeliveryAdapter
import com.kdy_soft.truck.data.entity.DeliveryRecord
import com.kdy_soft.truck.data.model.DeliveryModel
import com.kdy_soft.truck.data.repository.UserRepository
import com.kdy_soft.truck.databinding.FragmentDeliveryListBinding
import com.kdy_soft.truck.ui.LocationViewModel
import com.kdy_soft.truck.ui.UserViewModel
import com.kdy_soft.truck.ui.viewModel.DriverHelperViewModel
import com.kdy_soft.truck.util.TruckUtils
import com.kdy_soft.truck.worker.PathWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "DeliveryListFragment"

@AndroidEntryPoint
class DeliveryListFragment : Fragment(), DeliveryAdapter.OnItemClickListener {
    private var _binding: FragmentDeliveryListBinding? = null
    private lateinit var binding: FragmentDeliveryListBinding

    private val userViewModel: UserViewModel by activityViewModels()
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val deliveryViewModel: DeliveryListViewModel by viewModels()
    private val helperViewModel: DriverHelperViewModel by viewModels()

    private val chatViewModel: ChatViewModel by activityViewModels()

    private val adapter = DeliveryAdapter(this)
    private lateinit var chatRoomAdapter: ChatRoomAdapter

    private val mapFragment = SupportMapFragment.newInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeliveryListBinding.inflate(inflater, container, false)
        binding = _binding!!
        binding.searchAddress.setOnClickListener {
            searchAddress()
        }

        binding.viewModel = deliveryViewModel
        binding.chatViewModel = chatViewModel

        chatRoomAdapter = ChatRoomAdapter(viewLifecycleOwner)
        chatRoomAdapter.setItemClickListener { roomId ->
            chatViewModel.selectedChatRoomId.value = roomId
        }
        binding.lifecycleOwner = viewLifecycleOwner

        binding.deliveryList.adapter = adapter
        binding.chatRoomList.adapter = chatRoomAdapter

        binding.chat.setSendMessageCallback { msg ->
            chatViewModel.sendMessage(msg)
        }

        mapSetup()
        toolbarSetup()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUi()
    }

    @SuppressLint("PotentialBehaviorOverride", "MissingPermission")
    private fun mapSetup() {
        val currentChild = childFragmentManager.findFragmentByTag("mapFragment")

        if (currentChild == null) {
            childFragmentManager
                .beginTransaction()
                .add(R.id.mapView, mapFragment, "mapFragment")
                .commit()

            mapFragment.getMapAsync { googleMap ->
                googleMap.isMyLocationEnabled = locationViewModel.locationPermissionEnabled
                googleMap.setOnMyLocationButtonClickListener {
                    locationViewModel.location.value?.let {
                        val lat = it.latitude
                        val lon = it.longitude
                        locationViewModel.position.value = LatLng(lat, lon)
                    }
                    false
                }

                googleMap.uiSettings.isZoomControlsEnabled = true

                googleMap.setOnMapLoadedCallback {
                    googleMap.setMinZoomPreference(10f)
                    googleMap.setMaxZoomPreference(17f)

                    googleMap.setOnCameraMoveListener {
                        deliveryViewModel.focusPosition.value = LatLng(0.0, 0.0)

                        val cameraTarget = googleMap.cameraPosition.target
                        if (locationViewModel.outOfBoundary(cameraTarget)) {
                            deliveryViewModel.location.value = cameraTarget
                        }
                    }

                    googleMap.setOnMarkerClickListener { marker ->
                        if (marker.isInfoWindowShown) {
                            marker.hideInfoWindow()
                        } else {
                            marker.showInfoWindow()
                        }
                        true
                    }
                }
            }
        }
    }

    private fun toolbarSetup() {
        val controller = findNavController()

        binding.toolbar
            .apply {
                setupWithNavController(navController = controller)

                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_add_product -> {
                            val dir =
                                DeliveryListFragmentDirections.actionDestDeliveryListToDestDeliveryDetail()
                            findNavController().navigate(dir)
                            true
                        }

                        R.id.action_filter -> {
                            //todo backdrop to filter
                            true
                        }
                        else -> false
                    }

                }
            }
    }

    private fun subscribeDeliveryList() {
        deliveryViewModel
            .deliveryList
            .observe(viewLifecycleOwner) {
                Log.d(TAG, "list:$it")
                Log.d(TAG, it.toString())
                adapter.submitList(it)
                addMarkers(it)
            }
    }

    private fun showRecordWindow(record: DeliveryRecord) {
        //TODO record show wayBill
        Toast.makeText(requireContext(), "운송 완료", Toast.LENGTH_SHORT).show()
    }

    private fun subscribeCompleteRecord() {
        deliveryViewModel.completedRecord.observe(viewLifecycleOwner) { record ->
            record?.let {
                showRecordWindow(record)
            }
        }
    }

    private fun setDriverHelper(onDelivery: Boolean) {
        if (onDelivery) {
            subscribeCompleteRecord()
            deliveryViewModel.drivingDelivery.observe(viewLifecycleOwner) {
                it?.let {
                    helperViewModel.delivery.value = it
                }
            }

            deliveryViewModel.restDistance.observe(viewLifecycleOwner) {
                helperViewModel.restDistance.value = TruckUtils.distanceString(it)
            }

            deliveryViewModel.restTime.observe(viewLifecycleOwner) {
                val date = Date()
                date.time += it * 1000
                helperViewModel.approxTime.value = TruckUtils.getTimeString(date)
            }

            locationViewModel.location.observe(viewLifecycleOwner) {
                deliveryViewModel.velocity.value = it?.speed?.toInt() ?: 0
            }

            binding.driverHelper.setOnCompleteListener {
                deliveryViewModel.completeDelivery { record ->
                    if (record == null) {
                        //TODO fail
                        Toast
                            .makeText(
                                requireContext(),
                                "실패했습니다.",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    } else {
                        //TODO show wayBill
                        showRecordWindow(record)
                    }
                }
            }
            binding.driverHelper.setLifecycleOwner(viewLifecycleOwner)
            binding.driverHelper.setViewModel(helperViewModel)
        } else {
            binding.driverHelper.setLifecycleOwner(null)
            binding.driverHelper.setViewModel(null)
        }
    }

    private fun subscribeUi() {
        //운송중인 경우를 확인
        userViewModel.user.observe(viewLifecycleOwner) {
            it?.let { user ->
                deliveryViewModel.drivingId.value = user.deliveryId
                val onDelivery = user.deliveryId.isNotBlank()
                setDriverHelper(onDelivery)

                if (!onDelivery) {
                    subscribeDeliveryList()
                }
            }
        }
        //위치 변경시 체크
        locationViewModel
            .position
            .observe(viewLifecycleOwner) {
                deliveryViewModel.currentLocation.value = it
                deliveryViewModel.location.value = it
                it?.let {
                    moveCamera(it)
                }
            }

        //path
        deliveryViewModel.paths.observe(viewLifecycleOwner) {
            addPolyLines(it)
        }

        chatViewModel.chatRooms.observe(viewLifecycleOwner) {
            chatRoomAdapter.submitList(it)
        }

        chatViewModel.isShowRoom.observe(viewLifecycleOwner) {
            TransitionManager.beginDelayedTransition(
                binding.root as ViewGroup,
                Slide(Gravity.BOTTOM)
            )

            if (it) {
                binding.chatRoomList.visibility = View.VISIBLE
            } else {
                binding.chatRoomList.visibility = View.GONE
            }
        }

        chatViewModel.selectedChatRoomId.observe(viewLifecycleOwner) {

            TransitionManager.beginDelayedTransition(
                binding.root as ViewGroup,
                Slide(Gravity.BOTTOM)
            )

            if (it.isNotBlank()) {
                binding.chat.visibility = View.VISIBLE
                chatViewModel.selectRoomId(it)
            } else {
                binding.chat.visibility = View.GONE
            }

        }

        chatViewModel.chats.observe(viewLifecycleOwner) {
            binding.chat.submitList(it)
        }
    }

    private fun addPolyLines(routes: List<LatLng>) {
        mapFragment.getMapAsync { googleMap ->
            googleMap.addPolyline(
                PolylineOptions()
                    .color(Color.GREEN)
                    .addAll(routes)
            )
        }
    }

    private fun moveCamera(latLng: LatLng) {
        mapFragment.getMapAsync { googleMap ->

            googleMap.setOnMapLoadedCallback {
                googleMap.moveCamera(
                    CameraUpdateFactory
                        .newLatLngZoom(latLng, 15f)
                )
            }
        }
    }

    private fun addMarkers(deliveries: List<DeliveryModel>) {
        mapFragment.getMapAsync { googleMap ->
            deliveries.forEach {
                val marker = MarkerOptions()
                    .position(it.departure.latLng)
                    .title(it.departure.addressName)

                googleMap.addMarker(marker)
            }
        }
    }

    private fun searchAddress() {
        val dir = DeliveryListFragmentDirections
            .actionDestDeliveryListToDestSearchAddress(
                resources.getInteger(R.integer.search_address_location)
            )
        findNavController().navigate(dir)
    }

    private fun addPath(model: DeliveryModel) {
        val from = locationViewModel.position.value ?: return
        val to = model.departure.latLng

        deliveryViewModel.addPath(from, to)
    }

    override fun onItemClick(model: DeliveryModel, position: LatLng) {
        if (deliveryViewModel.focusPosition.value == position) {
            deliveryViewModel.focusPosition.value = null

            val dir = DeliveryListFragmentDirections
                .actionDestDeliveryListToDestDeliveryDetail(model.id)
            findNavController().navigate(dir)
        } else {
            moveCamera(position)
            addPath(model)
            deliveryViewModel.focusPosition.value = position
        }
    }

    private fun intervalAddPath() {
        val constraints = Constraints
            .Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<PathWorker>(
            15L,
            TimeUnit.MINUTES
        ).setConstraints(constraints)
            .build()

        WorkManager
            .getInstance(requireContext())
            .enqueueUniquePeriodicWork(
                PathWorker.WORKER_UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }
}