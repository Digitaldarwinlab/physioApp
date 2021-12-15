package com.darwin.physioai.posenet


import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.darwin.physioai.posenet.core.CameraXViewModel
import com.darwin.physioai.posenet.core.GraphicOverlay
import com.darwin.physioai.posenet.core.PreferenceUtils
import com.darwin.physioai.posenet.core.VisionImageProcessor
import com.google.mlkit.common.MlKitException
import java.util.*


class PoseNetActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback,
    AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    private var previewView: PreviewView? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var imageProcessor: VisionImageProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var selectedModel = POSE_DETECTION
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var cameraSelector: CameraSelector? = null
    private lateinit var exName : TextView
    private lateinit var rep : TextView
    private lateinit var value : TextView
    private lateinit var rep_total: TextView
    private lateinit var value_total: TextView
//    private var varibalesPose = PoseGraphic.PoseVariables

    private val interval: Long = 0
    private val base: Long = 0
    var set_count : Int? = 0

    object Myvariables{
        var angle : String?= null
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        Myvariables.angle = intent.getStringExtra("angle")

        if (savedInstanceState != null) {
            selectedModel = savedInstanceState.getString(STATE_SELECTED_MODEL, OBJECT_DETECTION)
            lensFacing =
                savedInstanceState.getInt(STATE_LENS_FACING, CameraSelector.LENS_FACING_BACK)
        }
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        setContentView(com.darwin.physioai.R.layout.activity_posenet)

        previewView = findViewById(com.darwin.physioai.R.id.preview_view)
        if (previewView == null) {
            Log.d(TAG, "previewView is null")
        }
        graphicOverlay = findViewById(com.darwin.physioai.R.id.graphic_overlay)
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null")
        }
        val intent = intent
        val vid = intent.getStringExtra("video")
        val name = intent.getStringExtra("name")
        val rep_all = intent.getStringExtra("reps")
        val set_all = intent.getStringExtra("sets")

//        varibalesPose.repcountfi.observe(this, {
//            Log.d("LogTagCount", it.toString())
//            it.toString();
//        })

        val totalReps = rep_all?.toInt()
        val totalSets = set_all?.toInt()

//        exName = findViewById<TextView>(R.id.namev)
//        exName.text = name

//        rep = findViewById(R.id.repititionv)
//        value = findViewById(R.id.valuev)
//
//        rep.text = PoseGraphic.count.toString()
//
//        value.text = set_count.toString()
//
//        rep_total = findViewById(R.id.repitition_total)
//        rep_total.text = "of ${totalReps}"

//        value_total = findViewById(R.id.value_total)
//        value_total.text = "of ${totalSets}"
//
//        viewModel = ViewModelProvider(this)[ScheduleViewModel::class.java]

        val date = intent.getStringExtra("date")
        val dateString = date.toString()
        Log.d("LogTagGetDate", date.toString())

        val sharedPref = getSharedPreferences("login", Context.MODE_PRIVATE)
        val episodeID = sharedPref.getInt("episode_id", 0)
        Log.d("LogTag", episodeID.toString())

//        var request = GetCarePlanRequest(episodeID, dateString)
//        Log.d("requestToString", request.toString())
//
//        val simpleChronometer =
//            findViewById<View>(R.id.simpleChronometer) as Chronometer // initiate a chronometer
//        simpleChronometer.start() // start a chronometer


//        lifecycleScope.launch {
//            viewModel.getScheduleFlow(request).collectLatest {
//                Log.d("LogTagPoseReq", it.toString())
//                for(i in it) {
//                    val exerciseList = ArrayList<ExerciseDetailsItem>()
//                    for(j in exerciseList) {
//                        if(j.name == name) {
////                            Log.d("LogTagJName", j.name.toString())
////                            rep_total.text = j.rep?.repCount.toString()
////                            Log.d("LogTagRep", j.rep?.repCount.toString())
////                            value_total.text = j.rep?.set.toString()
//                        }
//                    }
//                }
//            }
//        }



//        video = findViewById(R.id.videoview1)
//        video.setVideoPath(vid)
//        val mediaController = MediaController(this)
//        mediaController.setAnchorView(video)
//        video.setMediaController(mediaController)
//        video.setVideoURI(Uri.parse(vid))
//        //for video in loop
//        video.setOnPreparedListener { mp ->
//            mp.isLooping = true
//        }
//        video.start()

        //textView.text = PoseGraphic.angle25public.toString();
        val options: MutableList<String> = ArrayList()
        options.add(POSE_DETECTION)

        val dataAdapter = ArrayAdapter(this, com.darwin.physioai.R.layout.spinner_style, options)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val facingSwitch = findViewById<ToggleButton>(com.darwin.physioai.R.id.facing_switch)
        facingSwitch.setOnCheckedChangeListener(this)
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(CameraXViewModel::class.java)
            .processCameraProvider
            .observe(this, { provider: ProcessCameraProvider? ->
                cameraProvider = provider
                if (allPermissionsGranted()) {
                    bindAllCameraUseCases()
                }
            })


        if (!allPermissionsGranted()) {
            runtimePermissions
        }


