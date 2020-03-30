package com.foreverrafs.rdownloader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.extractor.FacebookExtractor
import com.foreverrafs.rdownloader.model.FacebookVideo
import com.foreverrafs.rdownloader.util.fromJson
import com.foreverrafs.rdownloader.util.toJson
import timber.log.Timber

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val preference = PreferenceManager.getDefaultSharedPreferences(app)
    private var _downloadList: MutableLiveData<List<DownloadInfo>> = MutableLiveData(emptyList())
    private var _videosList: MutableLiveData<List<FacebookVideo>> = MutableLiveData(
        emptyList()
    )

    companion object {
        const val PREF_KEY_DOWNLOADS = "download_list"
    }

    val downloadList: LiveData<List<DownloadInfo>>
        get() = _downloadList

    val videosList: LiveData<List<FacebookVideo>>
        get() = _videosList

    fun setDownloadList(list: MutableList<DownloadInfo>) {
        _downloadList.value = list
    }

    fun setVideosList(list: MutableList<FacebookVideo>) {
        _videosList.value = list
    }

    fun extractVideoDownloadUrl(
        streamUrl: String,
        listener: FacebookExtractor.ExtractionEvents
    ) {
        FacebookExtractor().apply {
            addEventsListener(listener)
            extract(streamUrl)
        }
    }

    fun saveDownloadList(downloadList: List<DownloadInfo>) {
        if (downloadList.isNotEmpty()) {
            val json = downloadList.toJson()
            preference.edit().putString(PREF_KEY_DOWNLOADS, json).apply()
            Timber.i("Saved ${downloadList.size} download items")
            return
        } else {
            preference.edit().putString(PREF_KEY_DOWNLOADS, null).apply()
        }

        Timber.i("Download list is empty")
    }

    fun retrieveDownloadList() {
        val json = preference.getString(PREF_KEY_DOWNLOADS, null)
        json?.let { listJson ->
            val list = listJson.fromJson()
            setDownloadList(list.toMutableList())
            Timber.i("${list.size} downloads retrieved")
        } ?: Timber.i("No previous download found")
    }
}