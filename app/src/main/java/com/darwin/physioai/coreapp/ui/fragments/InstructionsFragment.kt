package com.darwin.physioai.coreapp.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.darwin.physioai.R
import com.darwin.physioai.databinding.InstructionsFragmentBinding
import com.example.physioai.data.network.Resource
import com.darwin.physioai.coreapp.utils.CShowProgress
import com.darwin.physioai.coreapp.utils.SessionManager
import com.google.gson.JsonObject
import com.darwin.physioai.posenet.PoseNetActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InstructionsFragment : Fragment(R.layout.instructions_fragment) {

    private var binding : InstructionsFragmentBinding? = null
    private val viewModel : InstructionsViewModel by viewModels<InstructionsViewModel>()
    @Inject
    lateinit var sessionManager : SessionManager
    @Inject
    lateinit var progress : CShowProgress
    private var exercise : String? = null
    private var angle : String? = null
    val url = "https://myphysio.digitaldarwin.in/"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = InstructionsFragmentBinding.bind(view)
        sessionManager = SessionManager(requireContext())

        exercise = arguments?.getString("exercise").toString()
        angle = arguments?.getString("angle").toString()
        binding?.exerciseName?.text = exercise
        Apicall(exercise!!)

        binding?.apply {
            skip.setOnClickListener {
                val myIntent = Intent(requireContext(), PoseNetActivity::class.java)
                myIntent.putExtra("angle", angle)
                startActivity(myIntent)
            }
        }
    }

    private fun Apicall(exercise: String) {
        val jsonobj = JsonObject()
        jsonobj.addProperty("exercise", exercise)
        viewModel.apply {
            getInstructions(jsonobj)
            instructionsRes.observe(viewLifecycleOwner){
                when (it) {
                    is Resource.Success -> {
                        progress.hideProgress()
                        try {
                            val v = (url + it.value[0].video_path)
                            binding!!.instr.text = it.value[0].instruction1
                            binding!!.videoview1.setVideoPath(v)

                            val mediaController = MediaController(requireContext())
                            mediaController.setAnchorView(binding!!.videoview1)

                            binding!!.videoview1.setMediaController(mediaController)
                            binding!!.videoview1.setVideoURI(Uri.parse(v))

                            //for video in loop
                            binding!!.videoview1.setOnPreparedListener { mp ->
                                mp.isLooping = true
                            }
                            binding!!.videoview1.start()

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