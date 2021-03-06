package com.foreverrafs.hypervid.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.extractor.ExtractionException
import com.foreverrafs.extractor.Extractor
import com.foreverrafs.extractor.VideoExtractor
import com.foreverrafs.hypervid.analytics.Analytics
import com.foreverrafs.hypervid.analytics.events.ExtractVideoEvent
import com.foreverrafs.hypervid.data.repository.Repository
import com.foreverrafs.hypervid.model.FBVideo
import com.foreverrafs.hypervid.ui.states.DownloadListState
import com.foreverrafs.hypervid.ui.states.VideoListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val repository: Repository,
    private val preference: SharedPreferences,
    private val analytics: Analytics,
) : ViewModel() {

    val videosListState: Flow<VideoListState> = repository.getVideos().map { videos ->
        if (videos.isEmpty()) {
            VideoListState.Empty
        } else {
            VideoListState.Videos(videos = videos)
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 2)

    val downloadState: Flow<DownloadListState> = repository.getDownloads().map { downloads ->
        DownloadListState.DownloadList(downloads = downloads)
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 2)

    companion object {
        const val PREF_KEY_FIRSTRUN = "firstrun"
    }

    fun saveVideo(video: FBVideo) = viewModelScope.launch {
        repository.saveVideo(video)
    }

    fun deleteVideo(video: FBVideo) = viewModelScope.launch {
        val deleted = repository.deleteVideo(video)

        if (deleted >= 1) {
            if (File(video.path).absoluteFile.delete()) {
                Timber.d("deleteVideo: Delete status: $deleted")
            } else {
                Timber.e("Error deleting file")
            }
        }
    }

    fun saveDownload(download: DownloadInfo) = viewModelScope.launch {
        repository.saveDownload(download)
    }

    fun deleteDownload(download: DownloadInfo) = viewModelScope.launch {
        repository.deleteDownload(download)
    }

    fun extractVideoDownloadUrl(
        videoUrl: String,
        listener: Extractor.ExtractionEvents
    ): Job {
        val extractor = VideoExtractor()

        return viewModelScope.launch {
            try {
                val downloadable = extractor.extractVideoUrl(videoUrl)

                listener.onComplete(
                    downloadable
                )

                analytics.trackEvent(
                    ExtractVideoEvent(
                        title = downloadable.filename,
                        url = videoUrl,
                        successful = true
                    )
                )
            } catch (e: UnknownHostException) {
                listener.onError(e)
            } catch (e: ExtractionException) {
                listener.onError(e)

                analytics.trackEvent(
                    ExtractVideoEvent(
                        title = "",
                        url = videoUrl,
                        successful = false,
                    )
                )
            }
        }
    }

    var isFirstRun: Boolean
        get() = preference.getBoolean(PREF_KEY_FIRSTRUN, true)
        set(value) = preference.edit().putBoolean(PREF_KEY_FIRSTRUN, value).apply()
}
