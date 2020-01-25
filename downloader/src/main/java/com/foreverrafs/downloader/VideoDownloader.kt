package com.foreverrafs.downloader

import android.content.Context
import android.os.Environment
import com.downloader.*
import com.foreverrafs.downloader.model.DownloadInfo


class VideoDownloader private constructor(private val context: Context) : Downloader {
    companion object {
        var instance: VideoDownloader? = null

        fun getInstance(context: Context): VideoDownloader? {
            if (instance == null) {
                instance = VideoDownloader(context)
                val config = PRDownloaderConfig.newBuilder()
                    .setConnectTimeout(30_000)
                    .setReadTimeout(30_3000)
                    .build()
                PRDownloader.initialize(context, config)
            }
            return instance
        }
    }

    fun getDownloadDir(): String {
        return context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.absolutePath!!
    }
    override fun downloadFile(
        downloadInfo: DownloadInfo,
        videoDownloadListener: DownloadEvents
    ): Int {
        return PRDownloader.download(
            downloadInfo.url,
            getDownloadDir(),
            "${downloadInfo.name}.mp4"
        ).setPriority(Priority.IMMEDIATE)
            .build()
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

    fun close() {
        PRDownloader.shutDown()
    }
}