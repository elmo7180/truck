package com.kdy_soft.truck.ui.search_address

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kdy_soft.truck.R
import com.kdy_soft.truck.data.model.Address
import com.kdy_soft.truck.databinding.FragmentSearchAddressBinding
import com.kdy_soft.truck.ui.LocationViewModel
import com.kdy_soft.truck.ui.hideSoftInput
import com.kdy_soft.truck.ui.home.DeliveryDetailFragment
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "SearchAddressFragment"

@AndroidEntryPoint
class SearchAddressFragment : Fragment() {
    private var _binding: FragmentSearchAddressBinding? = null
    private lateinit var binding: FragmentSearchAddressBinding

    private val adapter = AddressAdapter()
    private val args by navArgs<SearchAddressFragmentArgs>()
    private var type: Int = 0
    private val locationViewModel: LocationViewModel by activityViewModels()

    private val addressWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(e: Editable?) {
            e?.let {
                searchAddress()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = args.type
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchAddressBinding.inflate(inflater, container, false)
        binding = _binding!!
        binding.result.adapter = adapter

        val navController = findNavController()
        binding.toolbar.setupWithNavController(navController)

        binding.buttonSearch.setOnClickListener { searchAddress() }

        binding.query.addTextChangedListener(addressWatcher)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        binding.query.removeTextChangedListener(addressWatcher)
    }

    private fun searchAddress() {
        val query = binding.query.text.toString()
        locationViewModel.searchAddressByKeyword(query)
    }

    private fun setAddress(address: Address) {
        when (type) {
            resources.getInteger(R.integer.search_address_location) -> {
                locationViewModel.position.value = address.latLng
            }
            resources.getInteger(R.integer.search_address_departure) -> {
                val result = bundleOf(DeliveryDetailFragment.DEPARTURE_KEY to address)
                setFragmentResult(DeliveryDetailFragment.REQUEST_DEPARTURE, result = result)
            }
            resources.getInteger(R.integer.search_address_destination) -> {
                val result = bundleOf(DeliveryDetailFragment.DESTINATION_KEY to address)
                setFragmentResult(DeliveryDetailFragment.REQUEST_DESTINATION, result = result)
            }
        }
        hideSoftInput(binding.query.windowToken, 0)
        findNavController().navigateUp()
    }

    private fun subscribeUi() {
        locationViewModel
            .addresses.observe(viewLifecycleOwner) {
                adapter.submitList(it.addresses)
            }
    }

    inner class AddressAdapter :
        ListAdapter<Address, AddressAdapter.AddressHolder>(DIFF_ITEM_CALLBACK) {

        inner class AddressHolder(
            private val itemView: View
        ) : RecyclerView.ViewHolder(itemView) {
            private val placeName = itemView.findViewById<TextView>(android.R.id.text1)
            private val addressName = itemView.findViewById<TextView>(android.R.id.text2)

            fun bind(address: Address) {
                placeName.text = address.placeName
                addressName.text = address.addressName
                itemView.setOnClickListener { setAddress(address) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressHolder {
            val itemView = LayoutInflater
                .from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)

            return AddressHolder(itemView)
        }

        override fun onBindViewHolder(holder: AddressHolder, position: Int) {
            holder.bind(getItem(position))
        }

    }

    companion object {
        private val DIFF_ITEM_CALLBACK = object : DiffUtil.ItemCallback<Address>() {
            override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
                return oldItem.addressName == newItem.addressName
            }

            override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
                return oldItem == newItem
            }
        }
    }
}