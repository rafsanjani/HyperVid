package com.foreverrafs.rdownloader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.foreverrafs.downloader.extractor.FacebookExtractor
import com.foreverrafs.rdownloader.adapter.DownloadsAdapter
import com.foreverrafs.rdownloader.adapter.VideosAdapter

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private var _downloadsAdapter: DownloadsAdapter = DownloadsAdapter(app)
    private var _videosAdapter: VideosAdapter = VideosAdapter(app)

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

    val videosAdapter: VideosAdapter
        get() = _videosAdapter

}