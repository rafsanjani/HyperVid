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

    fun saveVideos(videos: List<FBVideo>) {
        val saved = videoDao.insert(videos)
        Timber.i("saved videos: $saved")
    }

    fun deleteVideo(video: FBVideo) {
        val deleted = videoDao.delete(video)
        Timber.i("Deleted video: $deleted")
    }

    fun updateVideo(video: FBVideo) {
        val updated = videoDao.update(video)
        Timber.i("Updated video: $updated")
    }


    fun getVideos() = videoDao.getVideos()


    fun saveDownload(downloadInfo: DownloadInfo) {
        val saved = downloadDao.insert(downloadInfo)
        Timber.i("Saved download: $saved")
    }

    fun saveDownload(downloads: List<DownloadInfo>) {
        val saved = downloadDao.insert(downloads)
        Timber.i("saved downloads: $saved")
    }

    fun deleteDownload(download: DownloadInfo) {
        val deleted = downloadDao.delete(download)
        Timber.i("Deleted download: $deleted")
    }

    fun updateDownload(download: DownloadInfo) {
        val updated = downloadDao.update(download)
        Timber.i("Updated download: $updated")
    }

    fun getDownloads() = downloadDao.getDownloads()
}