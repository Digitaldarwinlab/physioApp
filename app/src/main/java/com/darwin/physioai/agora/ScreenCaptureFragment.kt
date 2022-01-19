//package com.darwin.physioai.agora
//
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.hardware.display.DisplayManager
//import android.hardware.display.VirtualDisplay
//import android.media.projection.MediaProjection
//import android.media.projection.MediaProjectionManager
//import android.os.Build
//import android.os.Bundle
//import android.os.Handler
//import android.util.DisplayMetrics
//import android.util.Log
//import android.view.*
//import android.widget.Button
//import android.widget.Toast
//import androidx.annotation.RequiresApi
//import androidx.fragment.app.Fragment
//import com.darwin.physioai.R
//
//class ScreenCaptureFragment : Fragment(), View.OnClickListener {
//
//    private var mScreenDensity = 0
//    private var mResultCode = 0
//    private var mResultData: Intent? = null
//    private var mSurface: Surface? = null
//    private var mMediaProjection: MediaProjection? = null
//    private var mVirtualDisplay: VirtualDisplay? = null
//    private var mMediaProjectionManager: MediaProjectionManager? = null
//    private var mButtonToggle: Button? = null
//    private var mSurfaceView: SurfaceView? = null
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        if (savedInstanceState != null) {
//            mResultCode = savedInstanceState.getInt(STATE_RESULT_CODE)
//            mResultData = savedInstanceState.getParcelable<Intent>(STATE_RESULT_DATA)
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return  inflater.inflate(R.layout.fragment_screen_capture, container, false)
//    }
//
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        mSurfaceView = view.findViewById<View>(R.id.surface) as SurfaceView
//        mSurface = mSurfaceView!!.holder.surface
//        mButtonToggle = view.findViewById<View>(R.id.toggle) as Button
//        mButtonToggle!!.setOnClickListener(this)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        val activity: Activity = activity!!
//        val metrics = DisplayMetrics()
//        activity.windowManager.defaultDisplay.getMetrics(metrics)
//        mScreenDensity = metrics.densityDpi
//        mMediaProjectionManager =
//            activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        if (mResultData != null) {
//            outState.putInt(STATE_RESULT_CODE, mResultCode)
//            outState.putParcelable(STATE_RESULT_DATA, mResultData)
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    override fun onClick(v: View) {
//        when (v.id) {
//            R.id.toggle -> if (mVirtualDisplay == null) {
//                startScreenCapture()
//            } else {
//                stopScreenCapture()
//            }
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == REQUEST_MEDIA_PROJECTION) {
//            if (resultCode != Activity.RESULT_OK) {
//                Log.i(TAG, "User cancelled")
//                Toast.makeText(activity, "User Cancelled", Toast.LENGTH_SHORT).show()
//                return
//            }
//            val activity: Activity = requireActivity()
//            Log.i(TAG, "Starting screen capture")
//            mResultCode = resultCode
//            mResultData = data
//            setUpMediaProjection()
//            setUpVirtualDisplay()
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        stopScreenCapture()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    override fun onDestroy() {
//        super.onDestroy()
//        tearDownMediaProjection()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun setUpMediaProjection() {
//        mMediaProjection = mMediaProjectionManager?.getMediaProjection(mResultCode, mResultData!!)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun tearDownMediaProjection() {
//        if (mMediaProjection != null) {
//            mMediaProjection!!.stop()
//            mMediaProjection = null
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun startScreenCapture() {
//        val activity: Activity = requireActivity()
//        if (mSurface == null || activity == null) {
//            return
//        }
//        if (mMediaProjection != null) {
//            setUpVirtualDisplay()
//        } else if (mResultCode != 0 && mResultData != null) {
//            setUpMediaProjection()
//            setUpVirtualDisplay()
//        } else {
//            Log.i(TAG, "Requesting confirmation")
//            // This initiates a prompt dialog for the user to confirm screen projection.
//            startActivityForResult(
//                mMediaProjectionManager?.createScreenCaptureIntent(),
//                REQUEST_MEDIA_PROJECTION
//            )
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun setUpVirtualDisplay() {
//        Log.i(TAG, "Setting up a VirtualDisplay: " + mSurfaceView?.width + "x" + mSurfaceView?.height + " (" + mScreenDensity + ")")
//        mVirtualDisplay = mMediaProjection?.createVirtualDisplay("ScreenCapture", mSurfaceView?.width!!, mSurfaceView?.height!!, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, null, null)
//        mButtonToggle?.text = "Stop"
//    }
//
//    private fun stopScreenCapture() {
//        if (mVirtualDisplay == null) {
//            return
//        }
//        mVirtualDisplay!!.release()
//        mVirtualDisplay = null
//        mButtonToggle?.text = "Start"
//    }
//
//    companion object {
//        private const val TAG = "ScreenCaptureFragment"
//        private const val STATE_RESULT_CODE = "result_code"
//        private const val STATE_RESULT_DATA = "result_data"
//        private const val REQUEST_MEDIA_PROJECTION = 1
//    }
//}