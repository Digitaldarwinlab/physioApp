//package com.darwin.physioai.agora
//
//import android.app.Activity
//import android.content.Intent
//import android.media.projection.MediaProjectionManager
//import android.os.Bundle
//import android.widget.Button
//
//class ScreenCaptureActivity : Activity() {
//    /****************************************** Activity Lifecycle methods  */
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_main)
//
//        // start projection
////        val startButton = findViewById<Button>(R.id.startButton)
////        startButton.setOnClickListener { startProjection() }
////
////        // stop projection
////        val stopButton = findViewById<Button>(R.id.stopButton)
////        stopButton.setOnClickListener { stopProjection() }
//    }
//
////    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
////        if (requestCode == REQUEST_CODE) {
////            if (resultCode == RESULT_OK) {
////                startService(
////                    com.mtsahakis.mediaprojectiondemo.ScreenCaptureService.getStartIntent(
////                        this,
////                        resultCode,
////                        data
////                    )
////                )
////            }
////        }
////    }
////
////    /****************************************** UI Widget Callbacks  */
////    private fun startProjection() {
////        val mProjectionManager =
////            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
////        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE)
////    }
////
////    private fun stopProjection() {
////        startService(com.mtsahakis.mediaprojectiondemo.ScreenCaptureService.getStopIntent(this))
////    }
//
//    companion object {
//        private const val REQUEST_CODE = 100
//    }
//}