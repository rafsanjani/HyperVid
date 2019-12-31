package com.foreverrafs.rdownloader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.foreverrafs.downloader.DownloadEvents
import com.foreverrafs.downloader.DownloadException
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

    fun downloadVideoFile(downloadInfo: DownloadInfo) {
        videoDownloader?.downloadFile(downloadInfo, object : DownloadEvents {
            override fun onDownloadProgressChanged(currentBytes: Long, totalBytes: Long) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDownloadPaused() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDownloadCompleted() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDownloadError(error: DownloadException) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDownloadCancelled() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDownloadStart() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }

}