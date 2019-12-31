package com.foreverrafs.downloader.model

import android.graphics.Bitmap
import org.joda.time.DateTime

data class DownloadInfo(
    val url: String,
    val downnloadId: Int,
    val name: String,
    val duration: Long,
    var image: Bitmap,
    var currentBytes: Long = 0,
    var totalBytes: Long = 0,
    val dateAdded: DateTime,
    val extension: String = "mp4"
)