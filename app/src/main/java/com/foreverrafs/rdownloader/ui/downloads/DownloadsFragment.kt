package com.foreverrafs.rdownloader.ui.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.foreverrafs.rdownloader.R
import com.foreverrafs.rdownloader.SharedViewModel
import com.foreverrafs.rdownloader.adapter.DownloadsAdapter
import com.foreverrafs.rdownloader.util.invisible
import com.foreverrafs.rdownloader.util.visible
import kotlinx.android.synthetic.main.fragment_downloads.*

class DownloadsFragment : Fragment() {
    private lateinit var downloadsAdapter: DownloadsAdapter
    private val mainViewModel: SharedViewModel by activityViewModels()
    private val downloadsViewModel: DownloadsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        downloadsAdapter = mainViewModel.downloadsAdapter

        downloadListRecyclerView.adapter = downloadsAdapter

        downloadsAdapter.addDownloadListChangedListener(object :
            DownloadsAdapter.DownloadListChangedListener {
            override fun onListChanged(listSize: Int) {
                if (listSize > 0) {
                    downloadListRecyclerView.visible()
                    layoutEmpty.invisible()
                } else {
                    downloadListRecyclerView.invisible()
                    layoutEmpty.visible()
                }
            }
        })
    }

    override fun onStart() {
        if (downloadsAdapter.itemCount > 0) {
            downloadListRecyclerView.visible()
            layoutEmpty.invisible()
        } else {
            downloadListRecyclerView.invisible()
            layoutEmpty.visible()
        }
        super.onStart()
    }
}