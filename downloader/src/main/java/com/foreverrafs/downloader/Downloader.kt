package com.foreverrafs.downloader

import com.foreverrafs.downloader.model.DownloadInfo

interface Downloader {
    fun downloadFile(downloadInfo: DownloadInfo, listener: DownloadEvents): Int
    fun pauseDownload(downloadId: Int): Boolean
    fun cancelDownload(downloadId: Int): Boolean
}

