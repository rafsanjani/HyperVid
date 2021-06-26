package com.foreverrafs.hypervid.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.hypervid.data.dao.DownloadDao
import com.foreverrafs.hypervid.data.dao.VideoDao
import com.foreverrafs.hypervid.model.FBVideo

@Database(
    entities = [FBVideo::class, DownloadInfo::class],
    version = 5,
    exportSchema = false
)
abstract class HyperVidDB : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun videoDao(): VideoDao
}
