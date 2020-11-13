package com.foreverrafs.hypervid.data

import androidx.annotation.WorkerThread
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.hypervid.model.FBVideo
import timber.log.Timber


/* Created by Rafsanjani on 29/03/2020. */

@WorkerThread
class AppRepository(appDb: AppDb) {

    private val videoDao = appDb.videoDao()
    private val downloadDao = appDb.downloadDao()

    fun saveVideo(video: FBVideo) {
        val saved = videoDao.insert(video)
        Timber.i("Saved video: $saved")
    }

    fun deleteVideo(video: FBVideo) {
        val deleted = videoDao.delete(video)
        Timber.i("Deleted video: $deleted")
    }

    fun getVideos() = videoDao.getVideos()

    fun saveDownload(downloadInfo: DownloadInfo) {
        val saved = downloadDao.insert(downloadInfo)
        Timber.i("Saved download: $saved")
    }

    fun deleteDownload(download: DownloadInfo) {
        val deleted = downloadDao.delete(download)
        Timber.i("Deleted download: $deleted")
    }

    fun getDownloads() = downloadDao.getDownloads()
}