package com.foreverrafs.hypervid.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.foreverrafs.hypervid.ui.TabLayoutCoordinator
import com.foreverrafs.hypervid.ui.add.AddUrlFragment
import com.foreverrafs.hypervid.ui.downloads.DownloadsFragment
import com.foreverrafs.hypervid.ui.videos.VideosFragment

class HomePagerAdapter(
    activity: FragmentActivity,
    private val tabLayoutCoordinator: TabLayoutCoordinator
) :
    FragmentStateAdapter(activity) {
    companion object {
        const val FRAGMENT_COUNT = 3
    }

    override fun getItemCount(): Int {
        return FRAGMENT_COUNT
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AddUrlFragment.newInstance(tabLayoutCoordinator)
            1 -> DownloadsFragment.newInstance(tabLayoutCoordinator)
            2 -> VideosFragment.newInstance(tabLayoutCoordinator)
            else -> throw IllegalArgumentException("Invalid fragment position specified")
        }
    }
}
