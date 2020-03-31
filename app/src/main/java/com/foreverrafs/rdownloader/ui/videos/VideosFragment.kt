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
import com.foreverrafs.rdownloader.databinding.FragmentVideosBinding
import com.foreverrafs.rdownloader.databinding.ListEmptyBinding
import com.foreverrafs.rdownloader.model.FacebookVideo
import com.foreverrafs.rdownloader.util.invisible
import com.foreverrafs.rdownloader.util.visible


class VideosFragment : Fragment() {
    private val vm: MainViewModel by activityViewModels()
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var videoBinding: FragmentVideosBinding
    private lateinit var emptyListBinding: ListEmptyBinding
    private var videoList = mutableListOf<FacebookVideo>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        videoBinding = FragmentVideosBinding.inflate(inflater)
        emptyListBinding = videoBinding.emptyLayout

        return videoBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoAdapter = VideoAdapter(requireContext())

        videoBinding.videoListRecyclerView.adapter =
            videoAdapter

        initEmptyLayoutTexts()

        vm.videosList.observe(viewLifecycleOwner, Observer { videosList ->
            if (videosList.isNotEmpty()) {
                videoBinding.videoListRecyclerView.visible()
                emptyListBinding.root.invisible()

                this.videoList = videosList.toMutableList()

                videoAdapter.submitList(videosList)

            } else {
                videoBinding.videoListRecyclerView.invisible()
                emptyListBinding.root.visible()
            }
        })
    }

    private fun initEmptyLayoutTexts() {
        emptyListBinding.apply {
            tvDescription.text = getString(R.string.empty_video_desc)
            tvTitle.text = getString(R.string.empty_video)
        }
    }

    override fun onPause() {
        vm.saveVideoList(videoList)
        super.onPause()
    }
}