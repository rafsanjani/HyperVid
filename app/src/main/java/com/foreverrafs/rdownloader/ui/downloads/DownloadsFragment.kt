package com.foreverrafs.rdownloader.ui.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.foreverrafs.rdownloader.MainViewModel
import com.foreverrafs.rdownloader.R
import com.foreverrafs.rdownloader.adapter.DownloadsAdapter
import com.foreverrafs.rdownloader.util.invisible
import com.foreverrafs.rdownloader.util.visible
import kotlinx.android.synthetic.main.fragment_downloads.*

class DownloadsFragment : Fragment() {
    private lateinit var downloadsAdapter: DownloadsAdapter
    private val vm: MainViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        downloadsAdapter = DownloadsAdapter(requireContext())

        initializeViews()
    }

    private fun initializeViews() {
        downloadListRecyclerView.adapter = downloadsAdapter
    }

    override fun onResume() {
        super.onResume()
        vm.downloadList.observe(viewLifecycleOwner, Observer { downloadList ->
            if (downloadList.isNotEmpty()) {
                downloadListRecyclerView.visible()
                layoutEmpty.invisible()
                downloadsAdapter.submitList(downloadList)
            } else {
                downloadListRecyclerView.invisible()
                layoutEmpty.visible()
            }
        })
    }
}