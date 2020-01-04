package com.foreverrafs.rdownloader.ui.videos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import com.foreverrafs.rdownloader.R
import com.foreverrafs.rdownloader.MainViewModel
import com.foreverrafs.rdownloader.adapter.SimpleItemTouchHelper
import com.foreverrafs.rdownloader.adapter.VideosAdapter
import com.foreverrafs.rdownloader.util.invisible
import com.foreverrafs.rdownloader.util.visible
import kotlinx.android.synthetic.main.fragment_videos.*
import kotlinx.android.synthetic.main.list_empty.*


class VideosFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var videosAdapter: VideosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       return inflater.inflate(R.layout.fragment_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videosAdapter = mainViewModel.videosAdapter
        videoListRecyclerView.adapter = videosAdapter

        initEmptyLayoutTexts()

        val itemTouchHelper = ItemTouchHelper(SimpleItemTouchHelper(videosAdapter))
        itemTouchHelper.attachToRecyclerView(videoListRecyclerView)

        videosAdapter.addVideosListChangedListener(object :
            VideosAdapter.VideosListChangedListener {
            override fun onVideosListChanged(size: Int) {
                if (size > 0) {
                    videoListRecyclerView.visible()
                    layoutEmpty.invisible()
                } else {
                    videoListRecyclerView.invisible()
                    layoutEmpty.visible()
                }
            }
        })

    }

    private fun initEmptyLayoutTexts() {
        tvTitle.text = getString(R.string.empty_video)
        tvDescription.text = getString(R.string.empty_video_desc)
    }

    override fun onResume() {
        if (videosAdapter.itemCount > 0) {
            videoListRecyclerView.visible()
            layoutEmpty.invisible()
        } else {
            videoListRecyclerView.invisible()
            layoutEmpty.visible()
        }
        super.onResume()
    }

}