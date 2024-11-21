package com.android.fitmoveai.core.movenet

import android.graphics.Bitmap
import com.android.fitmoveai.core.data.Person


interface PoseDetector : AutoCloseable {

    fun estimatePoses(bitmap: Bitmap): List<Person>

    fun lastInferenceTimeNanos(): Long
}
