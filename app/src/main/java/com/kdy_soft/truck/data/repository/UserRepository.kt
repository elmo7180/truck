package com.kdy_soft.truck.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kdy_soft.truck.data.contract.USER_TABLE
import com.kdy_soft.truck.data.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UserRepository"

@Singleton
class UserRepository @Inject constructor() {
    private val firebaseDatabase = Firebase.database
    private val firebaseStorage = Firebase.storage

    suspend fun addUser(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            var complete = false
            var isSuccess = false

            firebaseDatabase
                .getReference(USER_TABLE)
                .child(user.uid)
                .setValue(user)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(TAG, "onComplete is success")
                        isSuccess = true
                    }
                    complete = true
                }

            while (true) {
                if (complete) {
                    break
                }
            }
            isSuccess
        }
    }

    fun getProfile(
        uid: String,
        callback: (Map<String, String>) -> Unit
    ) {
        val map = mutableMapOf(
            KEY_NAME to "",
            KEY_PROFILE to ""
        )

        val profileNode = firebaseDatabase
            .getReference(USER_TABLE)
            .child(uid)

        profileNode
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = it.result.getValue(User::class.java)

                    user?.let {
                        map[KEY_NAME] = user.name
                        map[KEY_PROFILE] = user.profileUrl
                    }

                    callback.invoke(map)
                } else {
                    callback.invoke(map)
                }
            }
    }

    fun getUser(uid: String, callback: (User?) -> Unit) {
        val userNode = firebaseDatabase
            .getReference(USER_TABLE)
            .child(uid)

        userNode.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = if (snapshot.exists()) {
                    snapshot.getValue(User::class.java)
                } else {
                    null
                }
                callback.invoke(user)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled", error.toException())
                callback.invoke(null)
            }

        })

    }

    fun setToken(uid: String, token: String) {
        firebaseDatabase.getReference("token")
            .child(uid)
            .setValue(token)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Success")
                } else {
                    Log.w(TAG, "Fail", it.exception)
                }
            }
    }

    fun uploadProfile(uri: Uri) {
        val uid = Firebase.auth.uid ?: ""

        if (uid.isBlank()) {
            return
        }

        val reference = firebaseStorage.getReference("profile")
            .child(uid)

        reference.putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    reference.downloadUrl.addOnCompleteListener { downloadUrl ->
                        if (downloadUrl.isSuccessful) {
                            val result = downloadUrl.result.toString()

                            firebaseDatabase.getReference(USER_TABLE)
                                .child("${uid}/profileUrl")
                                .setValue(result)
                        }
                    }
                }
            }
    }

    companion object {
        const val KEY_NAME = "name"
        const val KEY_PROFILE = "profile"
    }
}