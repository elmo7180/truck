package com.kdy_soft.truck.ui.record

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.kdy_soft.truck.R
import com.kdy_soft.truck.databinding.FragmentRecordDetailBinding
import com.kdy_soft.truck.ui.home.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*


private const val TAG = "RecordDetailFragment"

@AndroidEntryPoint
class RecordDetailFragment : Fragment() {
    private var _binding: FragmentRecordDetailBinding? = null
    private lateinit var binding: FragmentRecordDetailBinding

    private val detailViewModel: RecordDetailViewModel by viewModels()
    private val args: RecordDetailFragmentArgs by navArgs()
    private val mapFragment = SupportMapFragment.newInstance()

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailViewModel.recordId.value = args.recordId
        Log.d(TAG, "id: ${args.recordId}")
    }

    private fun setupBackDispatcher() {

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /* onBackButton -> hideChat*/
        _binding = FragmentRecordDetailBinding.inflate(inflater, container, false)
        binding = _binding!!
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = detailViewModel

        binding.chatView.setLifecycleOwner(viewLifecycleOwner)
        binding.toolbar.setupWithNavController(findNavController())

        mapSetup()
        subscribeUi()
        binding.chatView.setReadOnly()

        binding.showChat.setOnClickListener { onChatButtonClicked() }
        binding.productImage.setOnClickListener { showProductImage() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this) {
                //TODO how to do onBackPress logic
                if (binding.chatView.visibility == View.VISIBLE) {
                    Log.d(TAG,"hide chat")
                    hideChat()
                } else {
                    Log.d(TAG,"onBackPressed : [else]")
                    requireActivity().onBackPressed()
                }
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun mapSetup() {
        //TODO childFragment add to map fragment
        if (childFragmentManager.findFragmentByTag("mapFragment") == null) {

            childFragmentManager
                .beginTransaction()
                .add(R.id.mapFragment, mapFragment, "mapFragment")
                .commit()

            mapFragment.getMapAsync { googleMap ->
                googleMap.uiSettings.isZoomControlsEnabled = true
            }
        }


    }

    private fun subscribeUi() {
        detailViewModel.record.observe(viewLifecycleOwner) {
            val departure = it.departLocation.toLatLng()

            CoroutineScope(Dispatchers.IO).launch {
                val zoom = getZoomLevel(it.distance)

                CoroutineScope(Dispatchers.Main).launch {
                    Log.d(TAG, "zoom : $zoom")
                    mapFragment.getMapAsync { map ->
                        val position = CameraPosition
                            .builder()
                            .zoom(zoom)
                            .target(departure)
                            .build()

                        val cameraUpdate = CameraUpdateFactory
                            .newCameraPosition(position)

                        map.moveCamera(cameraUpdate)
                    }
                }
            }
        }

        detailViewModel.path.observe(viewLifecycleOwner) {
            addPolyline(it)
        }

        detailViewModel.chats.observe(viewLifecycleOwner) { chats ->

            binding.chatView.submitList(chats)
        }
    }

    private fun addPolyline(routes: List<LatLng>) {
        mapFragment.getMapAsync { googleMap ->
            val polylineOpt = PolylineOptions()

            routes.forEach {
                polylineOpt.add(it)
            }

            googleMap.addPolyline(polylineOpt)
        }
    }

    private fun onChatButtonClicked() {
        showChat()
        hideChatButton()
    }

    private fun showChat() {
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup, Slide(Gravity.BOTTOM))
        binding.chatView.visibility = View.VISIBLE
    }

    private fun hideChat() {
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup, Slide(Gravity.TOP))
        binding.chatView.visibility = View.GONE
    }

    private fun hideChatButton() {

    }

    private fun showChatButton() {

    }

    private fun showProductImage() {

    }

    private suspend fun getZoomLevel(meter: Int): Float {
        var level = 20.0f

        withContext(Dispatchers.Default) {
            /*
            Maybe this information is wrong
            * 20 : 1128
            * 19 : 2256 (20 Level * 2)
            * 18 : 4512
            * 17 : 9024
            * 16 : 18048
            * ...
            * 0 : (20 Level * 2^20)
            * */
            var base = 1128

            while (base < meter) {
                level -= 2
                base *= 2
            }
        }
        Log.d(TAG, "level:$level")
        return level
    }
}