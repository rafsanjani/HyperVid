package com.foreverrafs.hypervid.data.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update


/* Created by Rafsanjani on 26/03/2020. */

interface BaseDao<T> {
    @Insert
    fun insert(item: T) : Long

    @Update
    fun update(item: T) : Int

    @Delete
    fun delete(item: T) : Int
}