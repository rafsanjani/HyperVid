package com.foreverrafs.hyperdownloader.ui.videos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import com.foreverrafs.hyperdownloader.MainViewModel
import com.foreverrafs.hyperdownloader.R
import com.foreverrafs.hyperdownloader.databinding.FragmentVideosBinding
import com.foreverrafs.hyperdownloader.databinding.ListEmptyBinding
import com.foreverrafs.hyperdownloader.model.FacebookVideo
import com.foreverrafs.hyperdownloader.util.ItemTouchCallback
import com.foreverrafs.hyperdownloader.util.invisible
import com.foreverrafs.hyperdownloader.util.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File


class VideosFragment : Fragment(), VideoAdapter.VideoCallback {
    private val vm: MainViewModel by activityViewModels()
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var videoBinding: FragmentVideosBinding
    private lateinit var emptyListBinding: ListEmptyBinding


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

        videoAdapter = VideoAdapter(requireActivity().applicationContext, this)

        videoBinding.videoListRecyclerView.adapter =
            videoAdapter

        val itemTouchHelperCallback = ItemTouchCallback(videoAdapter)

        val touchHelper = ItemTouchHelper(itemTouchHelperCallback)

        touchHelper.attachToRecyclerView(videoBinding.videoListRecyclerView)

        initEmptyLayoutTexts()

        vm.videosList.observe(viewLifecycleOwner, Observer { videosList ->
            if (videosList.isNotEmpty()) {
                videoBinding.videoListRecyclerView.visible()
                emptyListBinding.root.invisible()

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
        vm.saveVideoList(videoAdapter.videos)
        super.onPause()
    }

    override fun deleteVideo(video: FacebookVideo) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_delete_video)
            .setIcon(R.drawable.ic_delete)
            .setMessage(R.string.prompt_delete_video)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                videoAdapter.deleteVideo(video)
                File(video.path).delete()
                vm.setVideosList(videoAdapter.videos)
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }
}