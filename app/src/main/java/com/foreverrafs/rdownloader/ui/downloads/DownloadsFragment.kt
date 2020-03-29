package com.foreverrafs.rdownloader.ui.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.rdownloader.MainViewModel
import com.foreverrafs.rdownloader.databinding.FragmentDownloadsBinding
import com.foreverrafs.rdownloader.databinding.ListEmptyBinding
import com.foreverrafs.rdownloader.model.FacebookVideo
import com.foreverrafs.rdownloader.util.invisible
import com.foreverrafs.rdownloader.util.visible

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


        vm.downloadList.observe(viewLifecycleOwner, Observer { downloadList ->
            if (downloadList.isNotEmpty()) {
                emptyListBinding.root.invisible()

                showDownloads(downloadList)

            } else {
                downloadBinding.downloadListRecyclerView.invisible()
                emptyListBinding.root.visible()
            }
        })
    }

    private fun showDownloads(downloadList : List<DownloadInfo>){
        downloadBinding.downloadListRecyclerView.visible()

        downloadsAdapter = DownloadsAdapter(downloadList.toMutableList()) {
            onVideoDownloaded(it)
        }
        downloadBinding.downloadListRecyclerView.adapter = downloadsAdapter
    }


    private fun onVideoDownloaded(video: FacebookVideo) {
        videosList.add(video)

        Toast.makeText(requireContext(), "Video downloaded", Toast.LENGTH_SHORT).show()
        vm.setVideosList(videosList)
    }
}