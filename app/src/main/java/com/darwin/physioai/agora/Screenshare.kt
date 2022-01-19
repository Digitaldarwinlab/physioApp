//package com.darwin.physioai.agora
//
//import android.app.Activity
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.content.ServiceConnection
//import android.media.projection.MediaProjectionManager
//import android.os.*
//import android.text.TextUtils
//import android.util.DisplayMetrics
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.TextureView
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.EditText
//import android.widget.RelativeLayout
//import com.darwin.physioai.R
//
//
//import io.agora.rtc.Constants
//import io.agora.rtc.IRtcEngineEventHandler
//import io.agora.rtc.RtcEngine
//import io.agora.rtc.video.VideoEncoderConfiguration
//import io.agora.rtc.video.VideoEncoderConfiguration.ORIENTATION_MODE
//import io.agora.rtc.video.VideoEncoderConfiguration.VideoDimensions
//import java.io.File
//import java.lang.Exception
//
//class Screenshare : BaseFragment(), View.OnClickListener {
//    private var fl_local: RelativeLayout? = null
//    private var join: Button? = null
//    private var localVideo: Button? = null
//    private var et_channel: EditText? = null
//    private var myUid = 0
//    private var joined = false
//    private var mLocalVideoPath: String? = null
//    private var mLocalVideoExists = false
//    private var mService: IExternalVideoInputService? = null
//    private var mServiceConnection: VideoInputServiceConnection? = null
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_switch_external_video, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        join = view.findViewById(R.id.btn_join)
//        localVideo = view.findViewById(R.id.localVideo)
//        et_channel = view.findViewById(R.id.et_channel)
//        fl_local = view.findViewById(R.id.fl_local)
//        join.setOnClickListener(this)
//        localVideo.setOnClickListener(this)
//        checkLocalVideo()
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        // Check if the context is valid
//        val context = context ?: return
//        try {
//
//            javax.script.ScriptEngine.ENGINE = RtcEngine.create(
//                context.applicationContext,
//                getString(R.string.agora_app_id),
//                iRtcEngineEventHandler
//            )
//        } catch (e: Exception) {
//            e.printStackTrace()
//            activity!!.onBackPressed()
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PROJECTION_REQ_CODE && resultCode == Activity.RESULT_OK) {
//            try {
//                val metrics = DisplayMetrics()
//                activity!!.windowManager.defaultDisplay.getMetrics(metrics)
//                data!!.putExtra(ExternalVideoInputManager.FLAG_SCREEN_WIDTH, metrics.widthPixels)
//                data.putExtra(ExternalVideoInputManager.FLAG_SCREEN_HEIGHT, metrics.heightPixels)
//                data.putExtra(ExternalVideoInputManager.FLAG_SCREEN_DPI, metrics.density.toInt())
//                data.putExtra(ExternalVideoInputManager.FLAG_FRAME_RATE, DEFAULT_SHARE_FRAME_RATE)
//                setVideoConfig(
//                    ExternalVideoInputManager.TYPE_SCREEN_SHARE,
//                    metrics.widthPixels,
//                    metrics.heightPixels
//                )
//                mService.setExternalVideoInput(ExternalVideoInputManager.TYPE_SCREEN_SHARE, data)
//            } catch (e: RemoteException) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        unbindVideoService()
//        TEXTUREVIEW = null
//        /**leaveChannel and Destroy the RtcEngine instance */
//        if (javax.script.ScriptEngine.ENGINE != null) {
//            javax.script.ScriptEngine.ENGINE.leaveChannel()
//        }
//        handler.post(Runnable { RtcEngine.destroy() })
//        javax.script.ScriptEngine.ENGINE = null
//        super.onDestroy()
//    }
//
//    override fun onClick(v: View) {
//        if (v.id == R.id.btn_join) {
//            if (!joined) {
//                CommonUtil.hideInputBoard(activity, et_channel)
//                /**Instantiate the view ready to display the local preview screen */
//                TEXTUREVIEW = TextureView(
//                    context!!
//                )
//                // call when join button hit
//                val channelId = et_channel!!.text.toString()
//                // Check permission
//                if (AndPermission.hasPermissions(
//                        this,
//                        Permission.Group.STORAGE,
//                        Permission.Group.MICROPHONE,
//                        Permission.Group.CAMERA
//                    )
//                ) {
//                    joinChannel(channelId)
//                    return
//                }
//                // Request permission
//                AndPermission.with(this).runtime().permission(
//                    Permission.Group.STORAGE,
//                    Permission.Group.MICROPHONE,
//                    Permission.Group.CAMERA
//                ).onGranted { permissions ->
//                    // Permissions Granted
//                    joinChannel(channelId)
//                }.start()
//            } else {
//                joined = false
//                join!!.text = getString(R.string.join)
//                localVideo!!.isEnabled = false
//                fl_local!!.removeAllViews()
//                /**After joining a channel, the user must call the leaveChannel method to end the
//                 * call before joining another channel. This method returns 0 if the user leaves the
//                 * channel and releases all resources related to the call. This method call is
//                 * asynchronous, and the user has not exited the channel when the method call returns.
//                 * Once the user leaves the channel, the SDK triggers the onLeaveChannel callback.
//                 * A successful leaveChannel method call triggers the following callbacks:
//                 * 1:The local client: onLeaveChannel.
//                 * 2:The remote client: onUserOffline, if the user leaving the channel is in the
//                 * Communication channel, or is a BROADCASTER in the Live Broadcast profile.
//                 * @returns 0: Success.
//                 * < 0: Failure.
//                 * PS:
//                 * 1:If you call the destroy method immediately after calling the leaveChannel
//                 * method, the leaveChannel process interrupts, and the SDK does not trigger
//                 * the onLeaveChannel callback.
//                 * 2:If you call the leaveChannel method during CDN live streaming, the SDK
//                 * triggers the removeInjectStreamUrl method.
//                 */
//                javax.script.ScriptEngine.ENGINE.leaveChannel()
//                TEXTUREVIEW = null
//                unbindVideoService()
//            }
//        } else if (v.id == R.id.localVideo) {
//            try {
//                val intent = Intent()
//                setVideoConfig(
//                    ExternalVideoInputManager.TYPE_LOCAL_VIDEO,
//                    LOCAL_VIDEO_WIDTH,
//                    LOCAL_VIDEO_HEIGHT
//                )
//                intent.putExtra(ExternalVideoInputManager.FLAG_VIDEO_PATH, mLocalVideoPath)
//                if (mService.setExternalVideoInput(
//                        ExternalVideoInputManager.TYPE_LOCAL_VIDEO,
//                        intent
//                    )
//                ) {
//                    fl_local!!.removeAllViews()
//                    fl_local!!.addView(
//                        TEXTUREVIEW,
//                        RelativeLayout.LayoutParams.MATCH_PARENT,
//                        RelativeLayout.LayoutParams.MATCH_PARENT
//                    )
//                }
//            } catch (e: RemoteException) {
//                e.printStackTrace()
//            }
//        } else if (v.id == R.id.screenShare) {
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//                /**remove local preview */
//                fl_local!!.removeAllViews()
//                /** */
//                val mpm =
//                    context!!.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
//                val intent = mpm.createScreenCaptureIntent()
//                startActivityForResult(intent, PROJECTION_REQ_CODE)
//            } else {
//                showAlert(getString(R.string.lowversiontip))
//            }
//        }
//    }
//
//    private fun checkLocalVideo(): Boolean {
//        val dir = context!!.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
//        val videoFile = File(dir, VIDEO_NAME)
//        mLocalVideoPath = videoFile.absolutePath
//        mLocalVideoExists = videoFile.exists()
//        if (!mLocalVideoExists) {
//            showAlert(
//                String.format(
//                    getString(R.string.alert_no_local_video_message),
//                    mLocalVideoPath
//                )
//            )
//        }
//        return mLocalVideoExists
//    }
//
//    private fun setVideoConfig(sourceType: Int, width: Int, height: Int) {
//        val mode: ORIENTATION_MODE
//        mode =
//            when (sourceType) {
//                ExternalVideoInputManager.TYPE_LOCAL_VIDEO, ExternalVideoInputManager.TYPE_SCREEN_SHARE -> ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
//                else -> ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
//            }
//        /**Setup video stream encoding configs */
//        javax.script.ScriptEngine.ENGINE.setVideoEncoderConfiguration(
//            VideoEncoderConfiguration(
//                VideoDimensions(width, height),
//                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
//                VideoEncoderConfiguration.STANDARD_BITRATE, mode
//            )
//        )
//    }
//
//    private fun joinChannel(channelId: String) {
//        // Check if the context is valid
//        val context = context ?: return
//
//        javax.script.ScriptEngine.ENGINE.setChannelProfile(
//            Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
//        )
//        /**Sets the role of a user (Live Broadcast only). */
//        javax.script.ScriptEngine.ENGINE.setClientRole(
//            Constants.CLIENT_ROLE_BROADCASTER
//        )
//        /**Enable video module */
//        javax.script.ScriptEngine.ENGINE.enableVideo()
//        /**Set up to play remote sound with receiver */
//        javax.script.ScriptEngine.ENGINE.setDefaultAudioRoutetoSpeakerphone(true)
//        javax.script.ScriptEngine.ENGINE.setEnableSpeakerphone(false)
//
//        var accessToken: String? = getString(R.string.agora_access_token)
//        if (TextUtils.equals(accessToken, "") || TextUtils.equals(
//                accessToken,
//                "<#YOUR ACCESS TOKEN#>"
//            )
//        ) {
//            accessToken = null
//        }
//
//        val res: Int = javax.script.ScriptEngine.ENGINE.joinChannel(
//            accessToken,
//            channelId,
//            "Extra Optional Data",
//            0
//        )
//        if (res != 0) {
//
//            showAlert(RtcEngine.getErrorDescription(Math.abs(res)))
//            return
//        }
//
//        join!!.isEnabled = false
//    }
//
//    private fun bindVideoService() {
//        val intent = Intent()
//        intent.setClass(context!!, ExternalVideoInputService::class.java)
//        mServiceConnection = VideoInputServiceConnection()
//        context!!.bindService(intent, mServiceConnection!!, Context.BIND_AUTO_CREATE)
//    }
//
//    private fun unbindVideoService() {
//        if (mServiceConnection != null) {
//            context!!.unbindService(mServiceConnection!!)
//            mServiceConnection = null
//        }
//    }
//
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
//            showAlert(
//                String.format(
//                    "onError code %d message %s",
//                    err,
//                    RtcEngine.getErrorDescription(err)
//                )
//            )
//        }
//
//        /**Occurs when the local user joins a specified channel.
//         * The channel name assignment is based on channelName specified in the joinChannel method.
//         * If the uid is not specified when joinChannel is called, the server automatically assigns a uid.
//         * @param channel Channel name
//         * @param uid User ID
//         * @param elapsed Time elapsed (ms) from the user calling joinChannel until this callback is triggered
//         */
//        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
//            Log.i(TAG, String.format("onJoinChannelSuccess channel %s uid %d", channel, uid))
//            showLongToast(String.format("onJoinChannelSuccess channel %s uid %d", channel, uid))
//            myUid = uid
//            joined = true
//            handler.post(Runnable {
//                join!!.isEnabled = true
//                join!!.text = getString(R.string.leave)
//                localVideo!!.isEnabled = mLocalVideoExists
//                bindVideoService()
//            })
//        }
//
//        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
//            super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
//            Log.i(
//                TAG,
//                "onRemoteVideoStateChanged:uid->$uid, state->$state"
//            )
//            //            if (state == REMOTE_VIDEO_STATE_STARTING) {
////                /**Check if the context is correct*/
////                Context context = getContext();
////                if (context == null) {
////                    return;
////                }
////                handler.post(() ->
////                {
////                    /**Display remote video stream*/
////                    SurfaceView surfaceView = RtcEngine.CreateRendererView(context);
////                    surfaceView.setZOrderMediaOverlay(true);
////                    if (fl_remote.getChildCount() > 0) {
////                        fl_remote.removeAllViews();
////                    }
////                    fl_remote.addView(surfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
////                            ViewGroup.LayoutParams.MATCH_PARENT));
////                    /**Setup remote video to render*/
////                    ENGINE.setupRemoteVideo(new VideoCanvas(surfaceView, RENDER_MODE_HIDDEN, uid));
////                });
////            }
//        }
//
//        override fun onUserJoined(uid: Int, elapsed: Int) {
//            super.onUserJoined(uid, elapsed)
//            Log.i(TAG, "onUserJoined->$uid")
//            showLongToast(String.format("user %d joined!", uid))
//        }
//
//        override fun onUserOffline(uid: Int, reason: Int) {
//            Log.i(TAG, String.format("user %d offline! reason:%d", uid, reason))
//            //            showLongToast(String.format("user %d offline! reason:%d", uid, reason));
////            handler.post(new Runnable() {
////                @Override
////                public void run() {
////                    /**Clear render view
////                     Note: The video will stay at its last frame, to completely remove it you will need to
////                     remove the SurfaceView from its parent*/
////                    ENGINE.setupRemoteVideo(new VideoCanvas(null, RENDER_MODE_HIDDEN, uid));
////                    fl_remote.removeAllViews();
////                }
////            });
//        }
//    }
//
//    private inner class VideoInputServiceConnection : ServiceConnection {
//        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
//            mService = iBinder as IExternalVideoInputService
//        }
//
//        override fun onServiceDisconnected(componentName: ComponentName) {
//            mService = null
//        }
//    }
//
//    companion object {
//        private val TAG = Screenshare::class.java.simpleName
//        private const val VIDEO_NAME = "localvideo.mp4"
//        private const val PROJECTION_REQ_CODE = 1 shl 2
//        private const val DEFAULT_SHARE_FRAME_RATE = 15
//
//        /**
//         * The developers should defines their video dimension, for the
//         * video info cannot be obtained before the video is extracted.
//         */
//        private const val LOCAL_VIDEO_WIDTH = 1280
//        private const val LOCAL_VIDEO_HEIGHT = 720
//    }
//}