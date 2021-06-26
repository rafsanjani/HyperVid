package com.foreverrafs.hypervid.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.foreverrafs.hypervid.R
import com.foreverrafs.hypervid.adapter.HomePagerAdapter
import com.foreverrafs.hypervid.databinding.ActivityMainBinding
import com.foreverrafs.hypervid.ui.states.DownloadListState
import com.foreverrafs.hypervid.ui.states.VideoListState
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), TabLayoutCoordinator {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeTabLayout()
    }

    private fun initializeTabLayout() = with(binding) {
        setupViewPager(viewPager)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = resources.getString(R.string.title_url)
                1 -> tab.text = resources.getString(R.string.title_downloads)
                2 -> tab.text = resources.getString(R.string.title_videos)
            }
        }.attach()

        showCounterBadges()
    }

    private fun showCounterBadges() = with(binding) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.downloadState.collect { state ->
                    when (state) {
                        is DownloadListState.DownloadList -> {
                            if (state.downloads.isNotEmpty()) {
                                tabLayout.getTabAt(1)?.orCreateBadge?.apply {
                                    isVisible = true
                                    backgroundColor =
                                        ContextCompat.getColor(
                                            applicationContext,
                                            R.color.colorPrimary
                                        )
                                    number = state.downloads.size
                                }
                            } else {
                                tabLayout.getTabAt(1)?.badge?.isVisible = false
                            }
                        }
                        is DownloadListState.Error -> {
                            Timber.e(state.exception)
                        }
                        DownloadListState.Loading -> {
                            Timber.d("Loading downloadstate")
                        }
                    }
                }

                viewModel.videosListState.collect { state ->
                    when (state) {
                        is VideoListState.Error -> {
                            tabLayout.getTabAt(2)?.badge?.isVisible = false
                        }

                        VideoListState.Loading -> {
                            Timber.d("showCounterBadges: Loading Video List")
                        }

                        is VideoListState.Videos -> {
                            tabLayout.getTabAt(2)?.orCreateBadge?.apply {
                                isVisible = true
                                backgroundColor =
                                    ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                                number = state.videos.size
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        val viewPagerAdapter = HomePagerAdapter(activity = this, tabLayoutCoordinator = this)
        viewPager.adapter = viewPagerAdapter
    }

    override fun navigateToTab(position: Int) {
        lifecycleScope.launch {
            delay(1000)
            binding.viewPager.currentItem = position
        }
    }
}
