package com.foreverrafs.rdownloader

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.foreverrafs.rdownloader.adapter.HomeSectionsPagerAdapter
import com.foreverrafs.rdownloader.androidext.requestStoragePermission
import com.foreverrafs.rdownloader.ui.add.AddUrlFragment
import com.foreverrafs.rdownloader.ui.downloads.DownloadsFragment
import com.foreverrafs.rdownloader.ui.videos.VideosFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val storageReqCode = 1000
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestStoragePermission(storageReqCode)
        }

        initializeTabComponents()
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            storageReqCode ->
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
        val viewPagerAdapter = HomeSectionsPagerAdapter(this)
        viewPagerAdapter.addFragment(AddUrlFragment(), "")    // index 0
        viewPagerAdapter.addFragment(DownloadsFragment(), "")   // index 1
        viewPagerAdapter.addFragment(VideosFragment(), "")   // index 1

        viewPager.adapter = viewPagerAdapter
    }

}
