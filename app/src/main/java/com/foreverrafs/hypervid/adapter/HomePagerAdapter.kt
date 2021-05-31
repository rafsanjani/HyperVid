package com.foreverrafs.hypervid.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.foreverrafs.hypervid.ui.add.AddUrlFragment
import com.foreverrafs.hypervid.ui.downloads.DownloadsFragment
import com.foreverrafs.hypervid.ui.videos.VideosFragment

class HomePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    companion object {
        const val FRAGMENT_COUNT = 3
    }

    override fun getItemCount(): Int {
        return FRAGMENT_COUNT
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AddUrlFragment()
            1 -> DownloadsFragment()
            2 -> VideosFragment()
            else -> throw IllegalArgumentException("Invalid fragment position specified")
        }

    }
}