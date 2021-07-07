package com.foreverrafs.downloader.downloader

import com.foreverrafs.downloader.model.DownloadInfo

interface Downloader {
    fun downloadFile(downloadInfo: DownloadInfo, videoDownloadListener: DownloadEvents): Int
    fun pauseDownload(downloadId: Int): Boolean
    fun cancelDownload(downloadId: Int): Boolean
}
