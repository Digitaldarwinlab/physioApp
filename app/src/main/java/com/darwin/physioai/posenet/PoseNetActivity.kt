package com.darwin.physioai.posenet


import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
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
import com.darwin.physioai.R
import com.darwin.physioai.databinding.ActivityPosenetBinding
import com.darwin.physioai.posenet.core.CameraXViewModel
import com.darwin.physioai.posenet.core.GraphicOverlay
import com.darwin.physioai.posenet.core.PreferenceUtils
import com.darwin.physioai.posenet.core.VisionImageProcessor
import com.google.mlkit.common.MlKitException
import io.agora.rtc.ss.ScreenSharingClient
import io.agora.rtc.video.VideoEncoderConfiguration
import io.agora.rtm.*


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
    private var mFacing = CameraSelector.LENS_FACING_BACK
    private var cameraSelector: CameraSelector? = null
    private var binding : ActivityPosenetBinding? = null
    private var mRtmClient: RtmClient? = null
    private var mRtmChannel: RtmChannel? = null
    private var appid = "ff503c93e05d40bb964d6677ecb05d50"
    private var token = "006ff503c93e05d40bb964d6677ecb05d50IACfIIZBGj8hj6XcdIJ+BLaMs8/8EBmkveg036Dq9oh5GViNuxMAAAAAEAD1z9KPm0gBYgEAAQCZSAFi"
    private var uid = 1
    private var channel = "test2"
    private var screenshare : ScreenSharingClient? = null


    private lateinit var value : TextView

    object Myvariables {
        var angle : String?= null
        var excercise: String? = null
        var pp_cp_id : String? = null
        var time : String? = null
        var rep : String? = null
        var trigger : String? = "0"
    }


    @RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPosenetBinding.inflate(layoutInflater)

        Log.d(TAG, "onCreate")
        Myvariables.angle = intent.getStringExtra("angle")
        Myvariables.excercise = intent.getStringExtra("exercise")
        Myvariables.pp_cp_id = intent.getStringExtra("pp_cp_id")
        Myvariables.time = intent.getStringExtra("time")
        Myvariables.rep = intent.getStringExtra("rep")


        if (savedInstanceState != null) {
            selectedModel = savedInstanceState.getString(STATE_SELECTED_MODEL, OBJECT_DETECTION)
            mFacing =
                savedInstanceState.getInt(STATE_LENS_FACING, CameraSelector.LENS_FACING_BACK)
        }
        cameraSelector = CameraSelector.Builder().requireLensFacing(mFacing).build()
        setContentView(binding!!.root)


        previewView = findViewById(R.id.preview_view)


        if (previewView == null) {
            Log.d(TAG, "previewView is null")
        }

        graphicOverlay = findViewById(R.id.graphic_overlay)
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null")
        }

        val intent = intent
        val vid = intent.getStringExtra("video")
        val name = intent.getStringExtra("name")
        val rep_all = intent.getStringExtra("reps")
        val set_all = intent.getStringExtra("sets")


        val totalReps = rep_all?.toInt()
        val totalSets = set_all?.toInt()

        val date = intent.getStringExtra("date")
        val dateString = date.toString()
        Log.d("LogTagGetDate", date.toString())

        val sharedPref = getSharedPreferences("login", Context.MODE_PRIVATE)
        val episodeID = sharedPref.getInt("episode_id", 0)
        Log.d("LogTag", episodeID.toString())

        val options: MutableList<String> = ArrayList()
        options.add(POSE_DETECTION)

        val dataAdapter = ArrayAdapter(this, com.darwin.physioai.R.layout.spinner_style, options)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val facingSwitch = findViewById<ToggleButton>(com.darwin.physioai.R.id.facing_switch)
        facingSwitch.setOnCheckedChangeListener(this)
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(CameraXViewModel::class.java)
            .processCameraProvider
            .observe(this) { provider: ProcessCameraProvider? ->
                cameraProvider = provider
                if (allPermissionsGranted()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        bindAllCameraUseCases()
                    }
                }
            }

        if (!allPermissionsGranted()) {
            runtimePermissions
        }

        binding?.apply {
            val videoencode = VideoEncoderConfiguration().apply {
                frameRate = 24
                bitrate = 0
                dimensions = VideoEncoderConfiguration.VideoDimensions(720, 1080)
                orientationMode = VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            }

            startShare.setOnClickListener {
                if (mRtmClient == null){
                    initiateRTM()
                    loginRTM()
                    joinchannel()
                }
                if(screenshare == null){
                    screenshare = ScreenSharingClient()
                    screenshare?.start(
                        this@PoseNetActivity,
                        appid,
                        token,
                        channel,
                        uid,
                        videoencode
                    )
                }else{
                    Toast.makeText(this@PoseNetActivity, "Session Already Running", Toast.LENGTH_SHORT).show()
                }
            }

            stopShare.setOnClickListener {
                mRtmClient?.logout(null);
                mRtmChannel?.leave(null);
                screenshare?.stop(
                    this@PoseNetActivity
                )
                screenshare = null
                mRtmClient = null
                mRtmChannel = null
            }
        }

    }

    private fun initiateRTM(){
        try {
            mRtmClient = RtmClient.createInstance(baseContext,"08bcd6eabeab4d61bc9258c456d0f2d3", object :
                RtmClientListener {
                override fun onConnectionStateChanged(p0: Int, p1: Int) {
                    TODO("Not yet implemented")
                }

                override fun onMessageReceived(p0: RtmMessage?, p1: String?) {
                    Log.d("TestLog", p0?.text.toString())

                    Myvariables.trigger = p0?.text.toString()

                    Log.d("TestLog", p1.toString())
                }

                override fun onImageMessageReceivedFromPeer(
                    p0: RtmImageMessage?,
                    p1: String?
                ) {
                    TODO("Not yet implemented")
                }

                override fun onFileMessageReceivedFromPeer(
                    p0: RtmFileMessage?,
                    p1: String?
                ) {
                    TODO("Not yet implemented")
                }

                override fun onMediaUploadingProgress(
                    p0: RtmMediaOperationProgress?,
                    p1: Long
                ) {
                    TODO("Not yet implemented")
                }

                override fun onMediaDownloadingProgress(
                    p0: RtmMediaOperationProgress?,
                    p1: Long
                ) {
                    TODO("Not yet implemented")
                }

                override fun onTokenExpired() {
                    TODO("Not yet implemented")
                }

                override fun onPeersOnlineStatusChanged(p0: MutableMap<String, Int>?) {
                    TODO("Not yet implemented")
                }

            })
        }catch (e : Exception) {
            throw  RuntimeException("RTM initialization failed!");
        }
    }

    private fun loginRTM(){
        mRtmClient?.login("08bcd6eabeab4d61bc9258c456d0f2d3", uid.toString(), object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {
                Log.d("LogTag", responseInfo.toString())
            }
            override fun onFailure(errorInfo: ErrorInfo) {
                val text: CharSequence =
                    "User: $uid failed to log in to the RTM system!$errorInfo"
                val duration = Toast.LENGTH_SHORT
                Log.d("TestLog", errorInfo.toString())
                runOnUiThread {
                    val toast = Toast.makeText(applicationContext, text, duration)
                    toast.show()
                }
            }
        })
    }

    private fun joinchannel(){
        val mRtmChannelListener: RtmChannelListener = object : RtmChannelListener {
            override fun onMemberCountUpdated(i: Int) {}
            override fun onAttributesUpdated(list: List<RtmChannelAttribute>) {}
            override fun onMessageReceived(message: RtmMessage, fromMember: RtmChannelMember) {
                val text = message.text
                val fromUser = fromMember.userId
                val message_text = "Message received from $fromUser : $text\n"
                Log.d("TestLog", message_text.toString())
            }

            override fun onImageMessageReceived(
                rtmImageMessage: RtmImageMessage,
                rtmChannelMember: RtmChannelMember
            ) {
            }

            override fun onFileMessageReceived(
                rtmFileMessage: RtmFileMessage,
                rtmChannelMember: RtmChannelMember
            ) {
            }

            override fun onMemberJoined(member: RtmChannelMember) {}
            override fun onMemberLeft(member: RtmChannelMember) {}
        }
        try {
            mRtmChannel = mRtmClient!!.createChannel(channel, mRtmChannelListener)
        } catch (e: java.lang.RuntimeException) {
        }
        mRtmChannel!!.join(object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {}
            override fun onFailure(errorInfo: ErrorInfo) {
                val text: CharSequence = "User: $uid failed to join the channel!$errorInfo"
                val duration = Toast.LENGTH_SHORT
                runOnUiThread {
                    val toast = Toast.makeText(applicationContext, text, duration)
                    toast.show()
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putString(STATE_SELECTED_MODEL, selectedModel)
        outState.putInt(STATE_LENS_FACING, mFacing)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Synchronized
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {

        selectedModel = parent?.getItemAtPosition(pos).toString()
        Log.d(TAG, "Selected model: $selectedModel")
        bindAnalysisUseCase()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Do nothing.
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Log.d(TAG, "Set facing")
        if (cameraProvider == null) {
            return
        }
        val newLensFacing = if (mFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }

        val newCameraSelector =
            CameraSelector.Builder().requireLensFacing(newLensFacing).build()
        try {
            if (cameraProvider!!.hasCamera(newCameraSelector)) {
                mFacing = newLensFacing
                cameraSelector = newCameraSelector
                bindAllCameraUseCases()
                return
            }
        } catch (e: CameraInfoUnavailableException) {
            Log.d("LogTag", e.toString())
        }
        Toast.makeText(applicationContext, "This device does not have lens with facing: $newLensFacing", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
        if (mRtmClient !=null){
            mRtmClient?.logout(null);
        }

        if (mRtmChannel !=null){
            mRtmChannel?.leave(null);
        }

        if (screenshare != null){
            screenshare?.stop(
                this@PoseNetActivity
            )
        }

        screenshare = null
        mRtmClient = null
        mRtmChannel = null
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun bindAllCameraUseCases() {
        if (cameraProvider != null) {
            cameraProvider!!.unbindAll()
            bindPreviewUseCase()
            bindAnalysisUseCase()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this, mFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        previewUseCase = builder.build()
        //previewUseCase!!.setSurfaceProvider(previewView!!.createSurfaceProvider())
        cameraProvider!!.bindToLifecycle(/* lifecycleOwner= */this, cameraSelector!!, previewUseCase)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
                    PoseDetectorProcessor(this@PoseNetActivity,this, poseDetectorOptions, shouldShowInFrameLikelihood, visualizeZ, rescaleZ)
                }
                else -> throw IllegalStateException("Invalid model name")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Can not create image processor: $selectedModel", e)
            Toast.makeText(applicationContext, "Can not create image processor: " + e.localizedMessage, Toast.LENGTH_LONG).show()
            return
        }

        val builder = ImageAnalysis.Builder()
        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this, mFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        analysisUseCase = builder.build()
        needUpdateGraphicOverlayImageSourceInfo = true
        analysisUseCase?.setAnalyzer(ContextCompat.getMainExecutor(this)
        ) { imageProxy: ImageProxy ->
            if (needUpdateGraphicOverlayImageSourceInfo) {
                val isImageFlipped = mFacing == CameraSelector.LENS_FACING_FRONT
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                    graphicOverlay!!.setImageSourceInfo(
                        imageProxy.width,
                        imageProxy.height,
                        isImageFlipped
                    )
                } else {
                    graphicOverlay!!.setImageSourceInfo(
                        imageProxy.height,
                        imageProxy.width,
                        isImageFlipped
                    )
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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