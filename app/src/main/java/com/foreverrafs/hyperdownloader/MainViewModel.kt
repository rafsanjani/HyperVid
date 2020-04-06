package com.foreverrafs.hyperdownloader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.extractor.FacebookExtractor
import com.foreverrafs.hyperdownloader.model.FacebookVideo
import com.foreverrafs.hyperdownloader.util.toJson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber


class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val preference = PreferenceManager.getDefaultSharedPreferences(app)
    private val gson: Gson = Gson()
    private var _downloadList: MutableLiveData<List<DownloadInfo>> = MutableLiveData(emptyList())
    private var _videosList: MutableLiveData<List<FacebookVideo>> = MutableLiveData(
        emptyList()
    )

    companion object {
        const val PREF_KEY_DOWNLOADS = "download_list"
        const val PREF_KEY_VIDEOS = "videos_list"
    }

    val downloadList: LiveData<List<DownloadInfo>>
        get() = _downloadList

    val videosList: LiveData<List<FacebookVideo>>
        get() = _videosList

    fun setDownloadList(list: MutableList<DownloadInfo>) {
        _downloadList.value = list
    }

    fun setVideosList(list: MutableList<FacebookVideo>) {
        _videosList.value = list
    }

    fun extractVideoDownloadUrl(
        streamUrl: String,
        listener: FacebookExtractor.ExtractionEvents
    ) {
        FacebookExtractor().apply {
            addEventsListener(listener)
            extract(streamUrl)
        }
    }

    fun saveDownloadList(downloadList: List<DownloadInfo>) {
        if (downloadList.isNotEmpty()) {
            val json = downloadList.filter { !it.isCompleted }.toJson()
            preference.edit().putString(PREF_KEY_DOWNLOADS, json).apply()
            Timber.i("Saved ${downloadList.size} download items")
            return
        } else {
            preference.edit().putString(PREF_KEY_DOWNLOADS, null).apply()
        }

        Timber.i("Download list is empty")
    }

    fun getDownloadList() {
        val json = preference.getString(PREF_KEY_DOWNLOADS, null)
        json?.let { listJson ->

            val list: MutableList<DownloadInfo> = gson.fromJson(
                listJson,
                object : TypeToken<List<DownloadInfo>>() {}.type
            )

            setDownloadList(list)
            Timber.i("${list.size} downloads retrieved")
        } ?: Timber.i("No previous download found")
    }

    fun saveVideoList(videoList: List<FacebookVideo>) {
        if (videoList.isNotEmpty()) {
            val json = videoList.toJson()
            preference.edit().putString(PREF_KEY_VIDEOS, json).apply()
            Timber.i("Saved ${videoList.size} videos items")
            return
        } else {
            preference.edit().putString(PREF_KEY_VIDEOS, null).apply()
        }

        Timber.i("videos list is empty")
    }

    fun getVideoList() {
        val json = preference.getString(PREF_KEY_VIDEOS, null)
        json?.let { listJson ->
            val list: MutableList<FacebookVideo> = gson.fromJson(
                listJson,
                object : TypeToken<List<FacebookVideo>>() {}.type
            )

            setVideosList(list)
            Timber.i("${list.size} videos retrieved")
        } ?: Timber.i("no video found")
    }

}