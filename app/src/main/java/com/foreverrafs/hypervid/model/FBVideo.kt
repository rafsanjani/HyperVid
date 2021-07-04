package com.foreverrafs.hypervid.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class FBVideo(
    val title: String,
    val duration: Long = 0L,
    val path: String,
    val downloadUrl: String,
    val originalUrl: String,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)
