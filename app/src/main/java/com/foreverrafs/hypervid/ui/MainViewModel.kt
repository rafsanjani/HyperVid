package com.foreverrafs.hypervid.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.extractor.Downloadable
import com.foreverrafs.extractor.Extractor
import com.foreverrafs.extractor.VideoExtractor
import com.foreverrafs.hypervid.data.repository.Repository
import com.foreverrafs.hypervid.model.FBVideo
import com.foreverrafs.hypervid.ui.states.DownloadListState
import com.foreverrafs.hypervid.ui.states.VideoListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject


@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val repository: Repository,
    private val preference: SharedPreferences
) : ViewModel() {

    private val videos = mutableListOf<FBVideo>()
    private val downloads = mutableListOf<DownloadInfo>()

    val downloadList: Flow<List<DownloadInfo>> = repository.getDownloads()

    val videosListState: Flow<VideoListState> = repository.getVideos().map { videos ->
        if (videos.isEmpty())
            VideoListState.Error(Exception("Error Loading videos"))
        else
            VideoListState.Videos(videos = videos)
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    val downloadState: Flow<DownloadListState> = repository.getDownloads().map { downloads ->
        if (downloads.isEmpty())
            DownloadListState.Error(Exception("Error loading downloads"))
        else
            DownloadListState.DownloadList(downloads = downloads)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), DownloadListState.Loading)


    companion object {
        const val PREF_KEY_FIRSTRUN = "firstrun"
    }

    fun saveVideo(video: FBVideo) = viewModelScope.launch {
        repository.saveVideo(video)
    }

    fun deleteVideo(video: FBVideo) = viewModelScope.launch {
        val deleted = repository.deleteVideo(video)

        if (deleted >= 1) {
            if (File(video.path).absoluteFile.delete())
                Timber.d("deleteVideo: Delete status: $deleted")
            else
                Timber.e("Error deleting file")
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
                    Downloadable(
                        url = downloadable.url,
                        filename = downloadable.filename,
                    )
                )
            } catch (e: Exception) {
                listener.onError(e)
            }
        }
    }

    //Check if a video with specified url has been downloaded already
    fun videoExists(url: String) = videos.any { video -> video.url == url }

    //Check if a download already exists
    fun downloadExists(url: String) = downloads.any { download -> download.url == url }

    var isFirstRun: Boolean
        get() = preference.getBoolean(PREF_KEY_FIRSTRUN, true)
        set(value) = preference.edit().putBoolean(PREF_KEY_FIRSTRUN, value).apply()
}