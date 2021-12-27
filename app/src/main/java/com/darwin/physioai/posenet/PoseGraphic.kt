
package com.darwin.physioai.PoseNet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.text.TextUtils
import android.util.Log
import com.darwin.physioai.posenet.AfterStats
import com.darwin.physioai.posenet.PoseNetActivity
import com.darwin.physioai.posenet.core.GraphicOverlay
import com.darwin.physioai.posenet.core.InferenceInfoGraphic
import com.google.common.primitives.Ints
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.atan2

/** Draw the detected pose in preview. */
class PoseGraphic internal constructor(overlay: GraphicOverlay,
                                       private var context: Context,
                                       private val pose: Pose,
                                       private val showInFrameLikelihood: Boolean,
                                       private val visualizeZ: Boolean,
                                       private val rescaleZForVisualization: Boolean,
                                       ) :
  GraphicOverlay.Graphic(overlay) {
  private val leftPaint: Paint
  private val whitePaint: Paint = Paint()
  private val ringPaint: Paint
  private var zMin = Float.MAX_VALUE
  private var zMax = Float.MIN_VALUE
  private val bounds = Rect()

  @SuppressLint("SimpleDateFormat")
  override fun draw(canvas: Canvas) {
    val landmarks = pose.allPoseLandmarks
    val l = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
    if (landmarks.isEmpty()) {
      return
    }

    for (landmark in landmarks) {
      drawPoint(canvas, landmark, whitePaint)
      if (visualizeZ && rescaleZForVisualization) {
        zMin = zMin.coerceAtMost(landmark.position3D.z)
        zMax = zMax.coerceAtLeast(landmark.position3D.z)
      }
    }


    val nose = pose.getPoseLandmark(PoseLandmark.NOSE)
    val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
    val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
    val shoulx = (leftShoulder!!.position.x + rightShoulder!!.position.x) / 2
    val shouly = (leftShoulder.position.y + rightShoulder.position.y) / 2
    canvas.drawCircle(translateX(shoulx), translateY(shouly), DOT_RADIUS, whitePaint)
    val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
    val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
    val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
    val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
    val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
    val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
    val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
    val hipx = (leftHip!!.position.x + rightHip!!.position.x) / 2
    val hipy = (leftHip.position.y + rightHip.position.y) / 2
    canvas.drawCircle(translateX(hipx), translateY(hipy), DOT_RADIUS, whitePaint)
    val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
    val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
    val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
    val leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)
    val rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
    val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
    val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
    val leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)
    val rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
    val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
    val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
    val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
    val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)


    //Calculating the Angle between two landmarks
    val righthipangle = getAngle(leftHip, rightHip, rightKnee)
    ringPaint.getTextBounds("$righthipangle", 0, "$righthipangle".length, bounds)
    canvas.drawText(
      "$righthipangle",
      translateX(rightHip.position.x - (bounds.width() / 4)),
      translateY(rightHip.position.y),
      whitePaint
    )

    val lefthipangle = getAngle(rightHip, leftHip, leftKnee)
    canvas.drawText(
      "$lefthipangle",
      translateX(leftHip.position.x + (bounds.width())),
      translateY(leftHip.position.y),
      whitePaint
    )
    ringPaint.getTextBounds("$lefthipangle", 0, "$lefthipangle".length, bounds)

    val rightkneeangle = getAngle(rightHip, rightKnee, rightAnkle)
    canvas.drawText(
      "$rightkneeangle",
      translateX(rightKnee!!.position.x - (bounds.width() / 4)),
      translateY(rightKnee.position.y),
      whitePaint
    )
    ringPaint.getTextBounds("$rightkneeangle", 0, "$rightkneeangle".length, bounds)

    val leftkneeangle = getAngle(leftHip, leftKnee, leftAnkle)
    canvas.drawText(
      "$leftkneeangle",
      translateX(leftKnee!!.position.x + (bounds.width())),
      translateY(leftKnee.position.y),
      whitePaint
    )
    ringPaint.getTextBounds("$leftkneeangle", 0, "$leftkneeangle".length, bounds)

    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
    val currentDate = sdf.format(Date())
    val angle = arrayOf(leftkneeangle)
    angle25public = leftkneeangle
    for (elements in angle) {
      drawText(canvas, elements.toString(), 3)
      Log.i("angle of knee", "$elements    $currentDate")
      print("$elements   $currentDate")
    }


    val rightelbowangle = getAngle(rightShoulder, rightElbow, rightWrist)
    canvas.drawText(
      "$rightelbowangle",
      translateX(rightElbow!!.position.x - (bounds.width() / 4)),
      translateY(rightElbow.position.y),
      whitePaint
    )
    ringPaint.getTextBounds("$rightelbowangle", 0, "$rightelbowangle".length, bounds)
    //canvas.drawCircle(translateX(rightElbow.position.x - (bounds.width() / 2)), translateY(rightElbow.position.y), bounds.width().toFloat() - 20.0f, ringPaint)

    val leftelbowangle = getAngle(leftShoulder, leftElbow, leftWrist)
    canvas.drawText(
      "$leftelbowangle",
      translateX(leftElbow!!.position.x + (bounds.width())),
      translateY(leftElbow.position.y),
      whitePaint
    )
    ringPaint.getTextBounds("$leftelbowangle", 0, "$leftelbowangle".length, bounds)
    //canvas.drawCircle(translateX(leftElbow.position.x + (bounds.width() / 2)), translateY(leftElbow.position.y), bounds.width().toFloat() - 20.0F, ringPaint)

    val rightAnkleAngle = getAngle(rightKnee, rightAnkle, rightFootIndex)
    canvas.drawText(
      "$rightAnkleAngle",
      translateX(rightAnkle!!.position.x - (bounds.width() / 4)),
      translateY(rightAnkle.position.y),
      whitePaint
    )
    ringPaint.getTextBounds("$rightAnkleAngle", 0, "$rightAnkleAngle".length, bounds)
    //canvas.drawCircle(translateX(rightAnkle.position.x - (bounds.width() / 2)), translateY(rightAnkle.position.y), bounds.width().toFloat() - 20.0f, ringPaint)

    val leftAnkleAngle = getAngle(leftKnee, leftAnkle, leftFootIndex)
    canvas.drawText(
      "$leftAnkleAngle",
      translateX(leftAnkle!!.position.x + (bounds.width())),
      translateY(leftAnkle.position.y),
      whitePaint
    )
    ringPaint.getTextBounds("$leftAnkleAngle", 0, "$leftAnkleAngle".length, bounds)
    //canvas.drawCircle(translateX(leftKnee.position.x + (bounds.width() / 2)), translateY(leftKnee.position.y), bounds.width().toFloat() - 20.0f, ringPaint)

    val leftwristAngle = getAngle(leftElbow, leftWrist, leftPinky)
    canvas.drawText(
      "$leftwristAngle",
      translateX(leftWrist!!.position.x + (bounds.width())),
      translateY(leftWrist.position.y),
      whitePaint
    )
    ringPaint.getTextBounds("$leftwristAngle", 0, "$leftwristAngle".length, bounds)
    //canvas.drawCircle(translateX(leftWrist.position.x + (bounds.width() / 2)), translateY(leftWrist.position.y), bounds.width().toFloat() - 20.0f, ringPaint)

    val rightwristAngle = getAngle(rightElbow, rightWrist, rightPinky)
    canvas.drawText(
      "$rightwristAngle",
      translateX(rightWrist!!.position.x - (bounds.width() / 4)),
      translateY(rightWrist.position.y),
      whitePaint
    )
    ringPaint.getTextBounds("$rightwristAngle", 0, "$rightwristAngle".length, bounds)
    //canvas.drawCircle(translateX(rightWrist.position.x - (bounds.width() / 2)), translateY(rightWrist.position.y), bounds.width().toFloat() - 20.0f, ringPaint)

    val rightShoulderAngle = getAngle(rightElbow, rightShoulder, leftShoulder)
    canvas.drawText(
      "$rightShoulderAngle",
      translateX(rightShoulder.position.x - (bounds.width() / 4)),
      translateY(rightShoulder.position.y),
      whitePaint
    )
    ringPaint.getTextBounds("$rightShoulderAngle", 0, "$rightShoulderAngle".length, bounds)
    //canvas.drawCircle(translateX(rightShoulder.position.x - (bounds.width() / 2)), translateY(rightShoulder.position.y), bounds.width().toFloat() - 20.0f, ringPaint)

    val leftshoulderAngle = getAngle(leftElbow, leftShoulder, rightShoulder)
    canvas.drawText(
      "$leftshoulderAngle",
      translateX(leftShoulder.position.x + (bounds.width())),
      translateY(leftShoulder.position.y),
      whitePaint
    )
    ringPaint.getTextBounds("$leftshoulderAngle", 0, "$leftshoulderAngle".length, bounds)

    Log.d("LogTagLeftElbow", leftshoulderAngle.toString())
    //canvas.drawCircle(translateX(leftShoulder.position.x + (bounds.width() / 2)), translateY(leftShoulder.position.y), bounds.width().toFloat() - 20.0f, ringPaint)

    val leftneckAngle = getAngleBasedonXY(
      leftShoulder.position.x,
      leftShoulder.position.y,
      shoulx,
      shouly,
      nose.position.x,
      nose.position.y
    )
