package com.foreverrafs.rdownloader.ui.videos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.foreverrafs.rdownloader.MainViewModel
import com.foreverrafs.rdownloader.R
import com.foreverrafs.rdownloader.adapter.VideosAdapter
import com.foreverrafs.rdownloader.util.invisible
import com.foreverrafs.rdownloader.util.visible
import kotlinx.android.synthetic.main.fragment_downloads.*
import kotlinx.android.synthetic.main.fragment_videos.*
import kotlinx.android.synthetic.main.fragment_videos.layoutEmpty
import kotlinx.android.synthetic.main.list_empty.*


class VideosFragment : Fragment() {
    private val vm: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoListRecyclerView.adapter = VideosAdapter(requireContext())

        initEmptyLayoutTexts()
    }

    private fun initEmptyLayoutTexts() {
        tvTitle.text = getString(R.string.empty_video)
        tvDescription.text = getString(R.string.empty_video_desc)
    }

    override fun onResume() {
        super.onResume()
        vm.downloadedList.observe(viewLifecycleOwner, Observer { downloadList ->
            if (downloadList.isNotEmpty()) {
                downloadListRecyclerView.visible()
                layoutEmpty.invisible()

            } else {
                downloadListRecyclerView.invisible()
                layoutEmpty.visible()
            }
        })
    }
}