//        when(count == totalReps) {
//            //TODO Set count increase by 1
//            //TODO rep count reset to 0
//            when(set_count == totalSets) {
//                    val builder = AlertDialog.Builder(this)
//                builder.setMessage("fjorn")
//
//            }
//        }


    }
    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putString(STATE_SELECTED_MODEL, selectedModel)
        bundle.putInt(STATE_LENS_FACING, lensFacing)
    }
    @Synchronized
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {

        selectedModel = parent?.getItemAtPosition(pos).toString()
        Log.d(TAG, "Selected model: $selectedModel")
        bindAnalysisUseCase()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Do nothing.
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Log.d(TAG, "Set facing")
        if (cameraProvider == null) {
            return
        }
        val newLensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        val newCameraSelector =
            CameraSelector.Builder().requireLensFacing(newLensFacing).build()
        try {
            if (cameraProvider!!.hasCamera(newCameraSelector)) {
                lensFacing = newLensFacing
                cameraSelector = newCameraSelector
                bindAllCameraUseCases()
                return
            }
        } catch (e: CameraInfoUnavailableException) {
        }
        Toast.makeText(applicationContext, "This device does not have lens with facing: $newLensFacing", Toast.LENGTH_SHORT).show()
    }

    public override fun onResume() {
        super.onResume()
        bindAllCameraUseCases()
    }

    override fun onPause() {
        super.onPause()

        imageProcessor?.run {
            this.stop()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        imageProcessor?.run {
            this.stop()
        }
    }

    private fun bindAllCameraUseCases() {
        if (cameraProvider != null) {
            cameraProvider!!.unbindAll()
            bindPreviewUseCase()
            bindAnalysisUseCase()
        }
    }

    private fun bindPreviewUseCase() {
        if (!PreferenceUtils.isCameraLiveViewportEnabled(this)) {
            return
        }
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        val builder = Preview.Builder()
        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        previewUseCase = builder.build()
        //previewUseCase!!.setSurfaceProvider(previewView!!.createSurfaceProvider())
        cameraProvider!!.bindToLifecycle(/* lifecycleOwner= */this, cameraSelector!!, previewUseCase)
    }

    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }
        if (imageProcessor != null) {
            imageProcessor!!.stop()
        }
        imageProcessor = try {
            when (selectedModel) {
                POSE_DETECTION -> {
                    val poseDetectorOptions = PreferenceUtils.getPoseDetectorOptionsForLivePreview(this)
                    val shouldShowInFrameLikelihood = PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this)
                    val visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this)
                    val rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this)
                    PoseDetectorProcessor(this, poseDetectorOptions, shouldShowInFrameLikelihood, visualizeZ, rescaleZ)
                }
                else -> throw IllegalStateException("Invalid model name")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Can not create image processor: $selectedModel", e)
            Toast.makeText(applicationContext, "Can not create image processor: " + e.localizedMessage, Toast.LENGTH_LONG).show()
            return
        }

        val builder = ImageAnalysis.Builder()
        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        analysisUseCase = builder.build()
        needUpdateGraphicOverlayImageSourceInfo = true
        analysisUseCase?.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(this),
            { imageProxy: ImageProxy ->
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180)
                    {
                        graphicOverlay!!.setImageSourceInfo(imageProxy.width, imageProxy.height, isImageFlipped)
                    }
                    else {
                        graphicOverlay!!.setImageSourceInfo(imageProxy.height, imageProxy.width, isImageFlipped)
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
                try {
                    imageProcessor!!.processImageProxy(imageProxy, graphicOverlay)
                } catch (e: MlKitException) {
                    Log.e(TAG, "Failed to process image. Error: " + e.localizedMessage)
                    Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        )
        cameraProvider!!.bindToLifecycle(this, cameraSelector!!, analysisUseCase)
    }
    private val requiredPermissions: Array<String?>
        get() = try {
            val info = this.packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }


    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission)) {
                return false
            }
        }
        return true
    }
    private val runtimePermissions: Unit get() {
        val allNeededPermissions: MutableList<String?> = ArrayList()
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission)
            }
        }
        if (allNeededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "Permission granted!")
        if (allPermissionsGranted()) {
            bindAllCameraUseCases()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    companion object {
        private const val TAG = "CameraXLivePreview"
        private const val PERMISSION_REQUESTS = 1
        private const val OBJECT_DETECTION = "Object Detection"
        private const val POSE_DETECTION = "Pose Detection"
        private const val STATE_SELECTED_MODEL = "selected_model"
        private const val STATE_LENS_FACING = "lens_facing"

        private fun isPermissionGranted(
            context: Context,
            permission: String?
        ): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission!!) == PackageManager.PERMISSION_GRANTED)
            {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }
}