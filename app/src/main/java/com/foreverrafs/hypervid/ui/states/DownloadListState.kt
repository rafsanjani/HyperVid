package com.foreverrafs.hypervid.ui.states

import com.foreverrafs.downloader.model.DownloadInfo

sealed class DownloadListState {
    object Loading : DownloadListState()
    data class Error(val exception: Throwable) : DownloadListState()
    data class DownloadList(val downloads: List<DownloadInfo>) : DownloadListState()
}
