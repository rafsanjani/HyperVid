package com.foreverrafs.rdownloader

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.foreverrafs.downloader.downloader.VideoDownloader
import com.foreverrafs.rdownloader.adapter.HomePagerAdapter
import com.foreverrafs.rdownloader.androidext.requestStoragePermission
import com.foreverrafs.rdownloader.ui.add.AddUrlFragment
import com.foreverrafs.rdownloader.ui.downloads.DownloadsFragment
import com.foreverrafs.rdownloader.ui.videos.VideosFragment
import com.foreverrafs.rdownloader.util.fromJson
import com.foreverrafs.rdownloader.util.toJson
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    companion object {
        const val STORAGE_REQ_CODE = 1000
        const val PREF_KEY_DOWNLOADS = "download_list"
    }

    private lateinit var viewModel: MainViewModel

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestStoragePermission(STORAGE_REQ_CODE)
        }

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        initializeTabComponents()
        retrieveDownloadList()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            STORAGE_REQ_CODE ->
                if (grantResults.isEmpty() && (grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                    //request was denied so show user a friendly message telling him why the application will not work
                    //without getting storage access. How will we store the downloaded files? mmmm
                }
        }
    }

    private fun initializeTabComponents() {
        setupViewPager(viewPager)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = resources.getString(R.string.Url)
                1 -> tab.text = resources.getString(R.string.Downloads)
                2 -> tab.text = resources.getString(R.string.Videos)
            }
        }.attach()
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        val viewPagerAdapter = HomePagerAdapter(this)
        viewPagerAdapter.addFragment(AddUrlFragment())    // index 0
        viewPagerAdapter.addFragment(DownloadsFragment())   // index 1
        viewPagerAdapter.addFragment(VideosFragment())   // index 1

        viewPager.adapter = viewPagerAdapter
    }

    override fun onDestroy() {
        VideoDownloader.getInstance(this)?.close()
        saveDownloadList()
        super.onDestroy()
    }

    private fun saveDownloadList() {
        val downloadList = viewModel.downloadList.value

        downloadList?.let { list ->
            if (list.isNotEmpty()) {
                val json = list.toJson()
                preferences.edit().putString(PREF_KEY_DOWNLOADS, json).apply()
                Timber.i("Saved ${list.size} download items")
                return@let
            }

            Timber.i("Download list is empty")

        } ?: Timber.e("Download list is null")


    }

    private fun retrieveDownloadList() {
        val json = preferences.getString(PREF_KEY_DOWNLOADS, null)

        json?.let { listJson ->
            val list = listJson.fromJson()
            viewModel.setDownloadList(list.toMutableList())
            Timber.i("${list.size} downloads retrieved")
        } ?: Timber.i("No previous download found")
    }
}
