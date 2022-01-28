package com.darwin.physioai.coreapp.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.darwin.physioai.R

import com.darwin.physioai.coreapp.ui.MainActivity
import com.darwin.physioai.coreapp.utils.CShowProgress
import com.darwin.physioai.coreapp.utils.Constants
import com.darwin.physioai.coreapp.utils.SessionManager
import com.darwin.physioai.databinding.ProfileFragmentBinding
import com.example.physioai.data.models.profileResponse
import com.example.physioai.data.network.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class Profile : Fragment(R.layout.profile_fragment) {

    private var backPressedOnce = false
    private var binding : ProfileFragmentBinding? = null
    private val viewModel : ProfileViewModel by viewModels<ProfileViewModel>()
    @Inject
    lateinit var sessionManager : SessionManager
    @Inject lateinit var progress : CShowProgress
    private var userid : String? = null
    private var parseInt : Int? = null
    private var listprofile: List<profileResponse> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    Toast.makeText(requireContext(), "Press BACK again to exit", Toast.LENGTH_SHORT).show()

                    if (backPressedOnce){
                        requireActivity().finish()
                        return
                    }

                    backPressedOnce = true
                    lifecycleScope.launch{
                        delay(2000)
                        backPressedOnce = false
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ProfileFragmentBinding.bind(view)
        sessionManager = SessionManager(requireContext())
        userid = sessionManager.getStringData(Constants.USER_ID).toString()

        displayProfile()

        binding?.apply {
            logout.setOnClickListener {
                showLogoutDialog()
            }
        }

        binding?.apply {
            editb.setOnClickListener {
               // Navigation.findNavController(requireView()).navigate(R.id.action_profile_to_edit)
//                val i  = Intent(requireContext(), Agora::class.java)
//                startActivity(i)
            }
        }
    }

    private fun displayProfile() {
        val jsonobj = JsonObject()
        parseInt = userid?.toInt()
        Log.d("LogProfileId", parseInt.toString())
        jsonobj.addProperty("id", parseInt)
        viewModel.apply {
            getprofileRes(jsonobj)
            ProfileRes.observe(viewLifecycleOwner){
                when (it) {
                    is Resource.Success -> {
                        progress.hideProgress()
                        try {
                            listprofile = listOf(it.value)
                            val details = listprofile[0]
                            binding?.name?.text = "${details.first_name} ${details.last_name}"

                            binding?.bloodgrp?.text = details.blood_group
                            binding?.dob?.text = details.dob
                            binding?.email?.text = details.email
                            binding?.mobile?.text = details.mobile_no
                            binding?.whatsapp?.text = details.whatsapp_no
                            binding?.emergency?.text = details.emergence_contact.toString()

//                            val args = Bundle()
//                            args.putString("name", "${details.first_name} ${details.last_name}")
//                            args.putString("mobile", details.mobile_no)
//                            args.putString("landline", details.landline)
//                            args.putString("email", details.email)
//                            args.putString("whatsapp", details.whatsapp_no)
//                            args.putString("pincode", details.pin.toString())
//                            args.putString("emergency", details.emergence_contact.toString())
//
//                            val newFragment = Edit()
//                            newFragment.arguments = args

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

    private fun showLogoutDialog() {
        val builder = MaterialAlertDialogBuilder(requireActivity()).apply {
            setTitle("LOGOUT")
            setMessage("Are you sure ?")
            setPositiveButton("yes") { dialog: DialogInterface?, which: Int ->
                sessionManager.removeAll()
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                requireActivity().finish()
            }
            setNegativeButton("no", null)
            show()
        }
    }
}