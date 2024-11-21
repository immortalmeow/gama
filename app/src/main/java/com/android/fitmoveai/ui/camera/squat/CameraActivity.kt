package com.android.fitmoveai.ui.camera.squat


import android.Manifest
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
import com.android.fitmoveai.ui.MainActivity
import com.android.fitmoveai.ui.camera.CameraSource
import com.android.fitmoveai.ui.workout.WorkoutCounter
import com.android.fitmoveai.ui.workout.pushup.PushupCounter
import com.android.fitmoveai.ui.workout.squat.SquatCounter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var cameraSource: CameraSource? = null
    private lateinit var surfaceView: SurfaceView

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            }
        }

    private lateinit var chronometer: Chronometer
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isTimerRunning = false
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private val workoutName: String = "Squat"
    private var useFrontCamera = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        surfaceView = binding.surfaceView
        chronometer = binding.chronometer

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Camera"
        }

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

        binding.btnStartRecord.setImageResource(R.drawable.btn_stop)

        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (isTimerRunning) {
                    elapsedTime = SystemClock.elapsedRealtime() - startTime
                    val formattedTime = String.format(
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
                        TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
                    )
                    binding.chronometer.text = formattedTime

                    handler.postDelayed(this, 1000)
                }
            }
        })
        Log.d("CameraActivity", "Timer started")
    }

    private fun stopTimerAndSaveData() {
        isTimerRunning = false
        binding.btnStartRecord.setImageResource(R.drawable.btn_play)
        saveWorkoutData()
        resetTimer()
    }

    private fun resetTimer() {
        elapsedTime = 0
        workoutCounter.reset()
        binding.chronometer.text = "00:00"
        Log.d("CameraActivity", "Timer reset")
    }

    private fun saveWorkoutData() {
        if (elapsedTime > 0) {
            val workoutCount = workoutCounter.count
            val formattedTime = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
                TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
            )

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

    private fun openCamera() {
        if (isCameraPermissionGranted()) {
            cameraSource?.let {
                it.prepareCamera(useFrontCamera)
                lifecycleScope.launch {
                    it.initCamera()
                }
            } ?: run {
                cameraSource = CameraSource(surfaceView, object : CameraSource.CameraSourceListener {
                    override fun onFPSListener(fps: Int) {}
                    override fun onDetectedInfo(personScore: Float?, poseLabels: List<Pair<String, Float>>?) {}
                }, applicationContext).apply {
                    prepareCamera(useFrontCamera)
                    lifecycleScope.launch {
                        initCamera()
                    }
                }
            }
            createPoseEstimator()
        }
    }

    private fun restartCamera() {
        // Close current camera session
        cameraSource?.close()

        // Reset the state like the first time opening the activity
        resetState()

        // Reinitialize the camera session with the selected lens
        openCamera()
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
        workoutCounter.reset()

        // Clear previous detections
        personForCount.clear()

        Log.d("CameraActivity", "State has been reset")
    }

    private fun isCameraPermissionGranted(): Boolean {
        return checkPermission(
            Manifest.permission.CAMERA,
            Process.myPid(),
            Process.myUid()
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun createPoseEstimator() {
        val poseDetector = MoveNet.create(this, Device.CPU, ModelType.Thunder)
        poseDetector?.let { detector ->
            cameraSource?.setDetector(detector)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onStart() {
        super.onStart()
        openCamera()
    }

    override fun onPause() {
        super.onPause()
        cameraSource?.close()
        resetState() // Reset the state when the activity is paused
    }

    override fun onResume() {
        super.onResume()
        if (cameraSource == null) {
            openCamera()
        } else {
            cameraSource?.resume()
        }
    }

    override fun onBackPressed() {
        cameraSource?.close()
        resetState() // Reset the state when navigating back
        super.onBackPressed()
    }

    companion object {
        var personForCount: MutableList<Person> = mutableListOf()

        fun setWorkoutCounter(workout: String) {
            workoutCounter = when (workout) {
                "Squat" -> SquatCounter()
                "Pushup" -> PushupCounter()
                else -> SquatCounter() // Default fallback
            }
        }

        var workoutCounter: WorkoutCounter = SquatCounter()
    }
}

