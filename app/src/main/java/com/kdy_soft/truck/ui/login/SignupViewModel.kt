package com.kdy_soft.truck.ui.login

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kdy_soft.truck.data.entity.User
import com.kdy_soft.truck.data.entity.VehicleKinds
import com.kdy_soft.truck.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.NullPointerException
import javax.inject.Inject

private const val TAG = "SignupViewModel"

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _loading = MutableLiveData(false)
    private val _isSuccess = MutableLiveData(false)

    private val auth = Firebase.auth

    val name = MutableLiveData("")
    val mobile = MutableLiveData("")
    val carNumber = MutableLiveData("")
    val carKinds = MutableLiveData(VehicleKinds.TOP1)

    val loading: LiveData<Boolean> = _loading
    val isSuccess: LiveData<Boolean> = _isSuccess

    fun signup(view: View) {
        if (validate()) {
            addUser()
        } else {
            //TODO 유효성 검증 실패 -> 세부화
        }
    }

    fun cancel(view: View) {
        Log.d(TAG,"onCancel")
        auth.signOut()
        
        val dir = SignupFragmentDirections.actionDestSignupToDestLogin()
        view.findNavController().navigate(dir)
    }

    private fun addUser() {
        try {
            val uid = auth.currentUser!!.uid

            val user = User(
                uid = uid,
                name = name.value!!,
                mobile = mobile.value!!,
                vehicleNumber = carNumber.value!!,
                vehicleKinds = carKinds.value!!.toString()
            )

            _loading.value = true
            Log.d(TAG,"user:$user")
            viewModelScope.launch {
                _isSuccess.postValue(userRepository.addUser(user))
            }
        } catch (ex: NullPointerException) {

        }
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

}