package com.foreverrafs.extractor

data class DownloadableFile(
    var url: String = "",
    var filename: String = "",
    var author: String = "",
    var size: String = "",
    var duration: Long = 0L,
    var ext: String = "mp4"
)