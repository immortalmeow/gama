package com.android.fitmoveai.core.data

import android.graphics.RectF
import com.android.fitmoveai.core.data.KeyPoint

data class Person(
    var id: Int = -1,
    val keyPoints: List<KeyPoint>,
    val boundingBox: RectF? = null,
    val score: Float
)
