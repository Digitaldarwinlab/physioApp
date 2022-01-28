package com.darwin.physioai.coreapp.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.darwin.physioai.R
import com.darwin.physioai.databinding.LoginFragmentFragmentBinding
import com.example.physioai.data.network.Resource
import com.darwin.physioai.coreapp.utils.CShowProgress
import com.darwin.physioai.coreapp.utils.Constants
import com.darwin.physioai.coreapp.utils.SessionManager
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Login : Fragment(R.layout.login_fragment_fragment) {

    private var binding : LoginFragmentFragmentBinding? = null
    private val viewModel : LoginFragmentViewModel by viewModels<LoginFragmentViewModel>()
    @Inject lateinit var sessionManager : SessionManager
    @Inject lateinit var progress : CShowProgress
    private var userId : String? = null
    private var Passwrd : String? = null
    private var token :String? = null
    private var sessionid : String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = LoginFragmentFragmentBinding.bind(view)
        token =  sessionManager.getStringData(Constants.USER_TOKEN).toString()

        if(token!!.isNotEmpty()){
            Navigation.findNavController(requireView()).navigate(R.id.action_login_to_home)
        }

        binding?.apply {
            LoginButton.setOnClickListener{
                userId = userid.text.toString().trim()
                Passwrd = password.text.toString().trim()

                if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(Passwrd)){
                    Toast.makeText(requireContext(), "Please Enter UserId & Password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                Apicall()
            }

            forgetPass.setOnClickListener {
                Navigation.findNavController(requireView()).navigate(R.id.action_login_to_forget)
            }
        }
    }

    private fun Apicall() {
        val jsonobj = JsonObject()
        jsonobj.addProperty("uid", userId)
        jsonobj.addProperty("password", Passwrd)

        viewModel.apply {
            getUser(jsonobj)
            loginRes.observe(viewLifecycleOwner){
                when (it) {
                    is Resource.Success -> {
                        progress.hideProgress()
                        try {
                            token = it.value.jwt
                            sessionid = it.value.user_id.toString()
                            Log.d("LogTag", sessionid.toString())
                            sessionManager.apply {
                                putStringData(Constants.USER_TOKEN, token.toString())
                                putStringData(Constants.USER_ID, sessionid.toString())
                            }
                            Navigation.findNavController(requireView()).navigate(R.id.action_login_to_home)
                        } catch (e: NullPointerException) {
                            Toast.makeText(
                                requireActivity(),
                                "oops..! Something went wrong.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is Resource.Failure ->{
                        progress.hideProgress()
                        Toast.makeText(requireContext(), "Failed.", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading ->{
                        if(progress.mDialog?.isShowing == true){
                            progress.hideProgress()
                        }else{
                            progress.showProgress(requireContext())
                        }
                    }
                }
            }
        }
    }
}