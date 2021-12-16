package com.darwin.physioai.coreapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.darwin.physioai.R
import com.darwin.physioai.databinding.HomeFragmentBinding
import com.example.physioai.data.models.HomeResponseItem
import com.example.physioai.data.models.TreatingDoctorDetail
import com.example.physioai.data.network.Resource
import com.darwin.physioai.coreapp.utils.CShowProgress
import com.darwin.physioai.coreapp.utils.Constants
import com.darwin.physioai.coreapp.utils.SessionManager
import com.darwin.physioai.coreapp.data.Adapter.TreatingDocAdapter
import com.example.physioai.data.models.profileResponse
import com.google.gson.JsonObject
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class Home : Fragment(R.layout.home_fragment) {

    private lateinit var binding: HomeFragmentBinding
    private lateinit var navController : NavController
    private val viewModel : HomeViewModel by viewModels<HomeViewModel>()
    private val profileViewModel : ProfileViewModel by viewModels<ProfileViewModel>()
    @Inject lateinit var sessionManager : SessionManager
    @Inject lateinit var progress : CShowProgress
    private var treatingAdapter: TreatingDocAdapter? = null
    private var backPressedOnce = false
    private var userid : String? = null
    private var parseInt : Int? = null
    private var episodeid : String? = null
    private var listprofile: List<profileResponse> = ArrayList()

    private var listhome: List<HomeResponseItem> = ArrayList()
    private var lisDocDetails: List<TreatingDoctorDetail> = ArrayList()
    private var homeresponseitem: HomeResponseItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
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
        binding = HomeFragmentBinding.bind(view)
        userid = sessionManager.getStringData(Constants.USER_ID).toString()
        Apicall()
        getEpisode()
        displayProfile(userid!!)
        initializeYoutubevideo()
//        binding.apply {
//            castBut.setOnClickListener {
//                startActivity(Intent("android.settings.CAST_SETTINGS"));
//            }
//        }
    }

    private fun initializeYoutubevideo() {
        val youTubePlayerView: YouTubePlayerView = binding.youtubePlayerView
        lifecycle.addObserver(youTubePlayerView)

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(@NonNull youTubePlayer: YouTubePlayer) {
                val videoId = "g_tea8ZNk5A"
                youTubePlayer.loadVideo(videoId, 0f)
                youTubePlayer.pause()
            }
        })
    }

    private fun displayProfile(userid: String) {
        val jsonobj = JsonObject()
        parseInt = userid?.toInt()
        Log.d("LogProfileId", parseInt.toString())
        jsonobj.addProperty("id", parseInt)
        profileViewModel.apply {
            getprofileRes(jsonobj)
            ProfileRes.observe(viewLifecycleOwner){
                when (it) {
                    is Resource.Success -> {
                        progress.hideProgress()
                        try {
                            listprofile = listOf(it.value)
                            val details = listprofile[0]
                            binding.patientName.text = "${details.first_name} ${details.last_name}"
                            binding.patientCode.text = details.patient_code
                            binding.patientNum.text = details.mobile_no

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

    private fun getEpisode() {
        val jsonobj = JsonObject()
        val parsedInt = userid?.toInt()
        jsonobj.addProperty("id", parsedInt)
        viewModel.apply {
            getUserData(jsonobj)
            homeRes.observe(viewLifecycleOwner){
                when (it) {
                    is Resource.Success -> {
                        progress.hideProgress()
                        try {
                            if(it.value.isNotEmpty()) {
                            episodeid = it.value[0].pp_ed_id.toInt().toString()
                            Log.d("LogTagEpisodeID", episodeid.toString())
                            sessionManager.apply {
                                putStringData(Constants.EPISODE_ID, episodeid.toString())
                            }
                            } else {

                            }
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

    private fun Apicall() {
        val jsonobj = JsonObject()
        val parsedInt = userid?.toInt()
        jsonobj.addProperty("id", parsedInt)
        viewModel.apply {
            getUserData(jsonobj)
            homeRes.observe(viewLifecycleOwner){
                when (it) {
                    is Resource.Success -> {
                        progress.hideProgress()
                        try {
                            if(it.value.isNotEmpty()) {
                            listhome = listOf(it.value[0])
                            lisDocDetails = listhome[0].treating_doctor_detail
                            homeresponseitem = listhome[0]
                            setupData(homeresponseitem!!)
                            setupRecycler(lisDocDetails)
                            } else {

                            }
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

    private fun setupRecycler(lisDocDetails: List<TreatingDoctorDetail>) {
        binding.apply {
            treatingAdapter = TreatingDocAdapter(lisDocDetails)
            treatingDocRecyler.layoutManager = LinearLayoutManager(requireContext())
            treatingDocRecyler.adapter = treatingAdapter
        }
    }

    private fun setupData(dat: HomeResponseItem) {
        binding.apply {
            binding.pmId.text = dat.pp_pm_id.toString()
            binding.edId.text = dat.pp_ed_id.toString()
            binding.epiNum.text = dat.episode_number.toString()
            binding.endDate.text = dat.end_date.toString()
            binding.refDrName.text = dat.treating_doc_details_mobile.Ref_Dr_Name
            binding.refDrId.text = dat.treating_doc_details_mobile.Ref_Dr_ID
//            binding.patientCode.text = dat.PP_Patient_Details_mobile.Patient_code.toString()
//            binding.patientName.text = dat.PP_Patient_Details_mobile.Patient_name.toString()
//            binding.patientNum.text = dat.PP_Patient_Details_mobile.Patient_no.toString()
            binding.username.text = dat.PP_Patient_Details_mobile.Patient_name.toString()
        }
    }
}