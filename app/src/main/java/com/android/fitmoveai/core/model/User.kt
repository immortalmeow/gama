package com.android.fitmoveai.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val firstName:String? = null,
    val lastName:String? = null,
    val email:String? = null,
    val gender: String? = null,
    val usia: Int? = null,
    val tinggi: Int? = null,
    val berat:Int? = null,
    val fotoProfil:String? = null,
): Parcelable