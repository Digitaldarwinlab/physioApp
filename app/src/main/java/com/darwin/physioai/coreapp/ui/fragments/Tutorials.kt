package com.darwin.physioai.coreapp.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.darwin.physioai.R
import com.darwin.physioai.databinding.TutorialsFragmentBinding
import com.darwin.physioai.coreapp.data.Adapter.TutorialsAdapter
import com.darwin.physioai.coreapp.data.models.DataX
import com.example.physioai.data.network.Resource
import com.darwin.physioai.coreapp.utils.CShowProgress
import com.darwin.physioai.coreapp.utils.Constants
import com.darwin.physioai.coreapp.utils.SessionManager
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class Tutorials : Fragment(R.layout.tutorials_fragment) {

    private lateinit var binding: TutorialsFragmentBinding
    private val viewModel : ScheduleViewModel by viewModels<ScheduleViewModel>()
    @Inject
    lateinit var sessionManager : SessionManager
    @Inject
    lateinit var progress : CShowProgress
    private var episodeID : String? = null
    private var parseInt : Int? = null
    private var tutorialAdapter: TutorialsAdapter? = null
    private lateinit var list : ArrayList<DataX>
    private lateinit var items : ArrayList<DataX>

    val sdf = SimpleDateFormat("yyyy-MM-dd")
    private var currentDate: String= sdf.format(Date())

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
        binding = TutorialsFragmentBinding.bind(view)
        episodeID = sessionManager.getStringData(Constants.EPISODE_ID).toString()
        parseInt = episodeID?.toInt()
        list = ArrayList<DataX>()
        items = ArrayList<DataX>()

        showExercises(parseInt!!, currentDate)
    }

    private fun showExercises(parseInt: Int, currentDate: String) {
        Log.d("LogTutorialsDate", currentDate)
        val jsonobj = JsonObject()
        jsonobj.addProperty("id", parseInt)
        jsonobj.addProperty("date", currentDate)
        viewModel.apply {
            getScheduleRes(jsonobj)
            ScheduleRes.observe(viewLifecycleOwner){
                when (it) {
                    is Resource.Success -> {
                        progress.hideProgress()
                        try {
                            list.clear()
                            list.addAll(it.value.time_slot_mobile[0].data)
                            for (i in list.indices){
                                items.add(it.value.time_slot_mobile[0].data[i])
                            }
                            setupTutorialRecycler(items)
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

    @SuppressLint("NotifyDataSetChanged")
    private fun setupTutorialRecycler(items: ArrayList<DataX>) {
        binding.apply {
            tutorialAdapter = TutorialsAdapter(requireContext(), items)
            showExercises.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            showExercises.adapter = tutorialAdapter
            tutorialAdapter!!.notifyDataSetChanged()
        }
    }
}