package com.foreverrafs.rdownloader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.foreverrafs.downloader.VideoDownloader
import com.foreverrafs.downloader.extractor.FacebookExtractor
import com.foreverrafs.downloader.model.DownloadInfo

class MainViewModel(private val app: Application) : AndroidViewModel(app) {

    private val videoDownloader = VideoDownloader.getInstance(app)

    private var _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> = _progress

    private var _downloads = MutableLiveData<ArrayList<DownloadInfo>>()
    val downloads: LiveData<ArrayList<DownloadInfo>> = _downloads


    fun extractVideoDownloadUrl(
        streamUrl: String,
        listener: FacebookExtractor.ExtractionEventsListenener
    ) {
        val extractor = FacebookExtractor()
        extractor.addExtractionEventsListenener(listener)
        extractor.execute(streamUrl)
    }
}