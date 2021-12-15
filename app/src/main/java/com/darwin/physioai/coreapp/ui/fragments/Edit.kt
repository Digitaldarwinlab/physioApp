package com.darwin.physioai.coreapp.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.darwin.physioai.R
import com.darwin.physioai.databinding.EditFragmentBinding
import com.example.physioai.data.models.profileResponse
import com.example.physioai.data.network.Resource
import com.darwin.physioai.coreapp.utils.CShowProgress
import com.darwin.physioai.coreapp.utils.Constants
import com.darwin.physioai.coreapp.utils.SessionManager
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class Edit : Fragment(R.layout.edit_fragment) {

    private lateinit var binding : EditFragmentBinding
    @Inject
    lateinit var sessionManager : SessionManager
    @Inject lateinit var progress : CShowProgress

    private var dob : String = "";
    private var Address1 : String = "";
    private var city : String = "";
    private var State : String = "";
    private var Country : String = "";
    private var mobilenum : String = "";
    private var Landlinenum : String = "";
    private var Whatsappnum : String = "";
    private var emailid  : String = "";
    private var pincode : String = "";
    private var emergencycontact : String = "";

    private val viewModel : ProfileViewModel by viewModels<ProfileViewModel>()
    private val editViewModel : EditViewModel by viewModels<EditViewModel>()
    private var userid : String? = null
    private var parseInt : Int? = null
    private var listprofile: List<profileResponse> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = EditFragmentBinding.bind(view)
        sessionManager = SessionManager(requireContext())
        userid = sessionManager.getStringData(Constants.USER_ID).toString()

        getProfileData()
        setuplistner()

        binding.apply {
            cancel.setOnClickListener {
                Navigation.findNavController(requireView()).navigate(R.id.action_edit_to_profile)
            }
        }
    }

    private fun getProfileData() {
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
                            binding.name.text = "${details.first_name} ${details.last_name}"
                            binding.PatientNam.setText(details.first_name)
//                          binding.PatientmiddleNam.setText(details.middle_name)
                            binding.PatientlasNam.setText(details.last_name)
                            binding.mobileNum.setText(details.mobile_no)
                            binding.Emailid.setText(details.email)

//                      EDITABLE FIELDS
                            dob = details.dob.toString()
                            city = details.city.toString()
                            Landlinenum = details.landline.toString()
                            Whatsappnum = details.whatsapp_no.toString()
                            emergencycontact = details.emergence_contact.toString()
                            pincode = details.pin.toString()
                            binding.DOB.setText(details.dob)
                            binding.City.setText(details.city)
                            binding.AddressData.setText(details.Address_1)
                            binding.landlineNum.setText(details.landline)
                            binding.whatsAppNum.setText(details.whatsapp_no)
                            binding.Pincode.setText(details.pin.toString())
                            binding.EmergencyContact.setText(details.emergence_contact.toString())

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

    private fun setuplistner() {
        binding.apply {
            update.setOnClickListener {
                dob = DOB.text.toString()
                city =  City.text.toString().trim()
                Landlinenum = landlineNum.text.toString()
                Whatsappnum = whatsAppNum.text.toString()
                pincode = Pincode.text.toString()
                emergencycontact = EmergencyContact.text.toString()

                Apicall()
            }

            imageView.setOnClickListener {
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(requireContext(), { view, year, monthOfYear, dayOfMonth ->
                    val mon = monthOfYear+1
                    DOB.setText( "$year-$mon-$dayOfMonth")
                }, year, month, day)

                dpd.show()
            }
        }
    }

    private fun Apicall() {

        val jsonobj = JsonObject()
        jsonobj.addProperty("id", parseInt)

        if (dob.isNotEmpty()){
            jsonobj.addProperty("dob", dob)
        }else{
            dob= "NA"
        }

        if (city.isNotEmpty()){
            jsonobj.addProperty("city", city)
        }else{
            city= "NA"
        }

        if (pincode.isNotEmpty()){
            jsonobj.addProperty("pin", pincode)
        }else{
            pincode= "NA"
        }

        if (Whatsappnum.isNotEmpty()){
            jsonobj.addProperty("whatsapp_no", Whatsappnum)
        }else{
            Whatsappnum = "NA"
        }

        if (Landlinenum.isNotEmpty()){
            jsonobj.addProperty("landline", Landlinenum)
        }else{
            Landlinenum = "NA"
        }


        editViewModel.apply {
            updateProfile(jsonobj)
            EditRes.observe(viewLifecycleOwner){
                when (it) {
                    is Resource.Success -> {
                        progress.hideProgress()
                        Toast.makeText(requireContext(), "Data Updated", Toast.LENGTH_SHORT).show()
                        Navigation.findNavController(requireView()).navigate(R.id.action_edit_to_profile)
//                        try {
//                            Toast.makeText(requireActivity(), it.value.message.toString(), Toast.LENGTH_SHORT).show()
//                        } catch (e: NullPointerException) {
//                            Toast.makeText(
//                                requireActivity(),
//                                "oops..! Something went wrong.",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
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


