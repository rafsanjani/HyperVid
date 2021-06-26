package com.foreverrafs.downloader.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadInfo(
    val url: String,
    val downloadId: Int,
    val name: String,
    val originalUrl: String,
    var currentBytes: Long = 0,
    var totalBytes: Long = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val extension: String = "mp4",
    var isCompleted: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
