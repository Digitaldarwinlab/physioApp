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
import com.darwin.physioai.databinding.ScheduleFragmentBinding
import com.darwin.physioai.coreapp.data.Adapter.VisitAdapter
import com.example.physioai.data.models.*
import com.example.physioai.data.network.Resource
import com.darwin.physioai.coreapp.utils.CShowProgress
import com.darwin.physioai.coreapp.utils.Constants
import com.darwin.physioai.coreapp.utils.SessionManager
import com.darwin.physioai.coreapp.data.Adapter.ExcerciseDetailsAdapter
import com.darwin.physioai.coreapp.data.Adapter.TimeSlotAdapter
import com.darwin.physioai.coreapp.data.models.Data
import com.google.gson.JsonObject
import com.vivekkaushik.datepicker.DatePickerTimeline
import com.vivekkaushik.datepicker.OnDateSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class Schedule : Fragment(R.layout.schedule_fragment), TimeSlotAdapter.OnItemClickInterface {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var progress: CShowProgress
    private lateinit var binding: ScheduleFragmentBinding
    private var backPressedOnce = false
    private val viewModel: ScheduleViewModel by viewModels<ScheduleViewModel>()
    private var userid: String? = null
    private var episodeid: String? = null
    private var pat_name: String? = null
    private lateinit var list: ArrayList<TimeSlotMobileX>
    private lateinit var timelist: ArrayList<TimeSlotMobileX>
    private lateinit var listpres: ArrayList<MedicationDetail>
    private lateinit var exerciselist: ArrayList<DataXX>
    private lateinit var visitList: ArrayList<Data>
    private lateinit var visitItems: ArrayList<Data>

    private var timeSlotAdapter: TimeSlotAdapter? = null
    private var exercisedetailsAdapter: ExcerciseDetailsAdapter? = null
    private var visitAdapter: VisitAdapter? = null
    private var parseInt: Int? = null
    private var parseIntEID: Int? = null
    private var flag = 0

    val sdf = SimpleDateFormat("yyyy-MM-dd")
    private var currentDate: String = sdf.format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Toast.makeText(requireContext(), "Press BACK again to exit", Toast.LENGTH_SHORT)
                        .show()

                    if (backPressedOnce) {
                        requireActivity().finish()
                        return

                    }

                    backPressedOnce = true
                    lifecycleScope.launch {
                        delay(2000)
                        backPressedOnce = false
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ScheduleFragmentBinding.bind(view)
        userid = sessionManager.getStringData(Constants.USER_ID).toString()
        episodeid = sessionManager.getStringData(Constants.EPISODE_ID).toString()

        if (episodeid!!.isNotEmpty()) {
            parseIntEID = episodeid!!.toInt()
            Log.d("LogId", parseIntEID.toString())

            exerciselist = ArrayList<DataXX>()
            list = ArrayList<TimeSlotMobileX>()
            timelist = ArrayList<TimeSlotMobileX>()
            listpres = ArrayList<MedicationDetail>()

            visitList = ArrayList<Data>()
            visitItems = ArrayList<Data>()
            showPrescription(parseIntEID!!)
            setupDatePickr()
        } else {

        }

    }

    private fun showVisits(userid: String, strdate: String) {
        parseInt = userid.toInt()
        Log.d("LogVisitUID", parseInt.toString())
        Log.d("LogVisitDate", strdate)
        val jsonobj = JsonObject()
        jsonobj.addProperty("id", parseInt)
        jsonobj.addProperty("date", strdate)
        viewModel.apply {
            getVisit(jsonobj)
            VisitRes.observe(viewLifecycleOwner) {
                when (it) {
                    is Resource.Success -> {
                        progress.hideProgress()
                        if (!it.value.error) {
                        try {
                            visitItems.clear()
                            visitList.clear()
                            visitList.addAll(it.value.data)
                            for (i in visitList.indices) {
                                visitItems.add(it.value.data[i])
                            }
                            setupVisitRecycler(visitItems)
                            binding.visitStatus.text = ""
                        } catch (e: NullPointerException) {
                            Toast.makeText(
                                requireActivity(),
                                "oops..! Something went wrong.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        } else if (it.value.error) {
                            if (flag == 0) {
                                flag++
                                Toast.makeText(
                                    requireContext(),
                                    it.value.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.visitStatus.text = "No Visits Today!"
                            }
                            visitItems.clear()
                            setupVisitRecycler(visitItems)
                        }
                    }
                    is Resource.Failure -> {
                        progress.hideProgress()
                        Toast.makeText(requireContext(), "Failed.", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {
                        if (progress.mDialog?.isShowing == true) {
                            progress.hideProgress()
                        } else {
                            progress.showProgress(requireContext())
                        }
                    }
                }
            }
        }
    }

    private fun showPrescription(parseIntEID: Int) {
        Log.d("LogVisitEID", parseIntEID.toString())
        val jsonobj = JsonObject()
        jsonobj.addProperty("id", parseIntEID)
        viewModel.apply {
            getPres(jsonobj)
            PresRes.observe(viewLifecycleOwner) {
                when (it) {
                    is Resource.Success -> {
                        progress.hideProgress()
                        try {
                            if (it.value.isNotEmpty()) {
                                listpres.addAll(it.value[0].medication_detail)
                                Log.d("LogPres", listpres.toString())
                                val n = listpres.size
                                binding.presDetails.text = listpres[n-1].medicine_name
                                binding.otherDetailsValue.text = listpres[n-1].instruction
                            } else {
                                binding.presDetails.text = "No Prescription!"
                                binding.otherDetailsValue.text = "No Prescription!"
                            }
                        } catch (e: NullPointerException) {
                            Toast.makeText(
                                requireActivity(),
                                "oops..! Something went wrong.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is Resource.Failure -> {
                        progress.hideProgress()
                        Toast.makeText(requireContext(), "Failed.", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {
                        if (progress.mDialog?.isShowing == true) {
                            progress.hideProgress()
                        } else {
                            progress.showProgress(requireContext())
                        }
                    }
                }
            }
        }
    }

    private fun setupDatePickr() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val m = month + 1
        val dat = "$year-$m-$day"
        Apicall(parseIntEID!!, dat)
        showVisits(userid!!, dat)

        val datePickerTimeline: DatePickerTimeline = binding.datePickerTimeline
        datePickerTimeline.setInitialDate(year, month, day)
        datePickerTimeline.setActiveDate(c)
//        if (day>2){
//            datePickerTimeline.setInitialDate(year,month,day-2)
//            datePickerTimeline.setActiveDate(c)
//        }else{
//            datePickerTimeline.setInitialDate(year,month-1,day-2)
//            datePickerTimeline.setActiveDate(c)
//        }
        datePickerTimeline.setOnDateSelectedListener(object : OnDateSelectedListener {

            override fun onDateSelected(year: Int, month: Int, day: Int, dayOfWeek: Int) {
                val m = month + 1
                val strdate = "$year-$m-$day"
                flag = 0
                Apicall(parseIntEID!!, strdate)
                showVisits(userid!!, strdate)
            }

            override fun onDisabledDateSelected(
                year: Int,
                month: Int,
                day: Int,
                dayOfWeek: Int,
                isDisabled: Boolean
            ) {
                Log.d("LogDeactiveDate", "$year + $month + $day")
                Toast.makeText(requireContext(), "Already selected", Toast.LENGTH_SHORT).show()
            }
        })
//        val dates = arrayOf(Calendar.getInstance().time)
//        datePickerTimeline.deactivateDates(dates)
    }

    private fun Apicall(parseIntEID: Int, strdate: String) {
        val jsonobj = JsonObject()
        Log.d("LogDate", strdate.toString())
        jsonobj.addProperty("id", parseIntEID)
        jsonobj.addProperty("date", strdate)
        viewModel.apply {
            getScheduleRes(jsonobj)
            ScheduleRes.observe(viewLifecycleOwner) {
                when (it) {
                    is Resource.Success -> {
                        progress.hideProgress()
                        if (!it.value.error) {
                            try {
                                timelist.clear()
                                list.clear()
                                list.addAll(it.value.time_slot_mobile)
                                for (i in list.indices) {
                                    timelist.add(it.value.time_slot_mobile[i])
                                }
                                setupTimeSlotRecycler(timelist)
                            } catch (e: NullPointerException) {
                                Toast.makeText(
                                    requireActivity(),
                                    "oops..! Something went wrong.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else if (it.value.error) {
                            if (flag == 0) {
                                flag++
                                Toast.makeText(
                                    requireContext(),
                                    it.value.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            timelist.clear()
                            exerciselist.clear()
                            setupTimeSlotRecycler(timelist)
                            setupExcerciseRecycler(exerciselist, null)
                        }
                    }
                    is Resource.Failure -> {
                        progress.hideProgress()
                        Toast.makeText(requireContext(), "Failed.", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {
                        if (progress.mDialog?.isShowing == true) {
                            progress.hideProgress()
                        } else {
                            progress.showProgress(requireContext())
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupVisitRecycler(visitItems: ArrayList<Data>) {
        binding.apply {
            visitAdapter = VisitAdapter(requireContext(), visitItems)
            visitRecycler.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            visitRecycler.adapter = visitAdapter
            visitAdapter!!.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupTimeSlotRecycler(timelist: ArrayList<TimeSlotMobileX>) {
        binding.apply {
            timeSlotAdapter = TimeSlotAdapter(timelist, this@Schedule)
            timings.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            timings.adapter = timeSlotAdapter
            timeSlotAdapter!!.notifyDataSetChanged()
        }
    }

    private fun setupExcerciseRecycler(listimedate: List<DataXX>, time: String?) {
        binding.apply {
            exercisedetailsAdapter = ExcerciseDetailsAdapter(requireContext(), listimedate, time)
            recyclerV.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            recyclerV.adapter = exercisedetailsAdapter
        }
    }

    override fun onLeadClicked(timeSlotMobile: ArrayList<DataXX>, position: Int, time: String) {
        setupExcerciseRecycler(timeSlotMobile, time)
    }
}