package com.android.fitmoveai.ui.workout.pushup

import android.annotation.SuppressLint
import android.util.Log
import com.android.fitmoveai.core.data.BodyPart
import com.android.fitmoveai.core.data.Person
import com.android.fitmoveai.ui.workout.WorkoutCounter


class PushupCounter : WorkoutCounter() {

    override var MIN_AMPLITUDE = 10  // Adjust for smaller movement sensitivity

    var prev_y_right_wrist = 0
    var prev_dy_right_wrist = 0
    var top_right_wrist = 0
    var bottom_right_wrist = 0
    var prev_y_left_wrist = 0
    var prev_dy_left_wrist = 0
    var top_left_wrist = 0
    var bottom_left_wrist = 0

    @SuppressLint("SuspiciousIndentation")
    override fun countAlgorithm(person: Person): Int {

        Log.d("DumbbellMovementCounter", "Dumbbell Movement Algorithm")

        // Track movement in the wrists and elbows for the dumbbell curl
        if (person.keyPoints[BodyPart.LEFT_WRIST.ordinal].score >= MIN_CONFIDENCE &&
            person.keyPoints[BodyPart.RIGHT_WRIST.ordinal].score >= MIN_CONFIDENCE) {

            var yRightWrist = 1000 - person.keyPoints[BodyPart.RIGHT_WRIST.ordinal].coordinate.y
            var dyRightWrist = yRightWrist - prev_y_right_wrist
            var yLeftWrist = 1000 - person.keyPoints[BodyPart.LEFT_WRIST.ordinal].coordinate.y
            var dyLeftWrist = yLeftWrist - prev_y_left_wrist

            Log.d("Right Wrist", person.keyPoints[BodyPart.RIGHT_WRIST.ordinal].coordinate.y.toString())
            Log.d("Left Wrist", person.keyPoints[BodyPart.LEFT_WRIST.ordinal].coordinate.y.toString())

            if (!first) {
                if (bottom_right_wrist != 0 && top_right_wrist != 0) {
                    if (goal == 1 && dyRightWrist > 0 && (yRightWrist - bottom_right_wrist) > (top_right_wrist - bottom_right_wrist) * REP_THRESHOLD) {

                        if (dyLeftWrist > 0 && (yLeftWrist - bottom_left_wrist) > (top_left_wrist - bottom_left_wrist) * REP_THRESHOLD) {
                            if (top_right_wrist - bottom_right_wrist > MIN_AMPLITUDE && top_left_wrist - bottom_left_wrist > MIN_AMPLITUDE) {
                                count++
                                goal = -1
                            }
                        }
                    }
                    else if (goal == -1 && dyRightWrist < 0 && (top_right_wrist - yRightWrist) > (top_right_wrist - bottom_right_wrist) * REP_THRESHOLD) {
                        if (dyLeftWrist < 0 && (top_left_wrist - yLeftWrist) > (top_left_wrist - bottom_left_wrist) * REP_THRESHOLD) {
                            goal = 1
                        }
                    }
                }

                // Update top and bottom positions for movement detection
                if (dyRightWrist < 0 && prev_dy_right_wrist >= 0 && prev_y_right_wrist - bottom_right_wrist > MIN_AMPLITUDE) {
                    if (dyLeftWrist < 0 && prev_dy_left_wrist >= 0 && prev_y_left_wrist - bottom_left_wrist > MIN_AMPLITUDE) {
                        top_left_wrist = prev_y_left_wrist
                        top_right_wrist = prev_y_right_wrist
                    }
                } else if (dyRightWrist > 0 && prev_dy_right_wrist <= 0 && top_right_wrist - prev_y_right_wrist > MIN_AMPLITUDE) {
                    if (dyLeftWrist > 0 && prev_dy_left_wrist <= 0 && top_left_wrist - prev_y_left_wrist > MIN_AMPLITUDE) {
                        bottom_right_wrist = prev_y_right_wrist
                        bottom_left_wrist = prev_y_left_wrist
                    }
                }
            }

            // Save the current positions for the next frame comparison
            first = false
            prev_y_right_wrist = yRightWrist.toInt()
            prev_dy_right_wrist = dyRightWrist.toInt()
            prev_y_left_wrist = yLeftWrist.toInt()
            prev_dy_left_wrist = dyLeftWrist.toInt()
        }

        return count
    }
}