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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
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


    companion object {
        const val PREF_KEY_FIRSTRUN = "firstrun"
    }

    fun saveVideo(video: FBVideo) = viewModelScope.launch {
        repository.saveVideo(video)
    }

    fun deleteVideo(video: FBVideo) = viewModelScope.launch {
        repository.deleteVideo(video)
    }


    fun saveDownload(download: DownloadInfo) = viewModelScope.launch {
        repository.saveDownload(download)
    }

    fun deleteDownload(download: DownloadInfo) = viewModelScope.launch {
        repository.deleteDownload(download)
    }

    val downloadList: Flow<List<DownloadInfo>> = repository.getDownloads()

    val videosList: Flow<List<FBVideo>> = repository.getVideos()

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