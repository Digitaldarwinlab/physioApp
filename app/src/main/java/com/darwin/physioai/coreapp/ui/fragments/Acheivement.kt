package com.darwin.physioai.coreapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.darwin.physioai.R
import com.darwin.physioai.databinding.AcheivementFragmentBinding
import com.example.physioai.data.network.Resource
import com.darwin.physioai.coreapp.utils.CShowProgress
import com.darwin.physioai.coreapp.utils.Constants
import com.darwin.physioai.coreapp.utils.SessionManager
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.eazegraph.lib.models.PieModel
import javax.inject.Inject

@AndroidEntryPoint
class Acheivement : Fragment(R.layout.acheivement_fragment) {
    private lateinit var binding : AcheivementFragmentBinding
    @Inject
    lateinit var sessionManager : SessionManager
    @Inject
    lateinit var progress : CShowProgress



    private val viewModel : AcheivementViewModel by viewModels<AcheivementViewModel>()
    private var userid : String? = null
    private var parseInt : Int? = null
    private var complete : Double? = null
    private var pending : Double? = null

    private var backPressedOnce = false

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
        binding = AcheivementFragmentBinding.bind(view)
        sessionManager = SessionManager(requireContext())
        userid = sessionManager.getStringData(Constants.USER_ID).toString()

        setData(userid!!)

    }

    private fun setData(userid: String) {
        parseInt = userid.toInt()
        Log.d("LogVisitUID", parseInt.toString())
        val jsonobj = JsonObject()
        jsonobj.addProperty("id", parseInt)
        viewModel.apply {
            getData(jsonobj)
            AchievementRes.observe(viewLifecycleOwner){
                when (it) {
                    is Resource.Success -> {
                        progress.hideProgress()
                        try {
                            complete = it.value.score
                            pending = 100F - complete!!
                            binding.piechart.apply {
                                addPieSlice(PieModel("Pending", pending!!.toFloat(), Color.parseColor("#FE6DA8")))
                                addPieSlice(PieModel("Completed", complete!!.toFloat(), Color.parseColor("#56B7F1")))
                                startAnimation()
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

}