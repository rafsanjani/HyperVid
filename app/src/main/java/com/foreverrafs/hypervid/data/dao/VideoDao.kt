package com.foreverrafs.hypervid.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.foreverrafs.hypervid.model.FBVideo


/* Created by Rafsanjani on 23/05/2020. */

@Dao
abstract class VideoDao : BaseDao<FBVideo> {
    @Query("SELECT * from videos")
    abstract fun getVideos(): LiveData<List<FBVideo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(videos: List<FBVideo>)
}