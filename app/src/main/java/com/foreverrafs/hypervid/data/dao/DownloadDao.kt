package com.foreverrafs.hypervid.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.foreverrafs.downloader.model.DownloadInfo
import kotlinx.coroutines.flow.Flow


/* Created by Rafsanjani on 23/05/2020. */

@Dao
abstract class DownloadDao : BaseDao<DownloadInfo> {
    @Query("SELECT * from downloads")
    abstract fun getDownloads(): Flow<List<DownloadInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(videos: List<DownloadInfo>)
}