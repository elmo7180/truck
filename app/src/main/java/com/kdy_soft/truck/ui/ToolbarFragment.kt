package com.kdy_soft.truck.ui

import android.content.Context
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import android.widget.Toolbar
import androidx.fragment.app.Fragment

abstract class ToolbarFragment: Fragment() {
    fun setupToolbar(toolbar:Toolbar){

    }
}

fun Fragment.hideSoftInput(windowToken: IBinder, flag: Int){
    val imm: InputMethodManager =
        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0);
}