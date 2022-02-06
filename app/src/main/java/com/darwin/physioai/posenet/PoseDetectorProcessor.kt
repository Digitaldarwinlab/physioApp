
package com.darwin.physioai.posenet

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.app.mlkit.core.VisionProcessorBase
import com.darwin.physioai.PoseNet.PoseGraphic
import com.darwin.physioai.posenet.core.GraphicOverlay
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase


/** A processor to run pose detector.  */
class PoseDetectorProcessor(
  private var lifecycleOwner: LifecycleOwner,
  private var context: Context,
  options: PoseDetectorOptionsBase,
  private val showInFrameLikelihood: Boolean,
  private val visualizeZ: Boolean,
  private val rescaleZForVisualization: Boolean
) : VisionProcessorBase<Pose>(context) {

  private val detector: PoseDetector = PoseDetection.getClient(options)

  override fun stop() {
    super.stop()
    detector.close()
  }

  override fun detectInImage(image: InputImage): Task<Pose> {
    return detector.process(image)
  }
  override fun onSuccess(results: Pose, graphicOverlay: GraphicOverlay) =

  graphicOverlay.add(PoseGraphic(lifecycleOwner ,graphicOverlay, context, results, showInFrameLikelihood, visualizeZ, rescaleZForVisualization))

  override fun onFailure(e: Exception) {
    Log.e(TAG, "Pose detection failed!", e)
  }

  companion object {
    private const val TAG = "PoseDetectorProcessor"
  }

}