//    canvas.drawText("$leftshoulderAngle", translateX(leftShoulder.position.x + (bounds.width())), translateY(leftShoulder.position.y), whitePaint)
//    ringPaint.getTextBounds("$leftshoulderAngle", 0, "$leftshoulderAngle".length, bounds)

    val RightneckAngle = getAngleBasedonXY(
      rightShoulder.position.x,
      rightShoulder.position.y,
      shoulx,
      shouly,
      nose.position.x,
      nose.position.y
    )
//    canvas.drawText("$leftshoulderAngle", translateX(leftShoulder.position.x + (bounds.width())), translateY(leftShoulder.position.y), whitePaint)
//    ringPaint.getTextBounds("$leftshoulderAngle", 0, "$leftshoulderAngle".length, bounds)

    // Starting Position Logic
    var keyPointsCount = 0

    for (landmark in landmarks) {
      if (landmark.inFrameLikelihood < MIN_CONFIDENCE) {
        continue
      }
      keyPointsCount++
    }

    if (keyPointsCount >= 30) {

      replogic0(lefthipangle.toInt())
      replogic1(righthipangle.toInt())
      replogic2(leftshoulderAngle.toInt())
      replogic3(rightShoulderAngle.toInt())
      replogic4(leftkneeangle.toInt())
      replogic5(rightkneeangle.toInt())
      replogic6(rightelbowangle.toInt())
      replogic7(leftelbowangle.toInt())
      replogic8(leftAnkleAngle.toInt())
      replogic9(rightAnkleAngle.toInt())
      replogic10(leftwristAngle.toInt())
      replogic11(rightwristAngle.toInt())
      replogic12(leftneckAngle.toInt())
      replogic13(RightneckAngle.toInt())


      //tts.speak("Great start Excercise", TextToSpeech.QUEUE_FLUSH, null, null)
      //Repitition Logic for Squat
//      val count = OnFrame(nose!!)
//      OnRep(count.toString())

//        if(variable.angle.toString() == "leftknee"){
//          replogic0(leftkneeangle.toInt())
//        }
      drawText(canvas, "Rep Count :$count", 2)
      //rep logic end
    } else if (keyPointsCount <= 30) {
      //tts.speak("Go Back please", TextToSpeech.QUEUE_ADD, null, null)
    }

    if (keyPointsCount <= 30) {
      //Drawing line on the canvas
      drawLine(canvas, leftShoulder, rightShoulder, whitePaint)
      drawLine(canvas, leftHip, rightHip, whitePaint)
      canvas.drawLine(
        translateX(shoulx),
        translateY(shouly),
        translateX(hipx),
        translateY(hipy),
        whitePaint
      )
      val nosepoint = nose!!.position
      canvas.drawLine(
        translateX(nosepoint.x),
        translateY(nosepoint.y),
        translateX(shoulx),
        translateY(shouly),
        whitePaint
      )
      // Left body
      drawLine(canvas, leftShoulder, leftHip, whitePaint)
      drawLine(canvas, leftShoulder, leftElbow, whitePaint)
      drawLine(canvas, leftElbow, leftWrist, whitePaint)
      drawLine(canvas, leftHip, leftKnee, whitePaint)
      drawLine(canvas, leftKnee, leftAnkle, whitePaint)
      drawLine(canvas, leftWrist, leftThumb!!, whitePaint)
      drawLine(canvas, leftWrist, leftPinky!!, whitePaint)
      drawLine(canvas, leftWrist, leftIndex!!, whitePaint)
      drawLine(canvas, leftAnkle, leftHeel!!, whitePaint)
      drawLine(canvas, leftHeel, leftFootIndex!!, whitePaint)
      // Right body
      drawLine(canvas, rightShoulder, rightHip, whitePaint)
      drawLine(canvas, rightShoulder, rightElbow, whitePaint)
      drawLine(canvas, rightElbow, rightWrist, whitePaint)
      drawLine(canvas, rightHip, rightKnee, whitePaint)
      drawLine(canvas, rightKnee, rightAnkle, whitePaint)
      drawLine(canvas, rightWrist, rightThumb!!, whitePaint)
      drawLine(canvas, rightWrist, rightPinky!!, whitePaint)
      drawLine(canvas, rightWrist, rightIndex!!, whitePaint)
      drawLine(canvas, rightAnkle, rightHeel!!, whitePaint)
      drawLine(canvas, rightHeel, rightFootIndex!!, whitePaint)
    } else if (keyPointsCount > 30) {
      drawLine(canvas, leftShoulder, rightShoulder, ringPaint)
      drawLine(canvas, leftHip, rightHip, ringPaint)
      canvas.drawLine(
        translateX(shoulx),
        translateY(shouly),
        translateX(hipx),
        translateY(hipy),
        ringPaint
      )
      val nosepoint = nose!!.position
      canvas.drawLine(
        translateX(nosepoint.x),
        translateY(nosepoint.y),
        translateX(shoulx),
        translateY(shouly),
        ringPaint
      )
      // Left body
      drawLine(canvas, leftShoulder, leftHip, ringPaint)
      drawLine(canvas, leftShoulder, leftElbow, ringPaint)
      drawLine(canvas, leftElbow, leftWrist, ringPaint)
      drawLine(canvas, leftHip, leftKnee, ringPaint)
      drawLine(canvas, leftKnee, leftAnkle, ringPaint)
      drawLine(canvas, leftWrist, leftThumb!!, ringPaint)
      drawLine(canvas, leftWrist, leftPinky!!, ringPaint)
      drawLine(canvas, leftWrist, leftIndex!!, ringPaint)
      drawLine(canvas, leftAnkle, leftHeel!!, ringPaint)
      drawLine(canvas, leftHeel, leftFootIndex!!, ringPaint)
      // Right body
      drawLine(canvas, rightShoulder, rightHip, ringPaint)
      drawLine(canvas, rightShoulder, rightElbow, ringPaint)
      drawLine(canvas, rightElbow, rightWrist, ringPaint)
      drawLine(canvas, rightHip, rightKnee, ringPaint)
      drawLine(canvas, rightKnee, rightAnkle, ringPaint)
      drawLine(canvas, rightWrist, rightThumb!!, ringPaint)
      drawLine(canvas, rightWrist, rightPinky!!, ringPaint)
      drawLine(canvas, rightWrist, rightIndex!!, ringPaint)
      drawLine(canvas, rightAnkle, rightHeel!!, ringPaint)
      drawLine(canvas, rightHeel, rightFootIndex!!, ringPaint)
    }
  }


  fun drawText(canvas: Canvas, text: String, line: Int) {
    if (TextUtils.isEmpty(text)) {
      return
    }
    canvas.drawText(
      text,
      InferenceInfoGraphic.TEXT_SIZE * 0.5f,
      InferenceInfoGraphic.TEXT_SIZE * 3 + InferenceInfoGraphic.TEXT_SIZE * line,
      leftPaint
    )
  }

  private fun getAngle(
    firstPoint: PoseLandmark?,
    midPoint: PoseLandmark?,
    lastPoint: PoseLandmark?
  ): Double {
    var result = Math.toDegrees(
      atan2(
        1.0 * lastPoint!!.position.y - midPoint!!.position.y,
        1.0 * lastPoint.position.x - midPoint.position.x
      ) - atan2(
        firstPoint!!.position.y - midPoint.position.y,
        firstPoint.position.x - midPoint.position.x
      )
    )
    result = abs(result) // Angle should never be negative
    if (result > 180) {
      result = 360.0 - result // Always get the acute representation of the angle
    }
    return String.format("%.2f", result).toDouble()
  }

  private fun getAngleBasedonXY(
    firstPointx: Float,
    firstPointy: Float,
    midPointx: Float,
    midPointy: Float,
    lastPointx: Float,
    lastPointy: Float
  ): Double {
    var result = Math.toDegrees(
      atan2(
        1.0 * lastPointy - midPointy,
        1.0 * lastPointx - midPointx
      ) - atan2(
        firstPointy - midPointy,
        firstPointx - midPointx
      )
    )
    result = abs(result) // Angle should never be negative
    if (result > 180) {
      result = 360.0 - result // Always get the acute representation of the angle
    }
    return String.format("%.2f", result).toDouble()
  }

  private fun drawPoint(canvas: Canvas, landmark: PoseLandmark, paint: Paint) {
    val point = landmark.position
    canvas.drawCircle(translateX(point.x), translateY(point.y), DOT_RADIUS, paint)
  }

  private fun drawLine(
    canvas: Canvas,
    startLandmark: PoseLandmark,
    endLandmark: PoseLandmark,
    paint: Paint
  ) {
    if (visualizeZ) {
      val start: PointF3D = startLandmark.position3D
      val end: PointF3D = endLandmark.position3D
      val zLowerBoundInScreenPixel: Float
      val zUpperBoundInScreenPixel: Float
      if (rescaleZForVisualization) {
        zLowerBoundInScreenPixel = (-0.001f).coerceAtMost(scale(zMin))
        zUpperBoundInScreenPixel = 0.001f.coerceAtLeast(scale(zMax))
      } else {
        val defaultRangeFactor = 1f
        zLowerBoundInScreenPixel = -defaultRangeFactor * canvas.width
        zUpperBoundInScreenPixel = defaultRangeFactor * canvas.width
      }
      val avgZInImagePixel: Float = (start.z + end.z) / 2
      val zInScreenPixel: Float = scale(avgZInImagePixel)
      if (zInScreenPixel < 0) {
        var v = (zInScreenPixel / zLowerBoundInScreenPixel * 255).toInt()
        v = Ints.constrainToRange(v, 0, 255)
        // paint.setARGB(255, 255, 255 - v, 255 - v)
      } else {
        var v = (zInScreenPixel / zUpperBoundInScreenPixel * 255).toInt()
        v = Ints.constrainToRange(v, 0, 255)
        //paint.setARGB(255, 255 - v, 255 - v, 255)
      }

      canvas.drawLine(
        translateX(start.x),
        translateY(start.y),
        translateX(end.x),
        translateY(end.y),
        paint
      )
    } else {
      val start = startLandmark.position
      val end = endLandmark.position

      canvas.drawLine(
        translateX(start.x),
        translateY(start.y),
        translateX(end.x),
        translateY(end.y),
        paint
      )
    }
  }

