package com.foreverrafs.downloader.downloader

import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import okhttp3.OkHttpClient


class ParallelDownloader(okHttpClient: OkHttpClient) : OkHttpDownloader(okHttpClient) {
    private val slices = 10

    override fun getFileSlicingCount(request: Downloader.ServerRequest, contentLength: Long): Int? {
        return slices
    }

    override fun getRequestFileDownloaderType(
        request: Downloader.ServerRequest,
        supportedFileDownloaderTypes: Set<Downloader.FileDownloaderType>
    ): Downloader.FileDownloaderType {
        return Downloader.FileDownloaderType.PARALLEL
    }

}