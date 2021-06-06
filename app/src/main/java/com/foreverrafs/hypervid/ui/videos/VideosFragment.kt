package com.foreverrafs.hypervid.ui.videos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.foreverrafs.hypervid.R
import com.foreverrafs.hypervid.databinding.FragmentVideosBinding
import com.foreverrafs.hypervid.databinding.ListEmptyBinding
import com.foreverrafs.hypervid.model.FBVideo
import com.foreverrafs.hypervid.ui.MainViewModel
import com.foreverrafs.hypervid.ui.TabLayoutCoordinator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import invisible
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import visible
import java.io.File


@AndroidEntryPoint
class VideosFragment
private constructor() : Fragment(R.layout.fragment_videos),
    VideoAdapter.VideoCallback {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val binding by viewBinding(FragmentVideosBinding::bind)
    private lateinit var emptyListBinding: ListEmptyBinding

    private lateinit var videoAdapter: VideoAdapter

    companion object {
        var tabLayoutCoordinator: TabLayoutCoordinator? = null

        fun newInstance(tabLayoutCoordinator: TabLayoutCoordinator): VideosFragment {
            this.tabLayoutCoordinator = tabLayoutCoordinator
            return VideosFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        emptyListBinding = binding.emptyLayout

        videoAdapter = VideoAdapter(this)

        binding.videoListRecyclerView.adapter = videoAdapter

        initEmptyLayoutTexts()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.videosList.collect { videosList ->
                    if (videosList.isNotEmpty()) {
                        binding.videoListRecyclerView.visible()
                        emptyListBinding.root.invisible()

                        videoAdapter.submitList(videosList)

                    } else {
                        binding.videoListRecyclerView.invisible()
                        emptyListBinding.root.visible()
                    }
                }
            }
        }
    }

    private fun initEmptyLayoutTexts() {
        emptyListBinding.apply {
            tvDescription.text = getString(R.string.empty_video_desc)
            tvTitle.text = getString(R.string.empty_video)
        }
    }

    override fun deleteVideo(video: FBVideo) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_delete_video)
            .setIcon(R.drawable.ic_delete)
            .setMessage(R.string.prompt_delete_video)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                mainViewModel.deleteVideo(video)
                File(video.path).delete()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                videoAdapter.notifyDataSetChanged()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutCoordinator = null
    }
}