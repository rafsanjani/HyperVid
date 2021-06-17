package com.foreverrafs.downloader.downloader

import android.content.Context
import android.os.Environment
import com.foreverrafs.downloader.model.DownloadInfo
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.FetchLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class VideoDownloader
@Inject
constructor(@ApplicationContext private val context: Context) : Downloader {

    private lateinit var fetch: Fetch
    private var downloads = mutableMapOf<Int, DownloadEvents>()

    private val listener = object : AbstractFetchListener() {
        override fun onCancelled(download: Download) {
            downloads[download.id]?.onCancelled()
            downloads.remove(download.id)
        }

        override fun onCompleted(download: Download) {
            downloads[download.id]?.onCompleted(download.file)
            downloads.remove(download.id)
        }

        override fun onError(
            download: Download,
            error: Error,
            throwable: Throwable?
        ) {
            downloads[download.id]?.onError(
                DownloadException(
                    throwable?.message!!,
                    DownloadException.ExceptionType.NETWORK_ERROR
                )
            )
            Timber.e(throwable)
        }

        override fun onPaused(download: Download) {
            downloads[download.id]?.onPause()
        }

        override fun onProgress(
            download: Download,
            etaInMilliSeconds: Long,
            downloadedBytesPerSecond: Long
        ) {
            downloads[download.id]?.onProgressChanged(
                download.downloaded,
                download.progress
            )

            Timber.d(download.progress.toString())
        }

        override fun onStarted(
            download: Download,
            downloadBlocks: List<DownloadBlock>,
            totalBlocks: Int
        ) {
            downloads[download.id]?.onStart()
        }

        override fun onWaitingNetwork(download: Download) {
            downloads[download.id]?.onWaitingForNetwork()
        }
    }

    init {
        setUpDownloader()
    }


    private fun setUpDownloader() {
        val config = FetchConfiguration.Builder(context)
            .enableFileExistChecks(false)
            .setLogger(FetchLogger())
            .setHttpDownloader(
                ParallelDownloader(
                    OkHttpClient.Builder().connectTimeout(1, TimeUnit.MINUTES)
                        .build()
                )
            )
            .setDownloadConcurrentLimit(4)
            .enableAutoStart(true)
            .enableLogging(true)
            .enableRetryOnNetworkGain(true)
            .build()

        fetch = Fetch.getInstance(config)

        fetch.addListener(listener)
    }

    private fun getDownloadDir(): String {
        return context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.absolutePath!!
    }

    override fun downloadFile(
        downloadInfo: DownloadInfo,
        videoDownloadListener: DownloadEvents
    ): Int {
        val request = Request(
            downloadInfo.url,
            "${getDownloadDir()}/${UUID.randomUUID()}.${downloadInfo.extension}"
        )

        request.apply {
            priority = Priority.HIGH
            networkType = NetworkType.ALL
        }
        downloads[request.id] = videoDownloadListener

        fetch.enqueue(request)

        return request.id
    }


    override fun pauseDownload(downloadId: Int): Boolean {
        fetch.pause(downloadId)
        downloads[downloadId]?.onPause()
        return true
    }

    override fun cancelDownload(downloadId: Int): Boolean {
        fetch.cancel(downloadId)
        downloads[downloadId]?.onCancelled()
        return true
    }

    fun close() {
        fetch.pauseAll()
        fetch.removeListener(listener)
        fetch.close()
    }
}