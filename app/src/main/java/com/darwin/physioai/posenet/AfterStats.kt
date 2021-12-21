package com.darwin.physioai.posenet

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.darwin.physioai.R
import com.darwin.physioai.coreapp.utils.CShowProgress
import com.darwin.physioai.coreapp.utils.Constants
import com.darwin.physioai.coreapp.utils.SessionManager
import com.darwin.physioai.databinding.ActivityAfterStatsBinding
import com.example.physioai.data.network.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.WithFragmentBindings
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AfterStats : AppCompatActivity() {

    private val viewModel : StatsViewModel by viewModels()
    @Inject
    lateinit var sessionManager : SessionManager
    @Inject
    lateinit var progress : CShowProgress
    private var binding: ActivityAfterStatsBinding? = null

    private var excercise : String? = null
    private var pp_cp_id : String? = null
    private var time : String? = null
    private var min : String? = null
    private var min1 : String? = null
    private var min2 : String? = null
    private var min3 : String? = null
    private var min4 : String? = null
    private var min5 : String? = null
    private var min6 : String? = null
    private var min7 : String? = null
    private var min8 : String? = null
    private var min9 : String? = null
    private var min10 : String? = null
    private var min11 : String? = null
    private var min12 : String? = null
    private var min13 : String? = null

    // max
    private var max : String? = null
    private var max1 : String? = null
    private var max2 : String? = null
    private var max3 : String? = null
    private var max4 : String? = null
    private var max5 : String? = null
    private var max6 : String? = null
    private var max7 : String? = null
    private var max8 : String? = null
    private var max9 : String? = null
    private var max10 : String? = null
    private var max11 : String? = null
    private var max12 : String? = null
    private var max13 : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAfterStatsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        excercise = intent.getStringExtra("exercise")
        pp_cp_id = intent.getStringExtra("pp_cp_id")
        time = intent.getStringExtra("time")

        getAllMinMAx()
        Apicall()
    }

    private fun Apicall() {
        val jsonobj = JsonObject()
        val parseInt = pp_cp_id?.toInt()
        if(!(pp_cp_id.isNullOrEmpty() || excercise.isNullOrEmpty() || time.isNullOrEmpty())) {
            jsonobj.addProperty("id", parseInt)
            jsonobj.addProperty("exercise", excercise)
            jsonobj.addProperty("time", time)
            jsonobj.addProperty("Left Shoulder(ver) max", max2?.toInt())
            jsonobj.addProperty("Left Shoulder(ver) min", min2?.toInt())
            jsonobj.addProperty("Right Shoulder(ver) max", max3?.toInt())
            jsonobj.addProperty("Right Shoulder(ver) min", min3?.toInt())
            jsonobj.addProperty("Left Elbow max", max6?.toInt())
            jsonobj.addProperty("Left Elbow min", min6?.toInt())
            jsonobj.addProperty("Right Elbow max", max5?.toInt())
            jsonobj.addProperty("Right Elbow min", min5?.toInt())
            jsonobj.addProperty("Right Hip max", max1?.toInt())
            jsonobj.addProperty("Right Hip min", min1?.toInt())
            jsonobj.addProperty("Left Hip max", max?.toInt())
            jsonobj.addProperty("Left Hip min", min?.toInt())
            jsonobj.addProperty("leftKnee max", max4?.toInt())
            jsonobj.addProperty("leftKnee min", min4?.toInt())
            jsonobj.addProperty("RightKnee max", max5?.toInt())
            jsonobj.addProperty("RightKnee min", min5?.toInt())
            jsonobj.addProperty("Neck Left max", max11?.toInt())
            jsonobj.addProperty("Neck Left min", min11?.toInt())
            jsonobj.addProperty("Neck Right max", max12?.toInt())
            jsonobj.addProperty("Neck Right min", min12?.toInt())

            Log.d("LogPayer", jsonobj.toString())

            viewModel.apply {
                getstatsres(jsonobj)
                stats.observe(this@AfterStats, androidx.lifecycle.Observer {
                    when (it) {
                        is Resource.Success -> {
                            progress.hideProgress()
                            try {
                                binding?.statstext?.setText("Congratulations! Exercise Completed!")
                                Toast.makeText(this@AfterStats, it.value.message.toString(), Toast.LENGTH_SHORT).show()
                            } catch (e: NullPointerException) {
                                Toast.makeText(
                                    this@AfterStats,
                                    "oops..! Something went wrong.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is Resource.Failure -> {
                            progress.hideProgress()
                            Toast.makeText(this@AfterStats, "Failed.", Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Loading -> {
                            if (progress.mDialog?.isShowing == true) {
                                progress.hideProgress()
                            } else {
                                progress.showProgress(this@AfterStats)
                            }
                        }
                    }
                })
            }
        }
    }

    fun getAllMinMAx(){
        min = intent.getStringExtra("min")
        max = intent.getStringExtra("max")
        Log.d("LogTagStats0","$min - $max" )

        min1 = intent.getStringExtra("min1")
        max1 = intent.getStringExtra("max1")
        Log.d("LogTagStats1","$min1 - $max1" )

        min2 = intent.getStringExtra("min2")
        max2 = intent.getStringExtra("max2")
        Log.d("LogTagStats2","$min2 - $max2" )

        min3 = intent.getStringExtra("min3")
        max3 = intent.getStringExtra("max3")
        Log.d("LogTagStats3","$min3 - $max3" )

        min4 = intent.getStringExtra("min4")
        max4 = intent.getStringExtra("max4")
        Log.d("LogTagStats4","$min4 - $max4" )

        min5 = intent.getStringExtra("min5")
        max5 = intent.getStringExtra("max5")
        Log.d("LogTagStats5","$min5 - $max5" )

        min6 = intent.getStringExtra("min6")
        max6 = intent.getStringExtra("max6")
        Log.d("LogTagStats6","$min6 - $max6" )

        min7 = intent.getStringExtra("min7")
        max7 = intent.getStringExtra("max7")
        Log.d("LogTagStats7","$min7 - $max7" )

        min8 = intent.getStringExtra("min8")
        max8 = intent.getStringExtra("max8")
        Log.d("LogTagStats8","$min8 - $max8" )

        min9 = intent.getStringExtra("min9")
        max9 = intent.getStringExtra("max9")
        Log.d("LogTagStats9","$min9 - $max9" )

        min10 = intent.getStringExtra("min10")
        max10 = intent.getStringExtra("max10")
        Log.d("LogTagStats10","$min10 - $max10" )

        min11 = intent.getStringExtra("min11")
        max11 = intent.getStringExtra("max11")
        Log.d("LogTagStats11","$min11 - $max11" )

        min12 = intent.getStringExtra("min12")
        max12 = intent.getStringExtra("max12")
        Log.d("LogTagStats11","$min11 - $max11" )

        min13 = intent.getStringExtra("min13")
        max13 = intent.getStringExtra("max13")
        Log.d("LogTagStats11","$min11 - $max11" )
    }
}