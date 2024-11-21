package com.android.fitmoveai.core.model

import com.google.gson.annotations.SerializedName

data class History (

    @SerializedName("workout")
    val workout:String = "",

    @SerializedName("count")
    val count:Int = 0,

    @SerializedName("time")
    val time:String = ""
)