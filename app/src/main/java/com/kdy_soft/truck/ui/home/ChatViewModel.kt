package com.kdy_soft.truck.ui.home

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.kdy_soft.truck.data.entity.Chat
import com.kdy_soft.truck.data.entity.ChatRoom
import com.kdy_soft.truck.data.model.ChatModel
import com.kdy_soft.truck.data.model.ChatRoomModel
import com.kdy_soft.truck.data.repository.ChatRepository
import com.kdy_soft.truck.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

private const val TAG = "ChatViewModel"

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val uid: String = Firebase.auth.uid ?: ""

    val destDeliveryList = MutableLiveData(false)
    val isShowRoom = MutableLiveData(false)
    val selectedChatRoomId = MutableLiveData("")

    private val selectedChatOpponent = mutableMapOf<String, String>()
    private val _showButton = MediatorLiveData<Int>()
    val showButton: LiveData<Int> = _showButton

    val chatRooms: LiveData<List<ChatRoomModel>> =
        MutableLiveData<List<ChatRoomModel>>(emptyList()).apply {
            val chatRoomListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val list = mutableListOf<ChatRoomModel>()
                        val latch = CountDownLatch(snapshot.childrenCount.toInt())

                        Log.d(TAG, "onDataChange")
                        snapshot
                            .children
                            .forEach { room ->
                                viewModelScope.launch {
                                    val chatRoom = room.getValue(ChatRoom::class.java)
                                    Log.d(TAG, "chatRoom:${chatRoom}")

                                    if (chatRoom != null) {
                                        getOpponentProfile(chatRoom) { profile ->
                                            val roomModel = convertChatRoomModel(
                                                chatRoom,
                                                profile
                                            )
                                            Log.d(TAG, "chatRoomModel:${roomModel}")
                                            list.add(roomModel)
                                            latch.countDown()
                                        }
                                    } else {
                                        latch.countDown()
                                    }
                                }
                            }

                        viewModelScope
                            .launch(Dispatchers.Default) {
                                latch.await()
                                postValue(list)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
            chatRepository.getChatRooms(uid, chatRoomListener)
        }


    val chats = Transformations.switchMap(selectedChatRoomId) { id ->
        val data = MutableLiveData<List<ChatModel>>(emptyList())
        val list = mutableListOf<ChatModel>()

        data.apply {
            val chatListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chat = snapshot.getValue(Chat::class.java)
                    chat?.let {
                        val chatModel =
                            convertChatModel(
                                chat,
                                selectedChatOpponent
                            )
                        list.add(chatModel)
                        data.postValue(list)
                    }

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {

                }

            }
            chatRepository.getChats(id, chatListener, page = 1)
        }
        data
    }

    fun selectRoomId(selected: String) {
        chatRooms.value?.forEach { room ->
            if (room.id == selected) {
                selectedChatOpponent[UserRepository.KEY_NAME] = room.name
                selectedChatOpponent[UserRepository.KEY_PROFILE] = room.profileUrl
            }
        }

    }

    private fun getOpponentProfile(
        room: ChatRoom,
        callback: (Map<String, String>) -> Unit
    ) {
        room.participantIds.iterator().forEach { ids ->
            val id = ids.key

            if (id != uid) {
                userRepository.getProfile(id) { profileMap ->
                    callback.invoke(profileMap)
                }

            }
        }
    }

    private fun convertChatRoomModel(
        room: ChatRoom,
        profileMap: Map<String, String>
    ): ChatRoomModel {
        return ChatRoomModel(
            id = room.id,
            recentDate = Date(room.recentTime),
            recentMessage = room.recentMsg,
            name = profileMap[UserRepository.KEY_NAME]!!,
            profileUrl = profileMap[UserRepository.KEY_PROFILE]!!
        )

    }

    private fun convertChatModel(
        chat: Chat,
        profileMap: Map<String, String>
    ): ChatModel {
        return ChatModel(
            id = chat.id,
            isMe = chat.uid == uid,
            name = profileMap[UserRepository.KEY_NAME] ?: "",
            profileUrl = profileMap[UserRepository.KEY_PROFILE] ?: "",
            msg = chat.msg,
            date = Date(chat.time)
        )
    }

    fun sendMessage(msg: String) {
        val roomId = selectedChatRoomId.value ?: ""
        if (roomId.isNotBlank() && uid.isNotBlank()) {
            chatRepository.sendMessage(roomId, msg, uid)
        }

    }

    init {
        _showButton.apply {
            value = HIDE_FAB
            addSource(destDeliveryList) {
                it?.let {
                    value = if (it && chatRooms.value?.isNotEmpty() == true) {
                        if (isShowRoom.value == true) {
                            SHOW_BACK_BUTTON
                        } else {
                            SHOW_CHAT_BUTTON
                        }
                    } else {
                        HIDE_FAB
                    }
                }
            }

            addSource(chatRooms) { rooms ->
                rooms?.let {
                    value = if (it.isNotEmpty() || destDeliveryList.value == true) {
                        if (isShowRoom.value == true) {
                            SHOW_BACK_BUTTON
                        } else {
                            SHOW_CHAT_BUTTON
                        }
                    } else {
                        HIDE_FAB
                    }
                }
            }

            addSource(isShowRoom) {
                it?.let {
                    value = if (destDeliveryList.value != true) {
                        HIDE_FAB
                    } else {
                        if (it) {
                            SHOW_BACK_BUTTON
                        } else {
                            SHOW_CHAT_BUTTON
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val HIDE_FAB = 0
        const val SHOW_CHAT_BUTTON = 1
        const val SHOW_BACK_BUTTON = 2
    }
}