package com.foreverrafs.hypervid.data.repository

import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.hypervid.data.dao.DownloadDao
import com.foreverrafs.hypervid.data.dao.VideoDao
import com.foreverrafs.hypervid.model.FBVideo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


/* Created by Rafsanjani on 29/03/2020. */

@Singleton
class AppRepository
@Inject
constructor(
    private val videoDao: VideoDao,
    private val downloadDao: DownloadDao,
    private val dispatcher: CoroutineDispatcher
) : Repository {

    // TODO: 06/06/2021 Wrap this in a Response
    override suspend fun saveVideo(video: FBVideo) = withContext(dispatcher) {
        val saved = videoDao.insert(video)
        Timber.i("Saved video: $saved")
    }

    // TODO: 06/06/2021 Wrap this in a Response
    override suspend fun deleteVideo(video: FBVideo) = withContext(dispatcher) {
        val deleted = videoDao.delete(video)
        Timber.i("Deleted video: $deleted")
    }

    // TODO: 06/06/2021 Wrap this in a Response
    override fun getVideos() = videoDao.getVideos()

    // TODO: 06/06/2021 Wrap this in a Response
    override suspend fun saveDownload(downloadInfo: DownloadInfo) = withContext(dispatcher) {
        val saved = downloadDao.insert(downloadInfo)
        Timber.i("Saved download: $saved")
    }

    // TODO: 06/06/2021 Wrap this in a Response
    override suspend fun deleteDownload(downloadInfo: DownloadInfo) = withContext(dispatcher) {
        val deleted = downloadDao.delete(downloadInfo)
        Timber.i("Deleted download: $deleted")
    }

    // TODO: 06/06/2021 Wrap this in a Response
    override fun getDownloads() = downloadDao.getDownloads()
}