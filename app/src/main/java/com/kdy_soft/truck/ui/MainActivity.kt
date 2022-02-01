package com.kdy_soft.truck.ui

import android.Manifest
import android.animation.Animator
import android.graphics.drawable.AnimatedStateListDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.kdy_soft.truck.NavGraphDirections
import com.kdy_soft.truck.R
import com.kdy_soft.truck.databinding.ActivityMainBinding
import com.kdy_soft.truck.ui.home.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private lateinit var binding: ActivityMainBinding

    private val userViewModel: UserViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()

    private val hideChatAnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
            super.onAnimationStart(animation, isReverse)
        }

        override fun onAnimationStart(p0: Animator?) {

        }


        override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
            binding.fabConversation.hide()
        }

        override fun onAnimationEnd(p0: Animator?) {
            binding.fabConversation.hide()
        }

        override fun onAnimationCancel(p0: Animator?) {

        }

        override fun onAnimationRepeat(p0: Animator?) {

        }

    }

    private val showBackAnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
            super.onAnimationStart(animation, isReverse)
        }

        override fun onAnimationStart(p0: Animator?) {

        }

        override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
            binding.fabConversation.hide()

            binding.fabBack.animate()
                .setDuration(150)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .withEndAction { binding.fabBack.show() }
                .start()
        }

        override fun onAnimationEnd(p0: Animator?) {
            binding.fabConversation.hide()

            binding.fabBack.animate()
                .setDuration(150)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .withEndAction { binding.fabBack.show() }
                .start()
        }

        override fun onAnimationCancel(p0: Animator?) {

        }

        override fun onAnimationRepeat(p0: Animator?) {

        }
    }

    private val showChatAnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
            super.onAnimationStart(animation, isReverse)
        }

        override fun onAnimationStart(p0: Animator?) {

        }

        override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
            binding.fabConversation.show()
        }

        override fun onAnimationEnd(p0: Animator?) {
            binding.fabConversation.show()
        }

        override fun onAnimationCancel(p0: Animator?) {

        }

        override fun onAnimationRepeat(p0: Animator?) {

        }

    }

    private val idSet = setOf(
        R.id.nav_info,
        R.id.nav_main,
        R.id.nav_record
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        binding = _binding!!
        binding.lifecycleOwner = this
        binding.chatViewModel = chatViewModel
        binding.viewModel = userViewModel

        binding.fabBack.setOnClickListener {
            Log.d(TAG, "onFabBackClicked")
            onBackPressed()
        }

        binding.fabConversation.setOnClickListener {
            chatViewModel.isShowRoom.value = true
        }

        setContentView(binding.root)
        requestPermission()
        subscribeUi()

        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val controller = navHost.navController
        controller.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.dest_delivery_list) {
                chatViewModel.destDeliveryList.value = true
            } else {
                chatViewModel.apply {
                    destDeliveryList.value = false
                    selectedChatRoomId.value = ""
                    isShowRoom.value = false
                }
            }
        }

        binding.bottomNav.setupWithNavController(controller)
    }

    override fun onBackPressed() {
        if (chatViewModel.selectedChatRoomId.value?.isNotBlank() == true) {
            chatViewModel.selectedChatRoomId.value = ""
            return
        }

        if (chatViewModel.isShowRoom.value == true) {
            chatViewModel.isShowRoom.value = false
            return
        }

        val navController = findNavController(R.id.nav_host_fragment)
        if (!navController.popBackStack()) {
            finish()
        } else {
            val destId = navController.currentDestination?.id
            if (idSet.contains(destId)) {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun subscribeUi() {
        userViewModel.initiating.observe(this) {
            val complete = !it
            if (complete) {
                observeUser()
            }
        }

        chatViewModel.showButton.observe(this) {
            Log.d(TAG, "showButtonValue:$it")
            when (it) {
                ChatViewModel.HIDE_FAB -> {
                    hideBackButton()
                    hideChatButton()
                }

                ChatViewModel.SHOW_CHAT_BUTTON -> {
                    if (binding.fabBack.isShown) {
                        hideBackButton(showChat = true)
                    } else {
                        showChatButton()
                    }
                }

                ChatViewModel.SHOW_BACK_BUTTON -> {
                    showBackButton()
                }
            }
        }
    }

    private fun observeUser() {
        userViewModel.user.observe(this) {
            if (it == null) {
                val dir = NavGraphDirections.destToLogin()
                findNavController(R.id.nav_host_fragment).navigate(dir)
            } else {
                Firebase.messaging.token
                findNavController(R.id.nav_host_fragment).navigate(R.id.nav_main)
            }
        }
    }

    private fun hideChatButton() {
        Log.d(TAG, "hideChat")
        val chatButton = binding.fabConversation
        chatButton.animate()
            .setDuration(300)
            .setListener(hideChatAnimatorListener)
            .scaleX(0.0f).scaleY(0.0f)
            .start()
    }

    private fun showChatButton() {
        Log.d(TAG, "showChat")
        val chatButton = binding.fabConversation

        chatButton.animate()
            .setDuration(300)
            .setListener(showChatAnimatorListener)
            .scaleX(1.0f).scaleY(1.0f)
            .start()
    }

    private fun showBackButton() {
        Log.d(TAG, "showBack")
        val chatButton = binding.fabConversation

        chatButton
            .animate()
            .setDuration(150)
            .setListener(showBackAnimatorListener)
            .scaleX(0.0f)
            .scaleY(0.0f)
            .start()
    }

    private fun hideBackButton(showChat: Boolean = false) {
        Log.d(TAG, "hideBack")
        val backButton = binding.fabBack

        if (showChat) {
            backButton.animate()
                .setDuration(150)
                .scaleX(0.0f)
                .scaleY(0.0f)
                .withEndAction {
                    showChatButton()
                    backButton.hide()
                }
                .start()
        } else {
            backButton.animate()
                .setDuration(300)
                .withEndAction { backButton.hide() }
                .scaleX(0.0f)
                .scaleY(0.0f)
                .start()
        }
    }

    private fun requestPermission() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                //TODO insert into shared preferences
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    //coarse location granted
                }

                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    //fine location granted
                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
}