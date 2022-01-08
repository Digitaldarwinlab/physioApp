package com.darwin.physioai.posenet


import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.opengl.EGLSurface
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
import io.agora.rtc.RtcEngine
import io.agora.rtc.gdp.EglCore
import java.util.*


class PoseNetActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback,
    AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener{

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

//    // Kotlin
//    // Fill the App ID of your project generated on Agora Console.
//    private val APP_ID = "f6181a4c31b14c80a83607d8118c7b9e"
//
//    // Fill the channel name.
//    private val CHANNEL = "PhysioAI"
//
//    // Fill the temp token generated on Agora Console.
//    private val TOKEN =
//        "006f6181a4c31b14c80a83607d8118c7b9eIADznsGReUybJ9q7shawy2MGqorJOp+4Jb86C7p9aRhZ199bxfkAAAAAEABhfJB/sYq7YQEAAQCxirth"
//
//    private var mRtcEngine: RtcEngine? = null
//
//    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
//        // Listen for the remote user joining the channel to get the uid of the user.
//        override fun onUserJoined(uid: Int, elapsed: Int) {
//            runOnUiThread {
//                // Call setupRemoteVideo to set the remote video view after getting uid from the onUserJoined callback.
//                setupRemoteVideo(uid)
//            }
//        }
//    }
//
//    // Kotlin
//    private val PERMISSION_REQ_ID_RECORD_AUDIO = 22
//    private val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1
//
//    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
//        if (ContextCompat.checkSelfPermission(this, permission) !=
//            PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(permission),
//                requestCode
//            )
//            return false
//        }
//        return true
//    }
//
//
//
//    private fun initializeAndJoinChannel() {
//        try {
//            mRtcEngine = RtcEngine.create(baseContext, APP_ID, mRtcEventHandler)
//        } catch (e: Exception) {
//
//        }
//        mRtcEngine!!.enableVideo()
//
//        val localContainer = fl_local
//        val localFrame = RtcEngine.CreateRendererView(baseContext)
//        localContainer.addView(localFrame)
//        mRtcEngine!!.setupLocalVideo(VideoCanvas(localFrame, VideoCanvas.RENDER_MODE_FIT, 0))
//        mRtcEngine!!.joinChannel(TOKEN, CHANNEL, "", 0)
//    }
//
//    // Kotlin
//    private fun setupRemoteVideo(uid: Int) {
//        val remoteContainer = fl_remote
//
//        val remoteFrame = RtcEngine.CreateRendererView(baseContext)
//        remoteFrame.setZOrderMediaOverlay(true)
//        remoteContainer.addView(remoteFrame)
//        mRtcEngine!!.setupRemoteVideo(VideoCanvas(remoteFrame, VideoCanvas.RENDER_MODE_FIT, uid))
//    }
//
//    fun onEncCallClicked(view: android.view.View) {
//        finish()
//    }
//
//    fun onSwitchCameraClicked(view: android.view.View) {
//        mRtcEngine!!.switchCamera()
//    }
//
//    fun onLocalAudioMuteClicked(view: android.view.View) {
//        val iv = view as ImageView
//        if (iv.isSelected) {
//            iv.isSelected = false
//            iv.clearColorFilter()
//        } else {
//            iv.isSelected = true
//            iv.setColorFilter(resources.getColor(com.darwin.physioai.R.color.white), PorterDuff.Mode.MULTIPLY)
//        }
//
//        // Stops/Resumes sending the local audio stream.
//        mRtcEngine!!.muteLocalAudioStream(iv.isSelected)
//    }
//
//    fun onLocalVideoMuteClicked(view: android.view.View) {
//        val iv = view as ImageView
//        if (iv.isSelected) {
//            iv.isSelected = false
//            iv.clearColorFilter()
//        } else {
//            iv.isSelected = true
//            iv.setColorFilter(resources.getColor(com.darwin.physioai.R.color.white), PorterDuff.Mode.MULTIPLY)
//        }
//        // Stops/Resumes sending the local video stream.
//        mRtcEngine!!.muteLocalVideoStream(iv.isSelected)
//
//        val container = fl_local
//        val surfaceView = container.getChildAt(0) as SurfaceView
//        surfaceView.setZOrderMediaOverlay(!iv.isSelected)
//        surfaceView.visibility = if (iv.isSelected) View.GONE else View.VISIBLE
//    }
//
//    private fun initializeAgoraEngine() {
//        try {
//            mRtcEngine =
//                RtcEngine.create(baseContext, getString(com.darwin.physioai.R.string.agora_app_id), mRtcEventHandler)
//        } catch (e: Exception) {
//
//
//            throw RuntimeException(
//                "NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(
//                    e
//                )
//            )
//        }
//    }



//    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
//
//        Log.i(TAG, "onSurfaceTextureAvailable")
//        mTextureDestroyed = false
//        mSurfaceWidth = width
//        mSurfaceHeight = height
//        mEglCore = EglCore()
//        mDummySurface = mEglCore!!.createOffscreenSurface(1, 1)
//        mEglCore!!.makeCurrent(mDummySurface)
//        mPreviewTexture = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
//
//        mPreviewSurfaceTexture = SurfaceTexture(mPreviewTexture)
//        // Creates OnFrameAvailableListener using the Android API setOnFrameAvailableListener, which triggers the onFrameAvailable callback if there are new video frames for SurfaceTexture
//        // Creates OnFrameAvailableListener using the Android API setOnFrameAvailableListener, which triggers the onFrameAvailable callback if there are new video frames for SurfaceTexture
//        mPreviewSurfaceTexture!!.setOnFrameAvailableListener(this)
//        mDrawSurface = mEglCore!!.createWindowSurface(surface)
//        mProgram = ProgramTextureOES()
//        if (mCamera != null || mPreviewing) {
//            Log.e(TAG, "Camera preview has been started")
//            return
//        }
//        try {
//            // Enables the camera (the code sample uses Android's Camera class)
//            mCamera = android.hardware.Camera.open(mFacing)
//            // Sets the most suitable resolution for your app scenario
//            val parameters: Camera.Parameters = mCamera.parameters
//            parameters.setPreviewSize(DEFAULT_CAPTURE_WIDTH, DEFAULT_CAPTURE_HEIGHT)
//            mCamera.parameters = parameters
//            // Sets mPreviewSurfaceTexture as the SurfaceTexture object for camera preview
//            mCamera.setPreviewTexture(mPreviewSurfaceTexture)
//            // Enables the portrait mode for camera preview. To ensure that camera preview stays in the portrait mode, rotate the preview image by 90 degrees clockwise
//            mCamera.setDisplayOrientation(90)
//            // The camera starts capturing video frames and rendering them to the specified SurfaceView
//            mCamera.startPreview()
//            mPreviewing = true
//
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//
////        Log.i(TAG, "onSurfaceTextureAvailable")
////        mTextureDestroyed = false
////        mSurfaceWidth = width
////        mSurfaceHeight = height
////        mEglCore = EglCore()
////        mDummySurface = mEglCore!!.createOffscreenSurface(1, 1)
    ////        mEglCore!!.makeCurrent(mDummySurface)
////
////        mPreviewSurfaceTexture = SurfaceTexture(mPreviewTexture)
////        mPreviewSurfaceTexture!!.setOnFrameAvailableListener(this)
////        mDrawSurface = mEglCore!!.createWindowSurface(surface)
////        mProgram = ProgramTextureOES()
////        if (mCamera != null || mPreviewing) {
////            Log.e(TAG, "Camera preview has been started")
////            return
////        }
////        try {
////            mCamera = Camera.open(mFacing)
////            val parameters: Camera.Parameters = mCamera.getParameters()
////            parameters.setPreviewSize(DEFAULT_CAPTURE_WIDTH, DEFAULT_CAPTURE_HEIGHT)
////            mCamera.setParameters(parameters)
////            mCamera.setPreviewTexture(mPreviewSurfaceTexture)
////            mCamera.setDisplayOrientation(90)
////            mCamera.startPreview()
////            mPreviewing = true
////        } catch (e: IOException) {
////            e.printStackTrace()
////        }
//    }
//
//    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
//
//    }
//
//    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
//        Log.i(TAG, "onSurfaceTextureDestroyed");
//        mTextureDestroyed = true;
//        if (mPreviewing) {
//            mCamera.stopPreview();
//            mPreviewing = false;
//            mCamera.release();
//            mCamera = null;
//        }
//        mProgram.release();
//        mEglCore?.releaseSurface(mDummySurface);
//        mEglCore?.releaseSurface(mDrawSurface);
//        mEglCore?.release();
//        return true;
//    }
//
//    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
//
//    }
//
//    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
//        if (mTextureDestroyed) {
//            return
//        }
//
//        if (!mEglCore?.isCurrent(mDrawSurface)) {
//            mEglCore?.makeCurrent(mDrawSurface)
//        }
//        try {
//            mPreviewSurfaceTexture.updateTexImage()
//            mPreviewSurfaceTexture.getTransformMatrix(mTransform)
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//        }
//        if (!mMVPMatrixInit) {
//
//            val frameRatio: Float = DEFAULT_CAPTURE_HEIGHT / DEFAULT_CAPTURE_WIDTH.toFloat()
//            val surfaceRatio: Float = mSurfaceWidth / mSurfaceHeight.toFloat()
//            Matrix.setIdentityM(mMVPMatrix, 0)
//            if (frameRatio >= surfaceRatio) {
//                val w: Float = DEFAULT_CAPTURE_WIDTH * surfaceRatio
//                val scaleW: Float = DEFAULT_CAPTURE_HEIGHT / w
//                Matrix.scaleM(mMVPMatrix, 0, scaleW, 1F, 1F)
//            } else {
//                val h: Float = DEFAULT_CAPTURE_HEIGHT / surfaceRatio
//                val scaleH: Float = DEFAULT_CAPTURE_WIDTH / h
//                Matrix.scaleM(mMVPMatrix, 0, 1F, scaleH, 1F)
//            }
//            mMVPMatrixInit = true
//        }
//        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight)
//        mProgram.drawFrame(mPreviewTexture, mTransform, mMVPMatrix)
//        mEglCore?.swapBuffers(mDrawSurface)
//
//        if (joined) {
//            val frame = AgoraVideoFrame()
//            frame.textureID = mPreviewTexture
//            frame.format = AgoraVideoFrame.FORMAT_TEXTURE_OES
//            frame.transform = mTransform
//            frame.stride = DEFAULT_CAPTURE_HEIGHT
//            frame.height = DEFAULT_CAPTURE_WIDTH
//            frame.eglContext14 = mEglCore.getEGLContext()
//            frame.timeStamp = System.currentTimeMillis()
//            val a: Boolean = engine.pushExternalVideoFrame(frame)
//            Log.e(TAG, "pushExternalVideoFrame:$a")
//        }
//    }
//
//    private val iRtcEngineEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
//
//        override fun onWarning(warn: Int) {
//            Log.w(
//                TAG,
//                String.format(
//                    "onWarning code %d message %s",
//                    warn,
//                    RtcEngine.getErrorDescription(warn)
//                )
//            )
//        }
//
//        override fun onError(err: Int) {
//            Log.e(
//                TAG,
//                String.format("onError code %d message %s", err, RtcEngine.getErrorDescription(err))
//            )
////            showAlert(
////                String.format(
////                    "onError code %d message %s",
////                    err,
////                    RtcEngine.getErrorDescription(err)
////                )
////            )
//        }
//
//        override fun onLeaveChannel(stats: RtcStats) {
//            super.onLeaveChannel(stats)
//            Log.i(TAG, java.lang.String.format("local user %d leaveChannel!", myUid))
////            showLongToast(java.lang.String.format("local user %d leaveChannel!", myUid))
//        }
//
//        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
//            Log.i(TAG, String.format("onJoinChannelSuccess channel %s uid %d", channel, uid))
////            showLongToast(String.format("onJoinChannelSuccess channel %s uid %d", channel, uid))
//            myUid = uid
//            joined = true
//            handler.post(Runnable {
//                join?.isEnabled = true
//                join?.text = getString(R.string.leave)
//            })
//        }
//
//        override fun onUserJoined(uid: Int, elapsed: Int) {
//            super.onUserJoined(uid, elapsed)
//            Log.i(TAG, "onUserJoined->$uid")
//            handler.post {
//                val surfaceView = RtcEngine.CreateRendererView(this@PoseNetActivity)
//                surfaceView.setZOrderMediaOverlay(true)
//                if (fl_remote.getChildCount() > 0) {
//                    fl_remote.removeAllViews()
//                }
//                fl_remote.addView(
//                    surfaceView, FrameLayout.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT
//                    )
//                )
//                engine?.setupRemoteVideo(VideoCanvas(surfaceView, RENDER_MODE_HIDDEN, uid))
//            }
//        }
//
//        override fun onUserOffline(uid: Int, reason: Int) {
//            Log.i(TAG, String.format("user %d offline! reason:%d", uid, reason))
////            showLongToast(String.format("user %d offline! reason:%d", uid, reason))
//            handler.post(Runnable {
//                engine?.setupRemoteVideo(VideoCanvas(null, RENDER_MODE_HIDDEN, uid))
//            })
//        }
//    }
//
//    private fun joinChannel(channelId: String) {
//        val textureView = TextureView(this)
//        textureView.surfaceTextureListener = this
//        fl_local.addView(textureView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
//        val res = engine!!.joinChannel(accessToken, channelId, "Extra Optional Data", 0)
//
//
////        val textureView = TextureView(this)
////        textureView.surfaceTextureListener = this
////        frameview.addView(
////            textureView, FrameLayout.LayoutParams(
////                ViewGroup.LayoutParams.MATCH_PARENT,
////                ViewGroup.LayoutParams.MATCH_PARENT
////            )
////        )
////        engine!!.setDefaultAudioRoutetoSpeakerphone(true)
////        engine!!.setEnableSpeakerphone(false)
////        engine!!.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
////        engine!!.setClientRole(IRtcEngineEventHandler.ClientRole.CLIENT_ROLE_BROADCASTER)
////        engine!!.enableVideo()
//////        engine!!.setVideoEncoderConfiguration(
//////            VideoEncoderConfiguration((this@PoseNetActivity.application as MainApplication).getGlobalSettings().getVideoEncodingDimensionObject(),
//////                VideoEncoderConfiguration.FRAME_RATE.valueOf((getActivity().getApplication() as MainApplication).getGlobalSettings().getVideoEncodingFrameRate()), STANDARD_BITRATE,
//////                VideoEncoderConfiguration.ORIENTATION_MODE.valueOf((getActivity().getApplication() as MainApplication).getGlobalSettings().getVideoEncodingOrientation())
//////            )
//////        )
////        engine!!.setExternalVideoSource(true, true, true)
////        var accessToken: String? = getString(R.string.agora_access_token)
////        if (TextUtils.equals(accessToken, "") || TextUtils.equals(
////                accessToken,
////                "<#YOUR ACCESS TOKEN#>"
////            )
////        ) {
////            accessToken = null
////        }
////
////        val option = ChannelMediaOptions()
////        option.autoSubscribeAudio = true
////        option.autoSubscribeVideo = true
////        val res = engine!!.joinChannel(accessToken, channelId, "Extra Optional Data", 0, option)
////        if (res != 0) {
//////            showAlert(RtcEngine.getErrorDescription(Math.abs(res)))
////            return
////        }
////        join?.isEnabled = false
//    }
}