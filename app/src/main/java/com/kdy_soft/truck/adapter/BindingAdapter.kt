package com.kdy_soft.truck.adapter

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.kdy_soft.truck.data.model.Address
import com.kdy_soft.truck.util.TruckUtils
import java.lang.StringBuilder
import java.util.*

private const val TAG = "BindingAdapter"

@BindingAdapter("isGone")
fun isGone(view: View, isGone: Boolean?) {
    isGone ?: return
    view.visibility = if (isGone) View.GONE else View.VISIBLE
}

@BindingAdapter("downloadImage")
fun downloadImage(iv: ImageView, url: String?) {
    if (url?.isNotBlank() == true) {
        Glide.with(iv)
            .load(url)
            .into(iv)
    }
}

@BindingAdapter("placeName", "addressName", requireAll = false)
fun setAddressNames(view: TextView, placeName: String?, addressName: String?) {
    Log.d(TAG, "setAddressName($placeName, $addressName)")
    val sb = StringBuilder()

    if (placeName != null && addressName != null) {
        sb.append(placeName)
            .append("\n")
            .append(addressName)
    } else {
        sb.append(placeName ?: "")
            .append(addressName ?: "")
            .append("\n")
    }
    view.text = sb.toString()
}

@BindingAdapter("bindRecentDate")
fun bindRecentDate(view: TextView, date: Date?){
    date?.let{
        val string = TruckUtils.getRecentDate(date)
        view.text = string
    }
}

@BindingAdapter("priceText")
fun priceText(view:TextView, price: Int?){
    price?.let{
        val priceString = TruckUtils.priceString(price)
        view.text = priceString
    }
}

@BindingAdapter("addressText")
fun addressText(view:TextView, address: Address?){
    address?.let{
        view.text = if(it.placeName.isNotBlank()){
            it.placeName
        }else {
            it.addressName
        }
    }
}