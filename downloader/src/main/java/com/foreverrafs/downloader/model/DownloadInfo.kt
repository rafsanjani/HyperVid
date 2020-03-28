package com.foreverrafs.downloader.model

data class DownloadInfo(
    val url: String,
    val downnloadId: Int,
    val name: String,
    val duration: Long,
    var currentBytes: Long = 0,
    var totalBytes: Long = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val extension: String = "mp4",
    var isCompleted: Boolean = false
)