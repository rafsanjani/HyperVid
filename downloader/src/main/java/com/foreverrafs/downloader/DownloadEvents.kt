package com.foreverrafs.downloader

interface DownloadEvents {
    fun onDownloadProgressChanged(currentBytes: Long, totalBytes: Long)
    fun onDownloadPaused()
    fun onDownloadCompleted()
    fun onDownloadError(error: DownloadException)
    fun onDownloadCancelled()
    fun onDownloadStart()
}