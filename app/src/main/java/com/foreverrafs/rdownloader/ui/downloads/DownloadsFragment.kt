package com.foreverrafs.rdownloader.ui.downloads

import android.os.Bundle
import android.os.Handler
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
import kotlinx.android.synthetic.main.fragment_downloads.*

class DownloadsFragment private constructor() : Fragment(), DownloadAdapter.Events {
    private var downloadsAdapter = DownloadAdapter(this)

    private val vm: MainViewModel by activityViewModels()
    private val videosList = mutableListOf<FacebookVideo>()
    private var downloadList = mutableListOf<DownloadInfo>()

    private lateinit var downloadBinding: FragmentDownloadsBinding
    private lateinit var emptyListBinding: ListEmptyBinding


    companion object {
        private lateinit var pageNavigator: (pageNumber: Int) -> Boolean

        fun newInstance(listener: (pageNumber: Int) -> Boolean = { true }): DownloadsFragment {
            this.pageNavigator = listener
            return DownloadsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        downloadBinding = FragmentDownloadsBinding.inflate(inflater)
        emptyListBinding = downloadBinding.layoutEmpty

        downloadBinding.downloadListRecyclerView.adapter = downloadsAdapter

        return downloadBinding.root
    }


    private val downloadListObserver = Observer<List<DownloadInfo>> { list ->
        if (list.isNotEmpty()) {
            downloadList = list.toMutableList()
            emptyListBinding.root.invisible()

            Handler().postDelayed({
                showDownloads(downloadList)
            }, 300)

        } else {
            showEmptyScreen()
        }
    }

    private fun showEmptyScreen() {
        downloadBinding.downloadListRecyclerView.invisible()
        emptyListBinding.root.visible()
        progressBar.invisible()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm.downloadList.observe(viewLifecycleOwner, downloadListObserver)
    }


    private fun showDownloads(downloadList: List<DownloadInfo>) {
        progressBar.invisible()
        downloadBinding.downloadListRecyclerView.visible()

        downloadsAdapter.submitList(downloadList.toMutableList())
    }

    override fun onPause() {
        vm.saveDownloadList(downloadList)
        super.onPause()
    }


    /**
     * when a video has been successfully downloaded from the list. The adapter position of the downloaded
     * video together with the video item downloaded are received
     */
    override fun onSuccess(position: Int, video: FacebookVideo) {
        val downloadExists = videosList.any {
            it.path == video.path
        }

        if (!downloadExists) {
            videosList.add(video)
            vm.setVideosList(videosList)

            //navigate to the videos page : 2
            pageNavigator(2)

            Handler().postDelayed({

            }, 2000)
        } else {
            Toast.makeText(requireContext(), "Duplicate Video", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * When a download item is removed via the adapter, an event is propagated back to the fragment which is
     * used to resolve the UI
     */
    override fun onDeleted(position: Int) {
        downloadList.removeAt(position)

        if (downloadList.isNotEmpty())
            showDownloads(downloadList)
        else
            showEmptyScreen()
    }

    override fun onError(position: Int) {
        //NO-OP
    }
}