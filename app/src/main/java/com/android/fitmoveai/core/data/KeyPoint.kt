package com.android.fitmoveai.core.data

import android.graphics.PointF
import com.android.fitmoveai.core.data.BodyPart

data class KeyPoint(val bodyPart: BodyPart, var coordinate: PointF, val score: Float)