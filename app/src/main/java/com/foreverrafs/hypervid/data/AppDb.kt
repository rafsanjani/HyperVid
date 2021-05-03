package com.foreverrafs.hypervid.data

import android.net.Uri
import androidx.databinding.adapters.Converters
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.hypervid.data.dao.DownloadDao
import com.foreverrafs.hypervid.data.dao.VideoDao
import com.foreverrafs.hypervid.model.FBVideo

@Database(entities = [FBVideo::class, DownloadInfo::class], version = 3, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun videoDao(): VideoDao
}