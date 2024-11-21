package com.android.fitmoveai.ui.camera.squat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.util.Log
import android.view.SurfaceView
import android.widget.Chronometer
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.fitmoveai.R
import com.android.fitmoveai.core.data.Device
import com.android.fitmoveai.core.data.Person
import com.android.fitmoveai.core.movenet.ModelType
import com.android.fitmoveai.core.movenet.MoveNet
import com.android.fitmoveai.databinding.ActivityCameraBinding
import com.android.fitmoveai.ui.camera.CameraSource
import com.android.fitmoveai.ui.workout.WorkoutCounter
import com.android.fitmoveai.ui.workout.dumbell.DumbbellCounter
import com.android.fitmoveai.ui.workout.pushup.PushupCounter
import com.android.fitmoveai.ui.workout.squat.SquatCounter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class RearCameraActivity : AppCompatActivity() {

    private var cameraSource: CameraSource? = null
    private lateinit var surfaceView: SurfaceView
    private var device = Device.CPU
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {

                openCamera()
            } else {
            }
        }

    private lateinit var chronometer: Chronometer
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isTimerRunning = false
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private val workoutName:String = "Squat"
    private var useFrontCamera = false


    override fun getApplicationContext(): Context {
        return super.getApplicationContext()
    }



    lateinit var binding : ActivityCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        surfaceView = binding.surfaceView
        chronometer = binding.chronometer

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Camera"



        if (!isCameraPermissionGranted()) {
            requestPermission()
        }

        binding.btnStartRecord.setOnClickListener {
            if (isTimerRunning) {
                stopTimerAndSaveData()
            } else {
                startTimer()
            }
        }

        binding.btnSwitch.setOnClickListener {
            cameraSource?.close() // Close the current camera session
            cameraSource = null // Ensure the reference is cleared
            startActivity(Intent(this, RearCameraActivity::class.java))
            finish()
        }

    }

    private fun startTimer() {
        startTime = SystemClock.elapsedRealtime()
        isTimerRunning = true

        // Update the button image to indicate "Stop"
        binding.btnStartRecord.setImageResource(R.drawable.btn_stop)

        // Start the chronometer
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (isTimerRunning) {
                    elapsedTime = SystemClock.elapsedRealtime() - startTime
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60

                    // Format the time as MM:SS
                    val formattedTime = String.format("%02d:%02d", minutes, seconds)
                    binding.chronometer.text = formattedTime

                    handler.postDelayed(this, 1000)
                }
            }
        })

        Log.d("CameraActivity", "Timer started")
    }

    private fun stopTimerAndSaveData() {
        isTimerRunning = false

        // Update the button image to indicate "Play"
        binding.btnStartRecord.setImageResource(R.drawable.btn_play)

        saveWorkoutData()
        // Reset the timer
        resetTimer()
    }



    private fun resetTimer() {
        workoutCounter.reset()
        elapsedTime = 0
        binding.chronometer.text = "00:00"
        Log.d("CameraActivity", "Timer reset")
    }

    private fun saveWorkoutData() {
        if (elapsedTime > 0) {
            val workoutCount = CameraActivity.workoutCounter.count
            val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60

            val formattedTime = String.format("%02d:%02d", minutes, seconds)

            val userId = auth.currentUser?.uid ?: return
            val workoutData = hashMapOf(
                "workout" to workoutName,
                "count" to workoutCount,
                "time" to formattedTime
            )

            db.collection("user").document(userId).collection("workouts")
                .add(workoutData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Workout data saved!", Toast.LENGTH_SHORT).show()
                    Log.d("CameraActivity", "Workout data: $workoutData")
                }
                .addOnFailureListener { e ->
                    Log.w("CameraActivity", "Error adding document", e)
                    Toast.makeText(this, "Failed to save workout data.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Timer is still running or not started!", Toast.LENGTH_SHORT).show()
            Log.d("CameraActivity", "Timer is running or not started, time not saved.")
        }
    }

    private var modelPos = 1

    private fun openCamera() {
        if (isCameraPermissionGranted()) {
            if (cameraSource == null) {
                cameraSource = CameraSource(surfaceView, object : CameraSource.CameraSourceListener {
                    override fun onFPSListener(fps: Int) {}
                    override fun onDetectedInfo(
                        personScore: Float?,
                        poseLabels: List<Pair<String, Float>>?
                    ) {}
                }, applicationContext).apply {
                    prepareCamera(useFrontCamera)
                }

                lifecycleScope.launch {
                    cameraSource?.initCamera()
                }
            } else {
                cameraSource?.prepareCamera(useFrontCamera)
                lifecycleScope.launch {
                    cameraSource?.initCamera()
                }
            }
            createPoseEstimator()
        }
        Log.i("openCamera End", "openCamera End")
    }

    private fun isCameraPermissionGranted(): Boolean {
        Log.i("CameraActivity","isCameraPermissionGranted")
        return checkPermission(
            Manifest.permission.CAMERA,
            Process.myPid(),
            Process.myUid()
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        Log.i("CameraActivity : ","requestPermission")
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    private fun createPoseEstimator() {
        Log.i("CameraActivity","createPoseEstimator")

        val poseDetector = MoveNet.create(this, device, ModelType.Thunder)
        poseDetector?.let { detector ->
            cameraSource?.setDetector(detector)
        }
    }

    private fun resetState() {
        // Reset UI components
        binding.chronometer.text = "00:00"
        binding.btnStartRecord.setImageResource(R.drawable.btn_play)

        // Reset timers and counters
        elapsedTime = 0
        isTimerRunning = false
        startTime = 0

        // Reset workout counter
        CameraActivity.workoutCounter.reset()

        // Clear previous detections
        CameraActivity.personForCount.clear()

        Log.d("CameraActivity", "State has been reset")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    override fun onStart() {
        super.onStart()
        openCamera()
    }

    override fun onResume() {
        cameraSource?.resume()
        super.onResume()
    }

    override fun onPause() {
        cameraSource?.close()
        cameraSource = null
        resetState()
        super.onPause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        resetState()
    }

    companion object {
        var personForCount : MutableList<Person> = mutableListOf()

        fun setWorkoutCounter(workout : String)
        {
            Log.d("setWorkoutCounter",workout)
            if (workout == "Squat") {
                workoutCounter = SquatCounter()
            }

            else if (workout == "Pushup") {
                workoutCounter = PushupCounter()
            }

        }
        var workoutCounter : WorkoutCounter = SquatCounter()
    }

}
