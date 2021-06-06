package com.foreverrafs.hypervid.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.foreverrafs.hypervid.data.HyperVidDB
import com.foreverrafs.hypervid.data.repository.AppRepository
import com.foreverrafs.hypervid.data.repository.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Singleton
    @Provides
    fun provideRepository(database: HyperVidDB, dispatcher: CoroutineDispatcher): Repository =
        AppRepository(hyperVidDB = database, dispatcher = dispatcher)

    @Singleton
    @Provides
    fun provideDispatcher() = Dispatchers.Default

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): HyperVidDB {
        return Room
            .databaseBuilder(context, HyperVidDB::class.java, "hypervid.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
}