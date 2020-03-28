package com.foreverrafs.rdownloader.ui.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.foreverrafs.rdownloader.MainViewModel
import com.foreverrafs.rdownloader.databinding.FragmentDownloadsBinding
import com.foreverrafs.rdownloader.databinding.ListEmptyBinding
import com.foreverrafs.rdownloader.model.FacebookVideo
import com.foreverrafs.rdownloader.util.invisible
import com.foreverrafs.rdownloader.util.visible
import kotlinx.android.synthetic.main.fragment_downloads.*

class DownloadsFragment : Fragment() {
    private lateinit var downloadsAdapter: DownloadsAdapter
    private val vm: MainViewModel by activityViewModels()
    private val videosList = mutableListOf<FacebookVideo>()

    private lateinit var downloadBinding: FragmentDownloadsBinding
    private lateinit var emptyListBinding: ListEmptyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        downloadBinding = FragmentDownloadsBinding.inflate(inflater)
        emptyListBinding = downloadBinding.layoutEmpty

        return downloadBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        downloadsAdapter =
            DownloadsAdapter(
                requireContext()
            ) {
                onVideoDownloaded(it)
            }

        initializeViews()
    }

    private fun initializeViews() {
        downloadBinding.downloadListRecyclerView.adapter = downloadsAdapter
    }

    private fun onVideoDownloaded(video: FacebookVideo) {
        videosList.add(video)

        Toast.makeText(requireContext(), "Video downloaded", Toast.LENGTH_SHORT).show()
        vm.setVideosList(videosList)
    }

    override fun onResume() {
        super.onResume()
        vm.downloadList.observe(viewLifecycleOwner, Observer { downloadList ->
            if (downloadList.isNotEmpty()) {
                emptyListBinding.root.invisible()
                downloadBinding.downloadListRecyclerView.visible()

                downloadsAdapter.submitList(downloadList)
            } else {
                downloadListRecyclerView.invisible()
                emptyListBinding.root.visible()
            }
        })
    }
}