//  object PoseVariables{
//    var repcountfi : LiveData<Int> = MutableLiveData<Int>()
//    var repcountFinal : Int by Delegates.observable(0){property, old, newval ->
//      repcountFinal = newval
//    }
//  }

  companion object {
    private const val DOT_RADIUS = 10.0f
    private const val IN_FRAME_LIKELIHOOD_TEXT_SIZE = 50.0f
    private const val STROKE_WIDTH = 8.0f

    const val MIN_AMPLITUDE = 40
    const val REP_THRESHOLD = 0.8
    const val MIN_CONFIDENCE = 0.8
    var count = 0
    var angle25public: Double = 0.0
    private val variable = PoseNetActivity.Myvariables

    //rep logic value
    var repsDone = 0
    var maxAng = 175
    var minAng = 0
    var prev_angle = 0
    var prev_dangle = 0
    var first = true
    var goal = 1
    var setsDone = 0
    var trigger = 0

    //rep logic value 1

    var repsDone1 = 0
    var maxAng1 = 175
    var minAng1 = 0
    var prev_angle1 = 0
    var prev_dangle1 = 0
    var first1 = true
    var goal1 = 1
    var setsDone1 = 0
    var trigger1 = 0

    //rep logic value2

    var repsDone2 = 0
    var maxAng2 = 175
    var minAng2 = 0
    var prev_angle2 = 0
    var prev_dangle2 = 0
    var first2 = true
    var goal2 = 1
    var setsDone2 = 0
    var trigger2 = 0

    //rep logic value3

    var repsDone3 = 0
    var maxAng3 = 175
    var minAng3 = 0
    var prev_angle3 = 0
    var prev_dangle3 = 0
    var first3 = true
    var goal3 = 1
    var setsDone3 = 0
    var trigger3 = 0

    //rep logic value4

    var repsDone4 = 0
    var maxAng4 = 175
    var minAng4 = 0
    var prev_angle4 = 0
    var prev_dangle4 = 0
    var first4 = true
    var goal4 = 1
    var setsDone4 = 0
    var trigger4 = 0

    //rep logic value5

    var repsDone5 = 0
    var maxAng5 = 175
    var minAng5 = 0
    var prev_angle5 = 0
    var prev_dangle5 = 0
    var first5 = true
    var goal5 = 1
    var setsDone5 = 0
    var trigger5 = 0

    //rep logic value6

    var repsDone6 = 0
    var maxAng6 = 175
    var minAng6 = 0
    var prev_angle6 = 0
    var prev_dangle6 = 0
    var first6 = true
    var goal6 = 1
    var setsDone6 = 0
    var trigger6 = 0

    //rep logic value7

    var repsDone7 = 0
    var maxAng7 = 175
    var minAng7 = 0
    var prev_angle7 = 0
    var prev_dangle7 = 0
    var first7 = true
    var goal7 = 1
    var setsDone7 = 0
    var trigger7 = 0

    //rep logic value8

    var repsDone8 = 0
    var maxAng8 = 175
    var minAng8 = 0
    var prev_angle8 = 0
    var prev_dangle8 = 0
    var first8 = true
    var goal8 = 1
    var setsDone8 = 0
    var trigger8 = 0

    //rep logic value9

    var repsDone9 = 0
    var maxAng9 = 175
    var minAng9 = 0
    var prev_angle9 = 0
    var prev_dangle9 = 0
    var first9 = true
    var goal9 = 1
    var setsDone9 = 0
    var trigger9 = 0


    //rep logic value10

    var repsDone10 = 0
    var maxAng10 = 175
    var minAng10 = 0
    var prev_angle10 = 0
    var prev_dangle10 = 0
    var first10 = true
    var goal10 = 1
    var setsDone10 = 0
    var trigger10 = 0

    //rep logic value11

    var repsDone11 = 0
    var maxAng11 = 175
    var minAng11 = 0
    var prev_angle11 = 0
    var prev_dangle11 = 0
    var first11 = true
    var goal11 = 1
    var setsDone11 = 0
    var trigger11 = 0

    //rep logic value12

    var repsDone12 = 0
    var maxAng12 = 175
    var minAng12 = 0
    var prev_angle12 = 0
    var prev_dangle12 = 0
    var first12 = true
    var goal12 = 1
    var setsDone12 = 0
    var trigger12 = 0

    //rep logic value13

    var repsDone13 = 0
    var maxAng13 = 175
    var minAng13 = 0
    var prev_angle13 = 0
    var prev_dangle13 = 0
    var first13 = true
    var goal13 = 1
    var setsDone13 = 0
    var trigger13 = 0

    //flag
    var flagcount = 0

  }

  init {
    whitePaint.strokeWidth = STROKE_WIDTH
    whitePaint.color = Color.RED
    whitePaint.textSize = IN_FRAME_LIKELIHOOD_TEXT_SIZE

    ringPaint = Paint()
    ringPaint.style = Paint.Style.STROKE
    ringPaint.strokeWidth = STROKE_WIDTH
    ringPaint.color = Color.WHITE
    ringPaint.textSize = IN_FRAME_LIKELIHOOD_TEXT_SIZE

    leftPaint = Paint()
    leftPaint.strokeWidth = STROKE_WIDTH
    leftPaint.textSize = IN_FRAME_LIKELIHOOD_TEXT_SIZE
    leftPaint.color = Color.BLUE
  }

  fun replogic0(primaryangle: Int) { // primary Angle logic

    val minAmp = 30
    var crossed = 0
    val thresholdlocal = 0.8
    val dangle = primaryangle - prev_angle
    trigger = 0

    if (!first) {
      Log.d("logTagWork", "Working")
      if (goal == 1 && dangle > 0 && primaryangle - minAng > minAmp * thresholdlocal) {

        var resdonelocal = 0
        repsDone += 1;
        var setcount = setsDone
        Log.d("LogTag8", setsDone.toString())
        val repcount = repsDone

//        PoseVariables.repcountFinal = repcount

//        PoseVariables.repcountfinal = repcount.toString()
//        Log.d("LogTag2", repcount.toString())

        goal = -1;
        crossed = 0;
      } else if (goal == -1 && dangle < 0 && maxAng - primaryangle > minAmp * thresholdlocal) {
        goal = 1
      }
    }

    if (dangle < 0 && prev_dangle >= 0 && prev_angle - minAng > minAmp) {
      maxAng = prev_angle
      trigger = 1
      minAng = maxAng

    } else if (dangle > 0 && prev_dangle <= 0 && maxAng - prev_angle > minAmp) {
      minAng = prev_angle
      trigger = 2
    }

    first = false;
    prev_angle = primaryangle.toInt()
    Log.d("LogTag3", prev_angle.toString())

    if (!first) {
      prev_dangle = dangle.toInt()
      Log.d("LogTag6", prev_dangle.toString())
      updateExcersiseProgress()
    }
    Log.d("LogMinAngle0", minAng.toString())
    Log.d("LogMaxAngle0", maxAng.toString())
  }

  fun replogic1(primaryangle1: Int) { // primary Angle logic

    val minAmp1 = 30
    var crossed1 = 0
    val thresholdlocal1 = 0.8
    val dangle1 = primaryangle1 - prev_angle
    trigger1 = 0

    if (!first1) {
      Log.d("logTagWork", "Working")
      if (goal1 == 1 && dangle1 > 0 && primaryangle1 - minAng1 > minAmp1 * thresholdlocal1) {

        var resdonelocal1 = 0
        repsDone1 += 1;
        var setcount1 = setsDone1
        Log.d("LogTag8", setsDone1.toString())
        val repcount1 = repsDone1

//        PoseVariables.repcountFinal1 = repcount1

//        PoseVariables.repcountfinal = repcount.toString()
//        Log.d("LogTag2", repcount.toString())

        goal1 = -1;
        crossed1 = 0;
      } else if (goal1 == -1 && dangle1 < 0 && maxAng1 - primaryangle1 > minAmp1 * thresholdlocal1) {
        goal1 = 1
      }
    }

    if (dangle1 < 0 && prev_dangle1 >= 0 && prev_angle1 - minAng1 > minAmp1) {
      maxAng1 = prev_angle1
      trigger1 = 1
      minAng1 = maxAng1

    } else if (dangle1 > 0 && prev_dangle1 <= 0 && maxAng1 - prev_angle1 > minAmp1) {
      minAng1 = prev_angle1
      trigger1 = 2
    }

    first1 = false;
    prev_angle1 = primaryangle1.toInt()
    Log.d("LogTag3", prev_angle1.toString())

    if (!first1) {
      prev_dangle1 = dangle1.toInt()
      Log.d("LogTag6", prev_dangle1.toString())
      updateExcersiseProgress()
    }
    Log.d("LogMinAngle1", minAng1.toString())
    Log.d("LogMaxAngle1", maxAng1.toString())
  }

  fun replogic2(primaryangle2: Int) { // primary Angle logic

    val minAmp2 = 30
    var crossed2 = 0
    val thresholdlocal2 = 0.8
    val dangle2 = primaryangle2 - prev_angle2
    trigger2 = 0

    if (!first2) {
      Log.d("logTagWork", "Working")
      if (goal2 == 1 && dangle2 > 0 && primaryangle2 - minAng2 > minAmp2 * thresholdlocal2) {

        var resdonelocal2 = 0
        repsDone2 += 1;
        var setcount = setsDone2
        Log.d("LogTag8", setsDone2.toString())
        val repcount = repsDone2

//        PoseVariables.repcountFinal = repcount

//        PoseVariables.repcountfinal = repcount.toString()
//        Log.d("LogTag2", repcount.toString())

        goal2 = -1;
        crossed2 = 0;
      } else if (goal2 == -1 && dangle2 < 0 && maxAng2 - primaryangle2 > minAmp2 * thresholdlocal2) {
        goal2 = 1
      }
    }

    if (dangle2 < 0 && prev_dangle2 >= 0 && prev_angle2 - minAng2 > minAmp2) {
      maxAng2 = prev_angle2
      trigger2 = 1
      minAng2 = maxAng2

    } else if (dangle2 > 0 && prev_dangle2 <= 0 && maxAng2 - prev_angle2 > minAmp2) {
      minAng2 = prev_angle2
      trigger2 = 2
    }

    first2 = false;
    prev_angle2 = primaryangle2.toInt()
    Log.d("LogTag3", prev_angle2.toString())

    if (!first2) {
      prev_dangle2 = dangle2.toInt()
      Log.d("LogTag6", prev_dangle2.toString())
      updateExcersiseProgress()
    }
    Log.d("LogMinAngle2", minAng2.toString())
    Log.d("LogMaxAngle2", maxAng2.toString())
  }

  fun replogic3(primaryangle3: Int) { // primary Angle logic

    val minAmp3 = 30
    var crossed3 = 0
    val thresholdlocal3 = 0.8
    val dangle3 = primaryangle3 - prev_angle3
    trigger3 = 0

    if (!first3) {
      Log.d("logTagWork", "Working")
      if (goal3 == 1 && dangle3 > 0 && primaryangle3 - minAng3 > minAmp3 * thresholdlocal3) {

        var resdonelocal3 = 0
        repsDone3 += 1;
        var setcount3 = setsDone3
        Log.d("LogTag8", setsDone3.toString())
        val repcount3 = repsDone3

//        PoseVariables.repcountFinal = repcount

//        PoseVariables.repcountfinal = repcount.toString()
//        Log.d("LogTag2", repcount.toString())

        goal3 = -1;
        crossed3 = 0;
      } else if (goal3 == -1 && dangle3 < 0 && maxAng3 - primaryangle3 > minAmp3 * thresholdlocal3) {
        goal3 = 1
      }
    }

    if (dangle3 < 0 && prev_dangle3 >= 0 && prev_angle3 - minAng3 > minAmp3) {
      maxAng3 = prev_angle3
      trigger3 = 1
      minAng3 = maxAng3

    } else if (dangle3 > 0 && prev_dangle3 <= 0 && maxAng3 - prev_angle3 > minAmp3) {
      minAng3 = prev_angle3
      trigger3 = 2
    }

    first3 = false;
    prev_angle3 = primaryangle3.toInt()
    Log.d("LogTag3", prev_angle3.toString())

    if (!first3) {
      prev_dangle3 = dangle3.toInt()
      Log.d("LogTag6", prev_dangle3.toString())
      updateExcersiseProgress()
    }
    Log.d("LogMinAngle3", minAng3.toString())
    Log.d("LogMaxAngle3", maxAng3.toString())
  }

  fun replogic4(primaryangle4: Int) { // primary Angle logic

    val minAmp4 = 30
    var crossed4 = 0
    val thresholdlocal4 = 0.8
    val dangle4 = primaryangle4 - prev_angle4
    trigger4 = 0

    if (!first4) {
      Log.d("logTagWork", "Working")
      if (goal4 == 1 && dangle4 > 0 && primaryangle4 - minAng4 > minAmp4 * thresholdlocal4) {

        var resdonelocal4 = 0
        repsDone4 += 1;
        var setcount4 = setsDone4
        Log.d("LogTag8", setsDone4.toString())
        val repcount4 = repsDone4

//        PoseVariables.repcountFinal = repcount

//        PoseVariables.repcountfinal = repcount.toString()
//        Log.d("LogTag2", repcount.toString())

        goal4 = -1;
        crossed4 = 0;
      } else if (goal4 == -1 && dangle4 < 0 && maxAng4 - primaryangle4 > minAmp4 * thresholdlocal4) {
        goal4 = 1
      }
    }

    if (dangle4 < 0 && prev_dangle4 >= 0 && prev_angle4 - minAng4 > minAmp4) {
      maxAng4 = prev_angle4
      trigger4 = 1
      minAng4 = maxAng4

    } else if (dangle4 > 0 && prev_dangle4 <= 0 && maxAng4 - prev_angle4 > minAmp4) {
      minAng4 = prev_angle4
      trigger4 = 2
    }

    first4 = false;
    prev_angle4 = primaryangle4.toInt()
    Log.d("LogTag3", prev_angle4.toString())

    if (!first4) {
      prev_dangle4 = dangle4.toInt()
      Log.d("LogTag6", prev_dangle4.toString())
      updateExcersiseProgress()
    }
    Log.d("LogMinAngle4", minAng4.toString())
    Log.d("LogMaxAngle4", maxAng4.toString())
  }

  fun replogic5(primaryangle5: Int) { // primary Angle logic

    val minAmp5 = 30
    var crossed5 = 0
    val thresholdlocal5 = 0.8
    val dangle5 = primaryangle5 - prev_angle5
    trigger5 = 0

    if (!first5) {
      Log.d("logTagWork", "Working")
      if (goal5 == 1 && dangle5 > 0 && primaryangle5 - minAng5 > minAmp5 * thresholdlocal5) {

        var resdonelocal5 = 0
        repsDone5 += 1;
        var setcount5 = setsDone5
        Log.d("LogTag8", setsDone5.toString())
        val repcount5 = repsDone5

//        PoseVariables.repcountFinal = repcount

//        PoseVariables.repcountfinal = repcount.toString()
//        Log.d("LogTag2", repcount.toString())

        goal5 = -1;
        crossed5 = 0;
      } else if (goal5 == -1 && dangle5 < 0 && maxAng5 - primaryangle5 > minAmp5 * thresholdlocal5) {
        goal5 = 1
      }
    }

    if (dangle5 < 0 && prev_dangle5 >= 0 && prev_angle5 - minAng5 > minAmp5) {
      maxAng5 = prev_angle5
      trigger5 = 1
      minAng5 = maxAng5

    } else if (dangle5 > 0 && prev_dangle5 <= 0 && maxAng5 - prev_angle5 > minAmp5) {
      minAng5 = prev_angle5
      trigger5 = 2
    }

    first5 = false;
    prev_angle5 = primaryangle5.toInt()
    Log.d("LogTag3", prev_angle5.toString())

    if (!first5) {
      prev_dangle5 = dangle5.toInt()
      Log.d("LogTag6", prev_dangle5.toString())
      updateExcersiseProgress()
    }
    Log.d("LogMinAngle5", minAng5.toString())
    Log.d("LogMaxAngle5", maxAng5.toString())
  }

  fun replogic6(primaryangle6: Int) { // primary Angle logic

    val minAmp6 = 30
    var crossed6 = 0
    val thresholdlocal6 = 0.8
    val dangle6 = primaryangle6 - prev_angle6
    trigger6 = 0

    if (!first6) {
      Log.d("logTagWork", "Working")
      if (goal6 == 1 && dangle6 > 0 && primaryangle6 - minAng6 > minAmp6 * thresholdlocal6) {

        var resdonelocal6 = 0
        repsDone6 += 1;
        var setcount6 = setsDone6
        Log.d("LogTag8", setsDone6.toString())
        val repcount6 = repsDone6

//        PoseVariables.repcountFinal = repcount

//        PoseVariables.repcountfinal = repcount.toString()
//        Log.d("LogTag2", repcount.toString())

        goal6 = -1;
        crossed6 = 0;
      } else if (goal6 == -1 && dangle6 < 0 && maxAng6 - primaryangle6 > minAmp6 * thresholdlocal6) {
        goal6 = 1
      }
    }

    if (dangle6 < 0 && prev_dangle6 >= 0 && prev_angle6 - minAng6 > minAmp6) {
      maxAng6 = prev_angle6
      trigger6 = 1
      minAng6 = maxAng6

    } else if (dangle6 > 0 && prev_dangle6 <= 0 && maxAng6 - prev_angle6 > minAmp6) {
      minAng6 = prev_angle6
      trigger6 = 2
    }

    first6 = false;
    prev_angle6 = primaryangle6.toInt()
    Log.d("LogTag3", prev_angle6.toString())

    if (!first6) {
      prev_dangle5 = dangle6.toInt()
      Log.d("LogTag6", prev_dangle6.toString())
      updateExcersiseProgress()
    }
    Log.d("LogMinAngle6", minAng6.toString())
    Log.d("LogMaxAngle6", maxAng6.toString())
  }

  fun replogic7(primaryangle7: Int) { // primary Angle logic

    val minAmp7 = 30
    var crossed7 = 0
    val thresholdlocal7 = 0.8
    val dangle7 = primaryangle7 - prev_angle7
    trigger7 = 0

    if (!first7) {
      Log.d("logTagWork", "Working")
      if (goal7 == 1 && dangle7 > 0 && primaryangle7 - minAng7 > minAmp7 * thresholdlocal7) {

        var resdonelocal7 = 0
        repsDone7 += 1;
        var setcount7 = setsDone7
        Log.d("LogTag7", setsDone7.toString())
        val repcount7 = repsDone7

//        PoseVariables.repcountFinal = repcount

//        PoseVariables.repcountfinal = repcount.toString()
//        Log.d("LogTag2", repcount.toString())

        goal7 = -1;
        crossed7 = 0;
      } else if (goal7 == -1 && dangle7 < 0 && maxAng7 - primaryangle7 > minAmp7 * thresholdlocal7) {
        goal7 = 1
      }
    }

    if (dangle7 < 0 && prev_dangle7 >= 0 && prev_angle7 - minAng7 > minAmp7) {
      maxAng7 = prev_angle7
      trigger7 = 1
      minAng7 = maxAng7

    } else if (dangle7 > 0 && prev_dangle7 <= 0 && maxAng7 - prev_angle7 > minAmp7) {
      minAng7 = prev_angle7
      trigger7 = 2
    }

    first7 = false;
    prev_angle7 = primaryangle7.toInt()
    Log.d("LogTag3", prev_angle7.toString())

    if (!first7) {
      prev_dangle5 = dangle7.toInt()
      Log.d("LogTag7", prev_dangle7.toString())
      updateExcersiseProgress()
    }
    Log.d("LogMinAngle7", minAng7.toString())
    Log.d("LogMaxAngle7", maxAng7.toString())
  }

  fun replogic8(primaryangle8: Int) { // primary Angle logic

    val minAmp8 = 30
    var crossed8 = 0
    val thresholdlocal8 = 0.8
    val dangle8 = primaryangle8 - prev_angle8
    trigger8 = 0

    if (!first8) {
      Log.d("logTagWork", "Working")
      if (goal8 == 1 && dangle8 > 0 && primaryangle8 - minAng8 > minAmp8 * thresholdlocal8) {

        var resdonelocal8 = 0
        repsDone8 += 1;
        var setcount8 = setsDone8
        Log.d("LogTag8", setsDone8.toString())
        val repcount8 = repsDone8


//        PoseVariables.repcountFinal = repcount

//        PoseVariables.repcountfinal = repcount.toString()
//        Log.d("LogTag2", repcount.toString())

        goal8 = -1;
        crossed8 = 0;
      } else if (goal8 == -1 && dangle8 < 0 && maxAng8 - primaryangle8 > minAmp8 * thresholdlocal8) {
        goal8 = 1
      }
    }

    if (dangle8 < 0 && prev_dangle8 >= 0 && prev_angle8 - minAng8 > minAmp8) {
      maxAng8 = prev_angle8
      trigger8 = 1
      minAng8 = maxAng8

    } else if (dangle8 > 0 && prev_dangle8 <= 0 && maxAng8 - prev_angle8 > minAmp8) {
      minAng8 = prev_angle8
      trigger8 = 2
    }

    first8 = false;
    prev_angle8 = primaryangle8.toInt()
    Log.d("LogTag3", prev_angle8.toString())

    if (!first8) {
      prev_dangle5 = dangle8.toInt()
      Log.d("LogTag8", prev_dangle8.toString())
      updateExcersiseProgress()
    }
    Log.d("LogMinAngle8", minAng8.toString())
    Log.d("LogMaxAngle8", maxAng8.toString())
  }

  fun replogic9(primaryangle9: Int) { // primary Angle logic

    val minAmp9 = 30
    var crossed9 = 0
    val thresholdlocal9 = 0.8
    val dangle9 = primaryangle9 - prev_angle9
    trigger9 = 0

    if (!first9) {
      Log.d("logTagWork", "Working")
      if (goal9 == 1 && dangle9 > 0 && primaryangle9 - minAng9 > minAmp9 * thresholdlocal9) {

        var resdonelocal9 = 0
        repsDone9 += 1;
        var setcount9 = setsDone9
        Log.d("LogTag9", setsDone9.toString())
        val repcount9 = repsDone9

//        PoseVariables.repcountFinal = repcount

//        PoseVariables.repcountfinal = repcount.toString()
//        Log.d("LogTag2", repcount.toString())

        goal9 = -1;
        crossed9 = 0;
      } else if (goal9 == -1 && dangle9 < 0 && maxAng9 - primaryangle9 > minAmp9 * thresholdlocal9) {
        goal9 = 1
      }
    }

    if (dangle9 < 0 && prev_dangle9 >= 0 && prev_angle9 - minAng9 > minAmp9) {
      maxAng9 = prev_angle9
      trigger9 = 1
      minAng9 = maxAng9

    } else if (dangle9 > 0 && prev_dangle9 <= 0 && maxAng9 - prev_angle9 > minAmp9) {
      minAng9 = prev_angle9
      trigger9 = 2
    }

    first9 = false;
    prev_angle9 = primaryangle9.toInt()
    Log.d("LogTag3", prev_angle9.toString())

    if (!first9) {
      prev_dangle5 = dangle9.toInt()
      Log.d("LogTag9", prev_dangle9.toString())
      updateExcersiseProgress()
    }
    Log.d("LogMinAngle9", minAng9.toString())
    Log.d("LogMaxAngle9", maxAng9.toString())
  }

  fun replogic10(primaryangle10: Int) { // primary Angle logic

    val minAmp10 = 30
    var crossed10 = 0
    val thresholdlocal10 = 0.8
    val dangle10 = primaryangle10 - prev_angle10
    trigger10 = 0

    if (!first10) {
      Log.d("logTagWork", "Working")
      if (goal10 == 1 && dangle10 > 0 && primaryangle10 - minAng10 > minAmp10 * thresholdlocal10) {

        var resdonelocal10 = 0
        repsDone10 += 1;
        var setcount10 = setsDone10
        Log.d("LogTag10", setsDone10.toString())
        val repcount10 = repsDone10

//        PoseVariables.repcountFinal = repcount

//        PoseVariables.repcountfinal = repcount.toString()
//        Log.d("LogTag2", repcount.toString())

        goal10 = -1;
        crossed10 = 0;
      } else if (goal10 == -1 && dangle10 < 0 && maxAng10 - primaryangle10 > minAmp10 * thresholdlocal10) {
        goal10 = 1
      }
    }

    if (dangle10 < 0 && prev_dangle10 >= 0 && prev_angle10 - minAng10 > minAmp10) {
      maxAng10 = prev_angle10
      trigger10 = 1
      minAng10 = maxAng10

    } else if (dangle10 > 0 && prev_dangle10 <= 0 && maxAng10 - prev_angle10 > minAmp10) {
      minAng10 = prev_angle10
      trigger10 = 2
    }

    first10 = false;
    prev_angle10 = primaryangle10.toInt()
    Log.d("LogTag3", prev_angle10.toString())

    if (!first10) {
      prev_dangle5 = dangle10.toInt()
      Log.d("LogTag10", prev_dangle10.toString())
      updateExcersiseProgress()
    }
    Log.d("LogMinAngle10", minAng10.toString())
    Log.d("LogMaxAngle10", maxAng10.toString())
  }

  fun replogic11(primaryangle11: Int) { // primary Angle logic

    val minAmp11 = 30
    var crossed11 = 0
    val thresholdlocal11 = 0.8
    val dangle11 = primaryangle11 - prev_angle11
    trigger11 = 0

    if (!first11) {
      Log.d("logTagWork", "Working")
      if (goal11 == 1 && dangle11 > 0 && primaryangle11 - minAng11 > minAmp11 * thresholdlocal11) {

        var resdonelocal11 = 0
        repsDone11 += 1;
        var setcount11 = setsDone11
        Log.d("LogTag11", setsDone11.toString())
        val repcount11 = repsDone11

//        PoseVariables.repcountFinal = repcount

//        PoseVariables.repcountfinal = repcount.toString()
//        Log.d("LogTag2", repcount.toString())

        goal11 = -1;
        crossed11 = 0;
      } else if (goal11 == -1 && dangle11 < 0 && maxAng11 - primaryangle11 > minAmp11 * thresholdlocal11) {
        goal11 = 1
      }
    }

    if (dangle11 < 0 && prev_dangle11 >= 0 && prev_angle11 - minAng11 > minAmp11) {
      maxAng11 = prev_angle11
      trigger11 = 1
      minAng11 = maxAng11

    } else if (dangle11 > 0 && prev_dangle11 <= 0 && maxAng11 - prev_angle11 > minAmp11) {
      minAng11 = prev_angle11
      trigger11 = 2
    }

    first11 = false;
    prev_angle11 = primaryangle11.toInt()
    Log.d("LogTag3", prev_angle11.toString())

    if (!first11) {
      prev_dangle5 = dangle11.toInt()
      Log.d("LogTag11", prev_dangle11.toString())
      updateExcersiseProgress()
    }
    Log.d("LogMinAngle11", minAng11.toString())
    Log.d("LogMaxAngle11", maxAng11.toString())
  }

  fun replogic12(primaryangle12: Int) { // primary Angle logic

    val minAmp12 = 30
    var crossed12 = 0
    val thresholdlocal12 = 0.8
    val dangle12 = primaryangle12 - prev_angle12
    trigger12 = 0

    if (!first12) {
      Log.d("logTagWork", "Working")
      if (goal12 == 1 && dangle12 > 0 && primaryangle12 - minAng12 > minAmp12 * thresholdlocal12) {

        var resdonelocal12 = 0
        repsDone12 += 1;
        var setcount12 = setsDone12
        Log.d("LogTag12", setsDone12.toString())
        val repcount12 = repsDone12

//        PoseVariables.repcountFinal = repcount

//        PoseVariables.repcountfinal = repcount.toString()
//        Log.d("LogTag2", repcount.toString())

        goal12 = -1;
        crossed12 = 0;
      } else if (goal12 == -1 && dangle12 < 0 && maxAng12 - primaryangle12 > minAmp12 * thresholdlocal12) {
        goal12 = 1
      }
    }

    if (dangle12 < 0 && prev_dangle12 >= 0 && prev_angle12 - minAng12 > minAmp12) {
      maxAng12 = prev_angle12
      trigger12 = 1
      minAng12 = maxAng12

    } else if (dangle12 > 0 && prev_dangle12 <= 0 && maxAng12 - prev_angle12 > minAmp12) {
      minAng12 = prev_angle12
      trigger12 = 2
    }

    first12 = false;
    prev_angle12 = primaryangle12.toInt()
    Log.d("LogTag3", prev_angle12.toString())

    if (!first12) {
      prev_dangle5 = dangle12.toInt()
      Log.d("LogTag12", prev_dangle12.toString())
      updateExcersiseProgress()
    }
    Log.d("LogMinAngle12", minAng12.toString())
    Log.d("LogMaxAngle12", maxAng12.toString())
  }

  fun replogic13(primaryangle13: Int) { // primary Angle logic

    val minAmp13 = 30
    var crossed13 = 0
    val thresholdlocal13 = 0.8
    val dangle13 = primaryangle13 - prev_angle13
    trigger13 = 0

    if (!first13) {
      Log.d("logTagWork", "Working")
      if (goal13 == 1 && dangle13 > 0 && primaryangle13 - minAng13 > minAmp13 * thresholdlocal13) {

        var resdonelocal13 = 0
        repsDone13 += 1;
        var setcount13 = setsDone13
        Log.d("LogTag13", setsDone13.toString())
        val repcount13 = repsDone13

//        PoseVariables.repcountFinal = repcount

//        PoseVariables.repcountfinal = repcount.toString()
//        Log.d("LogTag2", repcount.toString())

        goal13 = -1;
        crossed13 = 0;
      } else if (goal13 == -1 && dangle13 < 0 && maxAng13 - primaryangle13 > minAmp13 * thresholdlocal13) {
        goal13 = 1
      }
    }

    if (dangle13 < 0 && prev_dangle13 >= 0 && prev_angle13 - minAng13 > minAmp13) {
      maxAng13 = prev_angle13
      trigger13 = 1
      minAng13 = maxAng13

    } else if (dangle13 > 0 && prev_dangle13 <= 0 && maxAng13 - prev_angle13 > minAmp13) {
      minAng13 = prev_angle13
      trigger13 = 2
    }

    first13 = false;
    prev_angle13 = primaryangle13.toInt()
    Log.d("LogTag3", prev_angle13.toString())

    if (!first13) {
      prev_dangle5 = dangle13.toInt()
      Log.d("LogTag13", prev_dangle13.toString())
      updateExcersiseProgress()
    }
    Log.d("LogMinAngle13", minAng13.toString())
    Log.d("LogMaxAngle13", maxAng13.toString())
  }


  fun updateExcersiseProgress() {
    when {

      variable.angle.equals("lefthip", false) -> {
        calculateRepAndMinMax(repsDone)
      }

      variable.angle.equals("righthip", false) -> {
        calculateRepAndMinMax(repsDone1)
      }
      variable.angle.equals("leftshoulder", false) -> {
        calculateRepAndMinMax(repsDone2)
      }
      variable.angle.equals("rightshoulder", false) -> {
        calculateRepAndMinMax(repsDone3)
      }
      variable.angle.equals("leftknee", false) -> {
        calculateRepAndMinMax(repsDone4)
      }
      variable.angle.equals("rightknee", false) -> {
        calculateRepAndMinMax(repsDone5)
      }
      variable.angle.equals("rightelbow", false) -> {
        calculateRepAndMinMax(repsDone6)
      }
      variable.angle.equals("leftelbow", false) -> {
        calculateRepAndMinMax(repsDone7)
      }
      variable.angle.equals("leftankle", false) -> {
        calculateRepAndMinMax(repsDone8)
      }
      variable.angle.equals("rightangle", false) -> {
        calculateRepAndMinMax(repsDone9)
      }
      variable.angle.equals("leftwrist", false) -> {
        calculateRepAndMinMax(repsDone10)
      }
      variable.angle.equals("rightwrist", false) -> {
        calculateRepAndMinMax(repsDone11)
      }
      variable.angle.equals("leftneck", false) -> {
        calculateRepAndMinMax(repsDone12)
      }
      variable.angle.equals("rightneck", false) -> {
        calculateRepAndMinMax(repsDone13)
      }
    }
  }

  fun calculateRepAndMinMax(repDoneForSelectedExcercise : Int ) {
    if (repDoneForSelectedExcercise == variable.rep?.toInt()!! + 1) { //this come from database
      if (flagcount == 0) {

        flagcount++
        val execer = variable.excercise.toString()
        val pp_cp_id = variable.pp_cp_id.toString()
        val time = variable.time.toString()

        val i = Intent(context, AfterStats::class.java).apply {
          putExtra("exercise", execer)
          putExtra("pp_cp_id", pp_cp_id)
          putExtra("time", time)
          //LeftHip
          putExtra("min", minAng.toString())
          putExtra("max", maxAng.toString())
          //RightHip
          putExtra("min1", minAng1.toString())
          putExtra("max1", maxAng1.toString())
          //leftshoulder
          putExtra("min2", minAng2.toString())
          putExtra("max2", maxAng2.toString())
          //rightshoulder
          putExtra("min3", minAng3.toString())
          putExtra("max3", maxAng3.toString())
          //leftknee
          putExtra("min4", minAng4.toString())
          putExtra("max4", maxAng4.toString())
          //Rightknee
          putExtra("min5", minAng5.toString())
          putExtra("max5", maxAng5.toString())
          //rightelbow
          putExtra("min6", minAng6.toString())
          putExtra("max6", maxAng6.toString())
          //leftelbow
          putExtra("min7", minAng7.toString())
          putExtra("max7", maxAng7.toString())
          //leftankle
          putExtra("min8", minAng8.toString())
          putExtra("max8", maxAng8.toString())
          //rightankle
          putExtra("min9", minAng9.toString())
          putExtra("max9", maxAng9.toString())
          //leftwrist
          putExtra("min10", minAng10.toString())
          putExtra("max10", maxAng10.toString())
          //rightwrist
          putExtra("min11", minAng11.toString())
          putExtra("max11", maxAng11.toString())
          //leftneck
          putExtra("min12", minAng11.toString())
          putExtra("max12", maxAng11.toString())
          //rightneck
          putExtra("min13", minAng11.toString())
          putExtra("max13", maxAng11.toString())
        }
        context.startActivity(i)
      }
    }
  }
}
