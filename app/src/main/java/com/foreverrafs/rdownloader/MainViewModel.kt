package com.foreverrafs.rdownloader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.extractor.FacebookExtractor

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private var _downloadList: MutableLiveData<List<DownloadInfo>> = MutableLiveData()
    private var _downloadedVideoList: MutableLiveData<List<DownloadInfo>> = MutableLiveData()

    val downloadList: LiveData<List<DownloadInfo>>
        get() = _downloadList

    val downloadedList: LiveData<List<DownloadInfo>>
        get() = _downloadedVideoList

    fun setDownloadList(list: MutableList<DownloadInfo>) {
        _downloadList.value = list
    }

    fun setDownloadedList(list: MutableList<DownloadInfo>) {
        _downloadedVideoList.value = list
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


}