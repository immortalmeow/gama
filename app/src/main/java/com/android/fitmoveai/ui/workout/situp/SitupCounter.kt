package com.android.fitmoveai.ui.workout.situp

import com.android.fitmoveai.core.data.BodyPart
import com.android.fitmoveai.core.data.Person
import com.android.fitmoveai.ui.workout.WorkoutCounter

class SitupCounter:WorkoutCounter (){

    override var MIN_AMPLITUDE = 20  // Adjust for sit-up movement sensitivity

    var prev_y_chest = 0
    var prev_dy_chest = 0
    var top_chest = 0
    var bottom_chest = 0

    override fun countAlgorithm(person: Person): Int {
        // Check if the torso part (chest or nose) is detected with sufficient confidence
        if (person.keyPoints[BodyPart.NOSE.ordinal].score >= MIN_CONFIDENCE) {
            // Get the y-coordinate of the chest or nose (flipped for consistency)
            var yChest = 1000 - person.keyPoints[BodyPart.NOSE.ordinal].coordinate.y
            var dyChest = yChest - prev_y_chest

            // Ensure this is not the first frame
            if (!first) {
                // Check if top and bottom positions are already set
                if (bottom_chest != 0 && top_chest != 0) {
                    // Detect if the person is sitting up (dy > 0) and passing the threshold
                    if (goal == 1 && dyChest > 0 && (yChest - bottom_chest) > (top_chest - bottom_chest) * REP_THRESHOLD) {
                        // Ensure the amplitude is significant enough to count a sit-up
                        if (top_chest - bottom_chest > MIN_AMPLITUDE) {
                            count++  // Increment the sit-up count
                            goal = -1  // Set goal to downward motion
                        }
                    }
                    // Detect if the person is lying back down (dy < 0)
                    else if (goal == -1 && dyChest < 0 && (top_chest - yChest) > (top_chest - bottom_chest) * REP_THRESHOLD) {
                        goal = 1  // Set goal to upward motion
                    }
                }

                // Update top and bottom positions based on the movement direction
                if (dyChest < 0 && prev_dy_chest >= 0 && prev_y_chest - bottom_chest > MIN_AMPLITUDE) {
                    top_chest = prev_y_chest  // Set the top position when going down
                } else if (dyChest > 0 && prev_dy_chest <= 0 && top_chest - prev_y_chest > MIN_AMPLITUDE) {
                    bottom_chest = prev_y_chest  // Set the bottom position when sitting up
                }
            }

            // Save the current position for the next frame comparison
            first = false
            prev_y_chest = yChest.toInt()
            prev_dy_chest = dyChest.toInt()
        }

        return count  // Return the current sit-up count
    }
}