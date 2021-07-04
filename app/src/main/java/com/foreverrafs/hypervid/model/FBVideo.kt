package com.foreverrafs.hypervid.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class FBVideo(
    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "duration")
    val duration: Long = 0L,

    @ColumnInfo(name = "path")
    val path: String,

    @ColumnInfo(name = "downloadUrl")
    val downloadUrl: String,

    @ColumnInfo(name = "originalUrl")
    val originalUrl: String,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)
