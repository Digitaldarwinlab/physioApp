package com.darwin.physioai.posenet


import android.R.attr
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.media.projection.MediaProjectionManager
import android.opengl.EGLSurface
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.TextureView
import android.view.View
import android.widget.*
import android.widget.RelativeLayout
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
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.gdp.EglCore
import io.agora.rtc.video.VideoEncoderConfiguration
import io.agora.rtc.video.VideoEncoderConfiguration.ORIENTATION_MODE
import io.agora.rtc.video.VideoEncoderConfiguration.VideoDimensions
import java.util.*
import javax.script.ScriptEngine.ENGINE


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

    private lateinit var rep_total: TextView
    private lateinit var value_total: TextView
    private lateinit var exName : TextView
    private lateinit var rep : TextView
    private lateinit var value : TextView
    private lateinit var fl_local : FrameLayout
    private lateinit var fl_remote : FrameLayout
    private var engine: RtcEngine? = null

    private val DEFAULT_CAPTURE_WIDTH = 640
    private val DEFAULT_CAPTURE_HEIGHT = 480
    private val join: Button? = null
    private val et_channel: EditText? = null
    private var myUid = 0
    private var joined = false
    private var mPreviewTexture = 0
    private var mPreviewSurfaceTexture: SurfaceTexture? = null
    private var mEglCore: EglCore? = null
    private var mDummySurface: EGLSurface? = null
    private var mDrawSurface: EGLSurface? = null
    private val mTransform = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)
    private var mMVPMatrixInit = false
    private lateinit var mCamera: android.hardware.Camera
    private var mPreviewing = false
    private var mSurfaceWidth = 0
    private var mSurfaceHeight = 0
    private var mTextureDestroyed = false
//    private var mProgram: ProgramTextureOES? = null


    private val interval: Long = 0
    private val base: Long = 0
    var set_count : Int? = 0
    // private var varibalesPose = PoseGraphic.PoseVariables

    object Myvariables{
        var angle : String?= null
        var excercise: String? = null
        var pp_cp_id : String? = null
        var time : String? = null
        var rep : String? = null
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        setContentView(com.darwin.physioai.R.layout.activity_posenet)

        previewView = findViewById(com.darwin.physioai.R.id.preview_view)
        fl_local = findViewById<FrameLayout>(com.darwin.physioai.R.id.frame)
        fl_remote = findViewById<FrameLayout>(com.darwin.physioai.R.id.remote_video_view_container2)

//        if (checkSelfPermission(
//                Manifest.permission.RECORD_AUDIO,
//                PERMISSION_REQ_ID_RECORD_AUDIO
//            ) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)
//        ) {
//            initializeAndJoinChannel()
//        }

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

//        try {
//            engine = RtcEngine.create(
//                this.applicationContext,
//                getString(R.string.agora_app_id),
//                iRtcEngineEventHandler
//            )
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//                this.onBackPressed()
//        }

//        varibalesPose.repcountfi.observe(this, {
//            Log.d("LogTagCount", it.toString())
//            it.toString();
//        })

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
            .observe(this, { provider: ProcessCameraProvider? ->
                cameraProvider = provider
                if (allPermissionsGranted()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        bindAllCameraUseCases()
                    }
                }
            })


        if (!allPermissionsGranted()) {
            runtimePermissions
        }
    }
    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putString(STATE_SELECTED_MODEL, selectedModel)
        bundle.putInt(STATE_LENS_FACING, mFacing)
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
        unbindVideoService();
        TEXTUREVIEW = null;
        /**leaveChannel and Destroy the RtcEngine instance*/
        if (ENGINE != null) {
            ENGINE.leaveChannel();
        }
        handler.post(RtcEngine::destroy);
        ENGINE = null;
        super.onDestroy();
