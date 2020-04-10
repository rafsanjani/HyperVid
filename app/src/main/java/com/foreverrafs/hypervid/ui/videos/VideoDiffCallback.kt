package com.foreverrafs.hypervid.ui.videos

import androidx.recyclerview.widget.DiffUtil
import com.foreverrafs.hypervid.model.FacebookVideo


/* Created by Rafsanjani on 02/04/2020. */

class VideoDiffCallback(
    private val oldList: List<FacebookVideo>,
    private val newList: List<FacebookVideo>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val (_, oldDuration, oldPath) = oldList[oldItemPosition]
        val (_, duration, path) = newList[oldItemPosition]

        return oldDuration == duration && oldPath == path
    }
}