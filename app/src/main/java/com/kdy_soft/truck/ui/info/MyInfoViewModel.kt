package com.kdy_soft.truck.ui.info

import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import androidx.navigation.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kdy_soft.truck.R
import com.kdy_soft.truck.data.entity.User
import com.kdy_soft.truck.data.entity.VehicleKinds
import com.kdy_soft.truck.data.repository.UserRepository
import com.kdy_soft.truck.ui.login.SignupFragmentDirections
import com.kdy_soft.truck.util.TruckUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.NullPointerException
import javax.inject.Inject

private const val TAG = "MyInfoViewModel"

@HiltViewModel
class MyInfoViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _loading = MutableLiveData(false)
    private val _isSuccess = MutableLiveData(false)

    private val auth = Firebase.auth

    val user = MutableLiveData<User>()

    val isAdmin = Transformations.map(user){
        it?.mobile == "관리자"
    }
    val name: MutableLiveData<String?> =
        Transformations.map(user) { it.name } as MutableLiveData<String?>
    val mobile = Transformations.map(user) { it.mobile } as MutableLiveData<String?>
    val carNumber = Transformations.map(user) { it.vehicleNumber } as MutableLiveData<String?>
    val carKinds = Transformations.map(user) {
        VehicleKinds.valueOf(it.vehicleKinds).indexOf()
    } as MutableLiveData<Int?>

    val loading: LiveData<Boolean> = _loading
    val isSuccess: LiveData<Boolean> = _isSuccess
    val editing = MutableLiveData(false)

    fun signOut(view: View) {
        view.findNavController().navigate(R.id.nav_main)

        auth.signOut()
    }

    fun addDataForAdmin(){
        TruckUtils.addDataForAdmin()
    }

    private fun nameValidate() = name.value?.isNotBlank() == true
    private fun mobileValidate() = mobile.value?.matches(Regex("")) == true
    private fun carNumberValidate() = carNumber.value?.matches(Regex("")) == true

    private fun validate(): Boolean {
        return true
//        if(!nameValidate()) return false
//        if(!mobileValidate()) return false
//        if(!carNumberValidate()) return false
//        return true
    }

    fun apply(view: View) {
        if (validate()) {
            val user = user.value?.copy(
                name = name.value!!,
                mobile = mobile.value!!,
                vehicleNumber = carNumber.value!!,
                vehicleKinds = VehicleKinds.values()[carKinds.value!!].toString()
            )
            editing.value = false
            user?.let {
                viewModelScope.launch {
                    userRepository.addUser(user)
                }
            }
        } else {
            //TODO notify 검증 실패
            Toast.makeText(
                view.context,
                "변경사항을 확인해주세요.",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    fun uploadProfile(uri: Uri){
        userRepository.uploadProfile(uri)
    }
}