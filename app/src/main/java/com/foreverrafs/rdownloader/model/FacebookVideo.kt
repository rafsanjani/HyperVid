package com.foreverrafs.rdownloader.model

import android.graphics.Bitmap
import java.time.Duration


data class FacebookVideo(
    val title: String,
    val size: Long,
    val duration: Duration,
    val url: String,
    val coverImage: Bitmap
)