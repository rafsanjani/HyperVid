package com.foreverrafs.downloader.downloader

interface DownloadEvents {
    fun onProgressChanged(downloaded: Long, percentage: Int)
    fun onPause()
    fun onCompleted()
    fun onError(error: DownloadException)
    fun onCancelled()
    fun onWaitingForNetwork()
    fun onStart()
}