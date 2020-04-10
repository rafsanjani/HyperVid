package com.foreverrafs.hypervid.ui.downloads

import androidx.recyclerview.widget.DiffUtil
import com.foreverrafs.downloader.model.DownloadInfo


/* Created by Rafsanjani on 02/04/2020. */

class DownloadDiffCallback(
    private val oldList: List<DownloadInfo>,
    private val newList: List<DownloadInfo>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].url == newList[newItemPosition].url
    }
}