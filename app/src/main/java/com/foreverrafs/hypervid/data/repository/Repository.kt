package com.foreverrafs.hypervid.data.repository

import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.hypervid.model.FBVideo
import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun saveVideo(video: FBVideo)
    suspend fun saveDownload(downloadInfo: DownloadInfo)

    suspend fun deleteVideo(video: FBVideo)
    suspend fun deleteDownload(downloadInfo: DownloadInfo)

    fun getVideos(): Flow<List<FBVideo>>
    fun getDownloads(): Flow<List<DownloadInfo>>
}