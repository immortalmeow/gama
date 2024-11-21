package com.android.fitmoveai.core.utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide

fun View.show() {
    this.visibility = View.VISIBLE
}

fun ImageView.loadImageUrl(url: String, requireContext: Context) {
    Glide.with(this.context.applicationContext)
        .load(url)
        .centerCrop()
        .into(this)
}