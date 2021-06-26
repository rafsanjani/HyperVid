package com.foreverrafs.hypervid.ui.downloads

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.foreverrafs.downloader.downloader.VideoDownloader
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.hypervid.R
import com.foreverrafs.hypervid.analytics.Analytics
import com.foreverrafs.hypervid.analytics.events.DownloadVideoEvent
import com.foreverrafs.hypervid.data.repository.AppRepository
import com.foreverrafs.hypervid.databinding.FragmentDownloadsBinding
import com.foreverrafs.hypervid.databinding.ListEmptyBinding
import com.foreverrafs.hypervid.model.FBVideo
import com.foreverrafs.hypervid.ui.MainViewModel
import com.foreverrafs.hypervid.ui.TabLayoutCoordinator
import com.foreverrafs.hypervid.ui.states.DownloadListState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import invisible
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import visible
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class DownloadsFragment : Fragment(R.layout.fragment_downloads),
    DownloadAdapter.VideoDownloadEvents {
    @Inject
    lateinit var videoDownloader: VideoDownloader

    @Inject
    lateinit var analytics: Analytics

    private val downloadsAdapter by lazy {
        DownloadAdapter(downloadEventsListener = this, videoDownloader = videoDownloader)
    }

    private val viewModel: MainViewModel by activityViewModels()

    private val binding by viewBinding(FragmentDownloadsBinding::bind)

    private lateinit var emptyListBinding: ListEmptyBinding

    @Inject
    lateinit var repository: AppRepository

    companion object {
        var tabLayoutCoordinator: TabLayoutCoordinator? = null

        fun newInstance(tabLayoutCoordinator: TabLayoutCoordinator): DownloadsFragment {
            this.tabLayoutCoordinator = tabLayoutCoordinator
            return DownloadsFragment()
        }
    }

    private fun showEmptyScreen() {
        binding.downloadListRecyclerView.invisible()
        emptyListBinding.root.visible()
        binding.progressBar.invisible()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        emptyListBinding = binding.layoutEmpty
        binding.downloadListRecyclerView.adapter = downloadsAdapter

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.downloadState.collect { state ->
                    when (state) {
                        is DownloadListState.DownloadList -> {
                            if (state.downloads.isNotEmpty()) {
                                emptyListBinding.root.invisible()
                                showDownloads(state.downloads)
                            } else {
                                showEmptyScreen()
                            }
                        }
                        is DownloadListState.Error -> {
                            Timber.e(state.exception)
                        }
                        DownloadListState.Loading -> {
                            Timber.d("loading downloadstate")
                        }
                    }
                }
            }
        }
    }

    private fun showDownloads(downloadList: List<DownloadInfo>) {
        binding.progressBar.invisible()
        binding.downloadListRecyclerView.visible()

        downloadsAdapter.submitList(downloadList)
    }

    /**
     * when a video has been successfully downloaded from the list. The adapter position of the downloaded
     * video together with the video item downloaded are received
     */
    override fun onVideoDownloaded(position: Int, video: FBVideo, download: DownloadInfo) {
        analytics.trackEvent(
            DownloadVideoEvent(
                title = video.title,
                url = download.originalUrl
            )
        )
        saveVideoToGallery(video)
        viewModel.saveVideo(video)

        // delete the download from the download list [Downloads]
        lifecycleScope.launch {
            viewModel.deleteDownload(download)
        }

        tabLayoutCoordinator?.navigateToTab(2)
    }

    @RequiresApi(29)
    private fun saveVideoToGalleryQ(videoFile: File, title: String): Uri? {
        val contentResolver = activity?.contentResolver

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DCIM + videoFile.path
            )
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
            put(MediaStore.MediaColumns.DISPLAY_NAME, title)
            put(MediaStore.MediaColumns.TITLE, title)
        }

        val uri = contentResolver?.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)

        contentResolver?.let {
            val stream = contentResolver.openOutputStream(uri!!)

            val fileStream = FileInputStream(videoFile)

            stream?.apply {
                write(fileStream.readBytes())
                close()
            }

            fileStream.close()

            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            val update =
                contentResolver.update(uri, values, null, null)

            Timber.d(update.toString())

            return uri
        } ?: return null
    }

    @RequiresApi(21)
    private fun saveVideoToGallery(video: FBVideo): Uri? {
        var uri: Uri? = null
        try {
            uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveVideoToGalleryQ(File(video.path), video.title)
            } else {
                saveVideoToGallery(File(video.path), video.title)
            }
        } catch (exception: IOException) {
            Timber.e(exception)
        }

        Timber.i("Saved Video to Gallery, Uri: $uri")
        return uri
    }

    @RequiresApi(21)
    @Suppress("DEPRECATION")
    private fun saveVideoToGallery(videoFile: File, title: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, title)
            put(MediaStore.Images.Media.DISPLAY_NAME, title)
            put(MediaStore.Images.Media.DESCRIPTION, "description")
            put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
            put(MediaStore.Images.Media.DATA, videoFile.toString())
        }

        val uri = requireActivity().contentResolver.insert(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            values
        )

        Timber.i("Saved Video to Gallery, Uri: $uri")
        return uri
    }

    /**
     * When a download item is removed via the adapter, an event is propagated back to the fragment which is
     * used to resolve the UI
     */
    override fun deleteDownload(download: DownloadInfo) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_delete_video)
            .setIcon(R.drawable.ic_delete)
            .setMessage(R.string.prompt_delete_download)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteDownload(download)
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onDownloadError(position: Int) {
        // NO-OP
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutCoordinator = null
    }
}
