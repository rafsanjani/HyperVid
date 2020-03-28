package com.foreverrafs.rdownloader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.extractor.FacebookExtractor
import com.foreverrafs.rdownloader.model.FacebookVideo

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private var _downloadList: MutableLiveData<List<DownloadInfo>> = MutableLiveData(emptyList())
    private var _videosList: MutableLiveData<List<FacebookVideo>> = MutableLiveData(
        emptyList()
    )

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


}