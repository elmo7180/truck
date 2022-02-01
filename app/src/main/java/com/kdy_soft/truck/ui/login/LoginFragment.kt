package com.kdy_soft.truck.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kdy_soft.truck.R
import com.kdy_soft.truck.databinding.FragmentLoginBinding
import com.kdy_soft.truck.ui.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "LoginFragment"

@AndroidEntryPoint
class LoginFragment : Fragment(), LoginViewModel.GoogleLoginCallback {
    private var _binding: FragmentLoginBinding? = null
    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (ex: ApiException) {
            //TODO failed to google sign-in reload page
            Log.d(TAG, "catchException")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginViewModel.googleCallback = this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding = _binding!!

        binding.viewModel = loginViewModel
        loginViewModel.googleClient = getGoogleClient()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val host = requireActivity()
        host.onBackPressedDispatcher
            .addCallback(viewLifecycleOwner){
                host.finish()
            }

        binding.lifecycleOwner = viewLifecycleOwner
        subscribeUi()
    }


    override fun onDestroy() {
        super.onDestroy()

        loginViewModel.googleCallback = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onGoogleLogin() {
        setOnLoading()
        val signInIntent = loginViewModel.googleClient!!.signInIntent
        googleLauncher.launch(signInIntent)
    }

    private fun getGoogleClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_default_web_client_key))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        Firebase.auth
            .signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "isSuccess")
                } else {
                    Log.d(TAG, "firebaseAuthWithGoogleIsNotSuccess")
                }
                setOnComplete()

            }
    }

    private fun setOnLoading() {
        loginViewModel.onLoading()
    }

    private fun setOnComplete() {
        loginViewModel.onComplete()
    }

    private fun subscribeUi() {
        userViewModel.goToSignup.observe(viewLifecycleOwner){ goToSignup->
            if(goToSignup) {
                setOnComplete()
                val dir = LoginFragmentDirections.actionDestLoginToDestSignup()
                findNavController().navigate(dir)
            }
        }
    }

}