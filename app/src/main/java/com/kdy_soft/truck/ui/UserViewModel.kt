package com.kdy_soft.truck.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kdy_soft.truck.data.entity.User
import com.kdy_soft.truck.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "UserViewModel"

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    private val auth = Firebase.auth
    private val _user = MutableLiveData<User?>(null)
    private val _initiating = MutableLiveData(true)
    private val _goToSignup = MutableLiveData(false)

    val goToSignup: LiveData<Boolean> = _goToSignup
    val initiating: LiveData<Boolean> = _initiating
    val user: LiveData<User?> = _user

    private val authStateListener = FirebaseAuth.AuthStateListener {
        val uid = it.uid

        if (uid == null) {
            _user.value = null
            _initiating.value = false
            return@AuthStateListener
        }

        viewModelScope.launch {
            userRepository.getUser(uid) { data ->
                _user.postValue(data)
                _initiating.postValue(false)
                _goToSignup.postValue(data == null)
            }
        }

    }

    fun onAdded() {
        val uid = auth.uid ?: return

        viewModelScope.launch {
            userRepository.getUser(uid) { data ->
                _user.postValue(data)
            }
        }
    }

    private fun signOut() {
        _user.value = null
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }


}