package com.foreverrafs.hypervid.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.invoke
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.foreverrafs.downloader.downloader.VideoDownloader
import com.foreverrafs.hypervid.R
import com.foreverrafs.hypervid.adapter.HomePagerAdapter
import com.foreverrafs.hypervid.ui.add.AddUrlFragment
import com.foreverrafs.hypervid.ui.downloads.DownloadsFragment
import com.foreverrafs.hypervid.ui.videos.VideosFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel

    private val requestStorPermission: ActivityResultLauncher<String> =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (!granted) {
                    finish()
                }
            }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            Timber.i("NULL")
        } else {
            Timber.i("NOT NULL")
        }

        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        if (viewModel.isFirstRun)
            showDisclaimer()

        initializeTabComponents()

        showCounterBadges()
    }

    private fun showCounterBadges() {
        viewModel.downloadList.observe(this, Observer { downloads ->
            if (downloads.isNotEmpty()) {
                tabLayout.getTabAt(1)?.getOrCreateBadge()?.apply {
                    isVisible = true
                    backgroundColor = R.color.colorPrimary
                    number = downloads.size
                }
            } else {
                tabLayout.getTabAt(1)?.badge?.isVisible = false
            }
        })

        viewModel.videosList.observe(this, Observer { videos ->
            if (videos.isNotEmpty()) {
                tabLayout.getTabAt(2)?.getOrCreateBadge()?.apply {
                    isVisible = true
                    backgroundColor = R.color.colorPrimary
                    number = videos.size
                }
            } else {
                tabLayout.getTabAt(1)?.badge?.isVisible = false
            }
        })
    }

    private fun showDisclaimer() {
        MaterialAlertDialogBuilder(this)
                .setMessage(
                        getString(R.string.message_copyright_notice)
                )
                .setTitle(getString(R.string.title_copyright_notice))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestStorPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        viewModel.isFirstRun = false
                    }
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    finish()
                }.show()
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
                AddUrlFragment(),
                DownloadsFragment(),
                VideosFragment()
        )
        val viewPagerAdapter = HomePagerAdapter(this)

        fragments.forEach {
            viewPagerAdapter.addFragment(it)
        }

        viewPager.adapter = viewPagerAdapter
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
