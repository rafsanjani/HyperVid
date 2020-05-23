package com.foreverrafs.hypervid.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.extractor.FacebookExtractor
import com.foreverrafs.hypervid.data.AppDb
import com.foreverrafs.hypervid.data.AppRepository
import com.foreverrafs.hypervid.model.FBVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val preference = PreferenceManager.getDefaultSharedPreferences(app)
    private val dispatcher = Dispatchers.IO

    private val database = Room
        .databaseBuilder(app, AppDb::class.java, "hypervid.db")
        .fallbackToDestructiveMigration()
        .build()

    private val repository = AppRepository(database)

    companion object {
        const val PREF_KEY_FIRSTRUN = "firstrun"
    }

    fun saveVideo(list: List<FBVideo>) = viewModelScope.launch(dispatcher) {
        repository.saveVideos(list)
    }

    fun saveVideo(video: FBVideo) = viewModelScope.launch(dispatcher) {
        repository.saveVideo(video)
    }

    fun deleteVideo(video: FBVideo) = viewModelScope.launch(dispatcher) {
        repository.deleteVideo(video)
    }

    fun updateVideo(video: FBVideo) = viewModelScope.launch(dispatcher) {
        repository.updateVideo(video)
    }


    fun saveDownload(list: List<DownloadInfo>) = viewModelScope.launch(dispatcher) {
        repository.saveDownload(list)
    }

    fun saveDownload(download: DownloadInfo) = viewModelScope.launch(dispatcher) {
        repository.saveDownload(download)
    }

    fun deleteDownload(download: DownloadInfo) = viewModelScope.launch(dispatcher) {
        repository.deleteDownload(download)
    }

    fun updateDownload(download: DownloadInfo) = viewModelScope.launch(dispatcher) {
        repository.updateDownload(download)
    }

    val downloadList: LiveData<List<DownloadInfo>>
        get() = repository.getDownloads()

    val videosList: LiveData<List<FBVideo>>
        get() = repository.getVideos()

    fun extractVideoDownloadUrl(
        streamUrl: String,
        listener: FacebookExtractor.ExtractionEvents
    ): Job {
        val extractor = FacebookExtractor()
        extractor.addEventsListener(listener)

        return viewModelScope.launch {
            extractor.extract(streamUrl)
        }
    }

    //Check if a video with specified url has been downloaded already
    fun hasVideo(url: String) = videosList.value?.any { video -> video.url == url } ?: false

    //Check if a download already exists
    fun hasDownload(url: String) =
        downloadList.value?.any { download -> download.url == url } ?: false

    var isFirstRun: Boolean
        get() = preference.getBoolean(PREF_KEY_FIRSTRUN, true)
        set(value) = preference.edit().putBoolean(PREF_KEY_FIRSTRUN, value).apply()
}