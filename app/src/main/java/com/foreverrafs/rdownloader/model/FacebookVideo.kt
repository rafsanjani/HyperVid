package com.foreverrafs.rdownloader.model

import android.graphics.Bitmap


data class FacebookVideo(
    val title: String,
    val duration: Long,
    val path: String,
    val coverImage: Bitmap
)