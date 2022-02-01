package com.kdy_soft.truck.ui.login

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kdy_soft.truck.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    interface GoogleLoginCallback {
        fun onGoogleLogin()
    }

    private val _loading = MutableLiveData(false)

    val loading: LiveData<Boolean> = _loading
    var googleClient: GoogleSignInClient? = null
    var googleCallback: GoogleLoginCallback? = null

    fun googleLogin(view: View) {
        googleCallback?.onGoogleLogin()
    }

    fun kakaoLogin(view: View) {

    }

    fun emailLogin(view: View) {

    }

    fun onLoading() {
        _loading.value = true
    }

    fun onComplete() {
        _loading.value = false
        googleClient?.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        googleClient = null
    }
}