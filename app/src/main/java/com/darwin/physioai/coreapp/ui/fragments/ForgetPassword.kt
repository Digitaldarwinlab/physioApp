package com.darwin.physioai.coreapp.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.darwin.physioai.R
import com.darwin.physioai.databinding.ForgetPasswordFragmentBinding
import com.example.physioai.data.network.Resource
import com.darwin.physioai.coreapp.utils.CShowProgress
import com.darwin.physioai.coreapp.utils.SessionManager
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ForgetPassword : Fragment(R.layout.forget_password_fragment) {

    private var binding : ForgetPasswordFragmentBinding? = null
    private var userid :String? = null
    private val viewModel : ForgetPasswordViewModel by viewModels<ForgetPasswordViewModel>()
    @Inject
    lateinit var sessionManager : SessionManager
    @Inject
    lateinit var progress : CShowProgress

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = ForgetPasswordFragmentBinding.bind(view)

        binding?.apply {
            submit.setOnClickListener{
                userid = uid.text.toString()
                if(!TextUtils.isEmpty(userid)){
                    Apicall()
                }else{
                    Toast.makeText(requireContext(), "Please Enter UserId", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun Apicall() {
        val jsonobj = JsonObject()
        jsonobj.addProperty("uid", userid)
        viewModel.apply {
            getforpass(jsonobj)
            forgetpass.observe(viewLifecycleOwner){
                when (it) {
                    is Resource.Success -> {
                        progress.hideProgress()
                        Toast.makeText(requireActivity(), it.value.message.toString(), Toast.LENGTH_SHORT).show()
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