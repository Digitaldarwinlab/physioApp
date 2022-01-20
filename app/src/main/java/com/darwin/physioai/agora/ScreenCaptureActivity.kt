package com.darwin.physioai.agora

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.view.SurfaceView
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.darwin.physioai.R


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ScreenCaptureActivity : Activity() {

    private var target : SurfaceView? = null

    object variables {
        @SuppressLint("StaticFieldLeak")
        var targetview :SurfaceView? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_screen_capture)

        // start projection
        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener { startProjection() }

        // stop projection
        val stopButton = findViewById<Button>(R.id.stopButton)
        stopButton.setOnClickListener { stopProjection() }

        target = findViewById<SurfaceView>(R.id.surfaceview)
        variables.targetview = target
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startService(ScreenCaptureService.getStartIntent(this, resultCode, data))
            }
        }
    }



    private fun startProjection() {
        val mProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE)
    }

    private fun stopProjection() {
        startService(ScreenCaptureService.getStopIntent(this))
    }

    companion object {
        private const val REQUEST_CODE = 100
    }
}