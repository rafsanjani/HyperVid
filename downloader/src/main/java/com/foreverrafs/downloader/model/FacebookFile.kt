package com.foreverrafs.downloader.model

data class FacebookFile(
    var url: String = "",
    var filename: String = "",
    var author: String = "",
    var size: String = "",
    var duration: Long = 0L,
    var ext: String = "mp4"
)