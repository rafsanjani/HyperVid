package com.foreverrafs.downloader

import android.content.Context
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.foreverrafs.downloader.model.DownloadInfo


class VideoDownloader private constructor(private val context: Context) : Downloader {
    companion object {
        var instance: VideoDownloader? = null

        fun getInstance(context: Context): VideoDownloader? {
            if (instance == null) {
                instance = VideoDownloader(context)
                val config = PRDownloaderConfig.newBuilder()
                    .setDatabaseEnabled(true)
                    .setConnectTimeout(30_000)
                    .setReadTimeout(30_3000)
                    .build()
                PRDownloader.initialize(context, config)
            }

            return instance
        }
    }

    /**
     * Suspend because we can't really tell whether our download engine will schedule the download operation on the
     * main thread or not. We suspend this so that we may call it in a different context other than main
     */
    override fun downloadFile(
        downloadInfo: DownloadInfo,
        videoDownloadListener: DownloadEvents
    ): Int {
        return PRDownloader.download(
            downloadInfo.url,
            context.filesDir.absolutePath,
            "downloads/${downloadInfo.name}.mp4"
        ).build()
            .setOnStartOrResumeListener {
                videoDownloadListener.onDownloadStart()
            }.setOnPauseListener {
                videoDownloadListener.onDownloadPaused()

            }.setOnCancelListener {
                videoDownloadListener.onDownloadCancelled()
            }.setOnProgressListener { progress ->
                videoDownloadListener.onDownloadProgressChanged(
                    progress.currentBytes,
                    progress.totalBytes
                )
            }.start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    videoDownloadListener.onDownloadCompleted()
                }

                override fun onError(error: Error) {
                    if (error.isConnectionError) {
                        videoDownloadListener.onDownloadError(
                            DownloadException(
                                error.connectionException.message!!,
                                DownloadException.ExceptionType.NETWORK_ERROR
                            )
                        )
                    } else if (error.isServerError) {
                        videoDownloadListener.onDownloadError(
                            DownloadException(
                                error.serverErrorMessage,
                                DownloadException.ExceptionType.SERVER_ERROR
                            )
                        )
                    }
                }
            })
    }

    override fun pauseDownload(downloadId: Int): Boolean {
        PRDownloader.pause(downloadId)
        return true
    }

    override fun cancelDownload(downloadId: Int): Boolean {
        PRDownloader.cancel(downloadId)
        return true
    }
}