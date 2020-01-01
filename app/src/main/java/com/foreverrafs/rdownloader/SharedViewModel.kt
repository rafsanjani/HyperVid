package com.foreverrafs.rdownloader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.foreverrafs.downloader.extractor.FacebookExtractor
import com.foreverrafs.rdownloader.adapter.DownloadsAdapter

class SharedViewModel(app: Application) : AndroidViewModel(app) {
    private var _downloadsAdapter: DownloadsAdapter = DownloadsAdapter.getInstance(app)
    private var _downloadListCount = MutableLiveData<Int>()
    private val downloadListCount: LiveData<Int> = _downloadListCount


    fun extractVideoDownloadUrl(
        streamUrl: String,
        listener: FacebookExtractor.ExtractionEventsListenener
    ) {
        val extractor = FacebookExtractor()
        extractor.addExtractionEventsListenener(listener)
        extractor.execute(streamUrl)
    }

    val downloadsAdapter: DownloadsAdapter
        get() = _downloadsAdapter

}