//        mRtcEngine?.leaveChannel()
//        RtcEngine.destroy()
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
        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this, mFacing)
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
                    val isImageFlipped = mFacing == CameraSelector.LENS_FACING_FRONT
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === PROJECTION_REQ_CODE && resultCode === RESULT_OK) {
            try {
                val metrics = DisplayMetrics()
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics)
                attr.data.putExtra(ExternalVideoInputManager.FLAG_SCREEN_WIDTH, metrics.widthPixels)
                attr.data.putExtra(
                    ExternalVideoInputManager.FLAG_SCREEN_HEIGHT,
                    metrics.heightPixels
                )
                attr.data.putExtra(
                    ExternalVideoInputManager.FLAG_SCREEN_DPI,
                    metrics.density.toInt()
                )
                attr.data.putExtra(
                    ExternalVideoInputManager.FLAG_FRAME_RATE,
                    DEFAULT_SHARE_FRAME_RATE
                )
                setVideoConfig(
                    ExternalVideoInputManager.TYPE_SCREEN_SHARE,
                    metrics.widthPixels,
                    metrics.heightPixels
                )
                mService.setExternalVideoInput(
                    ExternalVideoInputManager.TYPE_SCREEN_SHARE,
                    attr.data
                )
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    fun onClick(v: View) {
        if (v.id == R.id.btn_join) {
            if (!joined) {
                CommonUtil.hideInputBoard(getActivity(), et_channel)

                TEXTUREVIEW = TextureView(getContext())
                // call when join button hit
                val channelId = et_channel!!.text.toString()
                // Check permission
                if (AndPermission.hasPermissions(
                        this,
                        Permission.Group.STORAGE,
                        Permission.Group.MICROPHONE,
                        Permission.Group.CAMERA
                    )
                ) {
                    joinChannel(channelId)
                    return
                }
                // Request permission
                AndPermission.with(this).runtime().permission(
                    Permission.Group.STORAGE,
                    Permission.Group.MICROPHONE,
                    Permission.Group.CAMERA
                ).onGranted { permissions ->
                    // Permissions Granted
                    joinChannel(channelId)
                }.start()
            } else {
                joined = false
                join!!.text = getString(R.string.join)
                localVideo.setEnabled(false)
                fl_local.removeAllViews()
                javax.script.ScriptEngine.ENGINE.leaveChannel()
                TEXTUREVIEW = null
                unbindVideoService()
            }
        } else if (v.id == R.id.localVideo) {
            try {
                val intent = Intent()
                setVideoConfig(
                    ExternalVideoInputManager.TYPE_LOCAL_VIDEO,
                    LOCAL_VIDEO_WIDTH,
                    LOCAL_VIDEO_HEIGHT
                )
                intent.putExtra(ExternalVideoInputManager.FLAG_VIDEO_PATH, mLocalVideoPath)
                if (mService.setExternalVideoInput(
                        ExternalVideoInputManager.TYPE_LOCAL_VIDEO,
                        intent
                    )
                ) {
                    fl_local.removeAllViews()
                    fl_local.addView(
                        TEXTUREVIEW,
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        } else if (v.id == R.id.screenShare) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                /**remove local preview */
                fl_local.removeAllViews()
                /** */
                val mpm =
                    getContext().getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                val intent = mpm.createScreenCaptureIntent()
                startActivityForResult(intent, PROJECTION_REQ_CODE)
            } else {
                showAlert(getString(R.string.lowversiontip))
            }
        }
    }

    private fun checkLocalVideo(): Boolean {
        val dir: File = getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        val videoFile = File(dir, VIDEO_NAME)
        mLocalVideoPath = videoFile.getAbsolutePath()
        mLocalVideoExists = videoFile.exists()
        if (!mLocalVideoExists) {
            showAlert(
                java.lang.String.format(
                    getString(R.string.alert_no_local_video_message),
                    mLocalVideoPath
                )
            )
        }
        return mLocalVideoExists
    }

    private fun setVideoConfig(sourceType: Int, width: Int, height: Int) {
        val mode: ORIENTATION_MODE
        mode =
            when (sourceType) {
                ExternalVideoInputManager.TYPE_LOCAL_VIDEO, ExternalVideoInputManager.TYPE_SCREEN_SHARE -> ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
                else -> ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
            }
        /**Setup video stream encoding configs */
        javax.script.ScriptEngine.ENGINE.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoDimensions(width, height),
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE, mode
            )
        )
    }

    private fun joinChannel(channelId: String) {
        // Check if the context is valid
        val context: Context = getContext() ?: return

        javax.script.ScriptEngine.ENGINE.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        /**Sets the role of a user (Live Broadcast only). */
        javax.script.ScriptEngine.ENGINE.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

        javax.script.ScriptEngine.ENGINE.enableVideo()

        javax.script.ScriptEngine.ENGINE.setDefaultAudioRoutetoSpeakerphone(true)
        javax.script.ScriptEngine.ENGINE.setEnableSpeakerphone(false)

        var accessToken: String? = getString(R.string.agora_access_token)
        if (TextUtils.equals(accessToken, "") || TextUtils.equals(
                accessToken,
                "<#YOUR ACCESS TOKEN#>"
            )
        ) {
            accessToken = null
        }

        val res: Int = javax.script.ScriptEngine.ENGINE.joinChannel(
            accessToken,
            channelId,
            "Extra Optional Data",
            0
        )
        if (res != 0) {
            // Usually happens with invalid parameters
            // Error code description can be found at:
            // en: https://docs.agora.io/en/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html
            // cn: https://docs.agora.io/cn/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html
            showAlert(RtcEngine.getErrorDescription(Math.abs(res)))
            return
        }
        // Prevent repeated entry
        join!!.isEnabled = false
    }

    private fun bindVideoService() {
        val intent = Intent()
        intent.setClass(getContext(), ExternalVideoInputService::class.java)
        mServiceConnection = VideoInputServiceConnection()
        getContext().bindService(intent, mServiceConnection, BIND_AUTO_CREATE)
    }

    private fun unbindVideoService() {
        if (mServiceConnection != null) {
            getContext().unbindService(mServiceConnection)
            mServiceConnection = null
        }
    }

    private class VideoInputServiceConnection : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            mService = iBinder as IExternalVideoInputService
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mService = null
        }
    }

    private val iRtcEngineEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {

        override fun onWarning(warn: Int) {
            Log.w(
                TAG,
                String.format(
                    "onWarning code %d message %s",
                    warn,
                    RtcEngine.getErrorDescription(warn)
                )
            )
        }


        override fun onError(err: Int) {
            Log.e(
                TAG,
                String.format("onError code %d message %s", err, RtcEngine.getErrorDescription(err))
            )
//            showAlert(
//                String.format(
//                    "onError code %d message %s",
//                    err,
//                    RtcEngine.getErrorDescription(err)
//                )
//            )
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.i(TAG, String.format("onJoinChannelSuccess channel %s uid %d", channel, uid))
//            showLongToast(String.format("onJoinChannelSuccess channel %s uid %d", channel, uid))
            myUid = uid
            joined = true
            handler.post {
                join!!.isEnabled = true
                join.text = getString(R.string.leave)
                localVideo.setEnabled(mLocalVideoExists)
                bindVideoService()
            }
        }
        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
            Log.i(TAG, "onRemoteVideoStateChanged:uid->$uid, state->$state")
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            Log.i(TAG, "onUserJoined->$uid")
//            showLongToast(String.format("user %d joined!", uid))
        }
        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i(TAG, String.format("user %d offline! reason:%d", uid, reason))

        }
    }
}