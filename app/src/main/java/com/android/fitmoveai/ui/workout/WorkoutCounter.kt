package com.android.fitmoveai.ui.workout

import com.android.fitmoveai.core.data.Person




abstract class WorkoutCounter() {

    open var MIN_AMPLITUDE = 40
    open var REP_THRESHOLD = 0.8
    open var MIN_CONFIDENCE = 0.5

    var count = 0

    var first = true
    var goal = 1
    var prev_y = 0
    var prev_dy = 0
    var top = 0
    var bottom = 0

    abstract fun countAlgorithm(person : Person) : Int

    fun reset()
    {
        count = 0
    }
}