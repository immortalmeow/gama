package com.android.fitmoveai.ui.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.text.TextPaint
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import com.android.fitmoveai.core.data.Person
import com.android.fitmoveai.core.movenet.PoseClassifier
import com.android.fitmoveai.core.movenet.PoseDetector
import com.android.fitmoveai.core.visualization.VisualizationUtils
import com.android.fitmoveai.core.visualization.YuvToRgbConverter
import com.android.fitmoveai.ui.camera.squat.CameraActivity

import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraSource (
    private val surfaceView: SurfaceView,
    private var listener: CameraSourceListener? = null,
    val context: Context
) {

    companion object {
        private const val PREVIEW_WIDTH = 640
        private const val PREVIEW_HEIGHT = 480
        private const val MIN_CONFIDENCE = .2f
        private const val TAG = "Camera Source"
    }

    private val lock = Any()
    private var detector: PoseDetector? = null
    private var classifier: PoseClassifier? = null
    private var isTrackerEnabled = false
    private var yuvConverter: YuvToRgbConverter = YuvToRgbConverter(surfaceView.context)
    private lateinit var imageBitmap: Bitmap

    private var fpsTimer: Timer? = null
    private var frameProcessedInOneSecondInterval = 0
    private var framesPerSecond = 0

    private val cameraManager: CameraManager by lazy {
        val context = surfaceView.context
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private var imageReader: ImageReader? = null

    private var camera: CameraDevice? = null

    private var session: CameraCaptureSession? = null

    private var imageReaderThread: HandlerThread? = null

    private var imageReaderHandler: Handler? = null
    private var cameraId: String = ""

    private var useFrontCamera = true

    suspend fun initCamera() {

        Log.i("CameraSource","initCamera")

        camera = openCamera(cameraManager, cameraId)

        imageReader =
            ImageReader.newInstance(PREVIEW_WIDTH, PREVIEW_HEIGHT, ImageFormat.YUV_420_888, 3)

        imageReader?.setOnImageAvailableListener({ reader ->
            Log.d("CameraSource", "setOnImageAvailableListener")

            val image = reader.acquireLatestImage()
            if (!::imageBitmap.isInitialized) {
                imageBitmap = Bitmap.createBitmap(
                    PREVIEW_WIDTH,
                    PREVIEW_HEIGHT,
                    Bitmap.Config.ARGB_8888
                )
            }

            if (image != null) {
                yuvConverter.yuvToRgb(image, imageBitmap)

                // Get the sensor orientation
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0

                // Adjust the rotation and flipping based on the camera used
                val rotateMatrix = Matrix()
                if (useFrontCamera) {
                    rotateMatrix.postRotate(sensorOrientation.toFloat())
                    rotateMatrix.postScale(-1f, 1f) // Flip horizontally
                } else {
                    rotateMatrix.postRotate(sensorOrientation.toFloat())
                }

                // Apply the matrix transformation to the image
                val adjustedBitmap = Bitmap.createBitmap(
                    imageBitmap, 0, 0, imageBitmap.width, imageBitmap.height,
                    rotateMatrix, true
                )

                // Process the adjusted image
                processImage(adjustedBitmap)
                image.close()
            }
        }, imageReaderHandler)

        imageReader?.surface?.let { surface ->
            session = createSession(listOf(surface))
            val cameraRequest = camera?.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )?.apply {
                addTarget(surface)
            }
            cameraRequest?.build()?.let {
                session?.setRepeatingRequest(it, null, null)
            }
        }
        Log.d("CameraSource", "ImageAvailableListener End")

        Log.d("CameraSource","initCamera End")
    }

    private suspend fun createSession(targets: List<Surface>): CameraCaptureSession =
        suspendCancellableCoroutine { cont ->
            Log.d("CameraSource","createSession")
            camera?.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(captureSession: CameraCaptureSession) =
                    cont.resume(captureSession)

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    cont.resumeWithException(Exception("Session error"))
                }
            }, null)
            Log.d("CameraSource","createSession End")
        }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(manager: CameraManager, cameraId: String): CameraDevice =
        suspendCancellableCoroutine { cont ->
            Log.d("CameraSource","openCamera")
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) = cont.resume(camera)

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    if (cont.isActive) cont.resumeWithException(Exception("Camera error"))
                }
            }, imageReaderHandler)
            Log.d("CameraSource","openCamera End")
        }

    fun prepareCamera(useFrontCamera: Boolean) {
        Log.d("CameraSource", "prepareCamera")
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)

            val cameraDirection = if (useFrontCamera) {
                CameraCharacteristics.LENS_FACING_FRONT
            } else {
                CameraCharacteristics.LENS_FACING_BACK
            }

            if (cameraDirection == characteristics.get(CameraCharacteristics.LENS_FACING)) {
                this.cameraId = cameraId
                break
            }
        }
        Log.d("CameraSource", "prepareCamera End")
    }

    fun setDetector(detector: PoseDetector) {
        Log.d("CameraSource","setDetector")
        synchronized(lock) {
            if (this.detector != null) {
                this.detector?.close()
                this.detector = null
                Log.i("aa", CameraActivity.personForCount[1].keyPoints[0].coordinate.x.toString())
            }
            this.detector = detector
        }
    }

    fun setClassifier(classifier: PoseClassifier?) {
        synchronized(lock) {
            if (this.classifier != null) {
                this.classifier?.close()
                this.classifier = null
            }
            this.classifier = classifier
        }
    }

    fun resume() {
        imageReaderThread = HandlerThread("imageReaderThread").apply { start() }
        imageReaderHandler = Handler(imageReaderThread!!.looper)
        fpsTimer = Timer()
        fpsTimer?.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    framesPerSecond = frameProcessedInOneSecondInterval
                    frameProcessedInOneSecondInterval = 0
                }
            },
            0,
            1000
        )
    }

    fun close() {
        session?.close()
        session = null
        camera?.close()
        camera = null
        imageReader?.close()
        imageReader = null
        stopImageReaderThread()
        detector?.close()
        detector = null
        classifier?.close()
        classifier = null
        fpsTimer?.cancel()
        fpsTimer = null
        frameProcessedInOneSecondInterval = 0
        framesPerSecond = 0
    }

    private fun processImage(bitmap: Bitmap) {
        CameraActivity.personForCount = mutableListOf<Person>()

        val persons = mutableListOf<Person>()
        var classificationResult: List<Pair<String, Float>>? = null

        synchronized(lock) {
            detector?.estimatePoses(bitmap)?.let {

                persons.addAll(it)

            }
        }

        if (persons.isNotEmpty()) {
            CameraActivity.personForCount = persons
            CameraActivity.workoutCounter.countAlgorithm(persons[0])
        }

        visualize(persons, bitmap)
    }

    private fun visualize(persons: List<Person>, bitmap: Bitmap) {

        val outputBitmap = VisualizationUtils.drawBodyKeypoints(
            bitmap,
            persons.filter { it.score > MIN_CONFIDENCE }, isTrackerEnabled
        )

        val holder = surfaceView.holder

        val surfaceCanvas = holder.lockCanvas()
        surfaceCanvas?.let { canvas ->
            val screenWidth: Int
            val screenHeight: Int
            val left: Int
            val top: Int

            if (canvas.height > canvas.width) {
                val ratio = outputBitmap.height.toFloat() / outputBitmap.width
                screenWidth = canvas.width
                left = 0
                screenHeight = (canvas.width * ratio).toInt()
                top = (canvas.height - screenHeight) / 2
            } else {
                val ratio = outputBitmap.width.toFloat() / outputBitmap.height
                screenHeight = canvas.height
                top = 0
                screenWidth = (canvas.height * ratio).toInt()
                left = (canvas.width - screenWidth) / 2
            }
            val right: Int = left + screenWidth
            val bottom: Int = top + screenHeight

            canvas.drawBitmap(
                outputBitmap, Rect(0, 0, outputBitmap.width, outputBitmap.height),
                Rect(left, top, right, bottom), null
            )

            val textPaint = TextPaint()

            textPaint.textSize = 80F
            textPaint.color = Color.BLUE
            textPaint.strokeWidth = 10.toFloat()
            textPaint.isAntiAlias = true

            val xPos = (canvas.width / 20).toFloat()
            val yPos = (bottom - canvas.height / 30).toFloat()
            canvas.drawText("Count : " + CameraActivity.workoutCounter.count.toString(),
                xPos,
                yPos,
                textPaint)

            surfaceView.holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun stopImageReaderThread() {
        imageReaderThread?.quitSafely()
        try {
            imageReaderThread?.join()
            imageReaderThread = null
            imageReaderHandler = null
        } catch (e: InterruptedException) {
            Log.d(TAG, e.message.toString())
        }
    }

    interface CameraSourceListener {
        fun onFPSListener(fps: Int)

        fun onDetectedInfo(personScore: Float?, poseLabels: List<Pair<String, Float>>?)
    }
}