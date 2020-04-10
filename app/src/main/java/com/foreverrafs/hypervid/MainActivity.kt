package com.foreverrafs.hypervid

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.foreverrafs.downloader.downloader.VideoDownloader
import com.foreverrafs.hypervid.adapter.HomePagerAdapter
import com.foreverrafs.hypervid.androidext.requestStoragePermission
import com.foreverrafs.hypervid.ui.add.AddUrlFragment
import com.foreverrafs.hypervid.ui.downloads.DownloadsFragment
import com.foreverrafs.hypervid.ui.videos.VideosFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val STORAGE_REQ_CODE = 1000
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestStoragePermission(STORAGE_REQ_CODE)
        }

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        if (viewModel.isFirstRun)
            showDisclaimer()

        initializeTabComponents()
        viewModel.getDownloadList()
        viewModel.getVideoList()
    }

    private fun showDisclaimer() {
        MaterialAlertDialogBuilder(this)
            .setMessage(
                getString(R.string.message_copyright_notice)
            )
            .setTitle(getString(R.string.title_copyright_notice))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.isFirstRun = false
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                finish()
            }.show()
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
                0 -> tab.text = resources.getString(R.string.title_url)
                1 -> tab.text = resources.getString(R.string.title_downloads)
                2 -> tab.text = resources.getString(R.string.title_videos)
            }
        }.attach()
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        val fragments = listOf(
            AddUrlFragment.newInstance {
                navigateTo(it)
            },

            DownloadsFragment.newInstance {
                navigateTo(it)
            },
            VideosFragment()
        )
        val viewPagerAdapter = HomePagerAdapter(this)

        fragments.forEach {
            viewPagerAdapter.addFragment(it)
        }

        viewPager.adapter = viewPagerAdapter
    }

    private fun navigateTo(pageNumber: Int): Boolean {
        Handler().postDelayed({
            viewPager.setCurrentItem(pageNumber, true)
        }, 500)

        return true
    }

    override fun onDestroy() {
        VideoDownloader.getInstance(this)?.close()
        super.onDestroy()
    }
}


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
