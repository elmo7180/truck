package com.kdy_soft.truck.service

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

private const val TAG = "FCMService"

class FCMService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        /*
            1. foreground -> 화면 상단에 표시한다.
            2. background -> 알림을 띄운다.

            확인하는 방법 : 두가지 경우 모두 리시버에서 받는다.
            그 이후에 [동적 리시버], [매니페스트 리시버] 를 우선순위로 나눈다.
            동적리시버는 포어그라운드 , 매니페스트 리시버는 백그라운드 이벤트를 받는다.
        */

        Log.d(TAG, "From:${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data:${remoteMessage.data}")
        }

        remoteMessage.notification?.let { notification ->

        }
    }

    override fun onNewToken(token: String) {
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val uid = Firebase.auth.uid ?: return

        Firebase.database.getReference("token")
            .child(uid)
            .setValue(token)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "sendTokenToServer: [Success]")
                } else {
                    Log.w(TAG, "sendTokenToServer: [Fail]", it.exception)
                }
            }
    }
}