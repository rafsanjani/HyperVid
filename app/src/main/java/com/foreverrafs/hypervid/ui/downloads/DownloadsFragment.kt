package com.foreverrafs.hypervid.ui.downloads

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.hypervid.MainViewModel
import com.foreverrafs.hypervid.R
import com.foreverrafs.hypervid.databinding.FragmentDownloadsBinding
import com.foreverrafs.hypervid.databinding.ListEmptyBinding
import com.foreverrafs.hypervid.model.FBVideo
import com.foreverrafs.hypervid.util.invisible
import com.foreverrafs.hypervid.util.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_downloads.*
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class DownloadsFragment : Fragment(), DownloadAdapter.Interaction {
    private val downloadsAdapter = DownloadAdapter(this)

    private val vm: MainViewModel by activityViewModels()
    private var videosList = mutableListOf<FBVideo>()

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
            emptyListBinding.root.invisible()

            Handler().postDelayed({
                showDownloads(list)
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

        vm.videosList.observe(viewLifecycleOwner, Observer {
            videosList = it.toMutableList()
        })
    }

    private fun showDownloads(downloadList: List<DownloadInfo>) {
        progressBar.invisible()
        downloadBinding.downloadListRecyclerView.visible()

        downloadsAdapter.submitList(downloadList.toMutableList())
    }


    /**
     * when a video has been successfully downloaded from the list. The adapter position of the downloaded
     * video together with the video item downloaded are received
     */
    override fun onVideoDownloaded(position: Int, video: FBVideo, download: DownloadInfo) {
        val downloadExists = videosList.any {
            it.path == video.path
        }

        if (!downloadExists) {
            saveVideoToGallery(video)
            vm.saveVideo(video)

            Handler().postDelayed({
                vm.deleteDownload(download)
            }, 800)


            //navigate to the videos page : 2
            pageNavigator(2)
        } else {
            Toast.makeText(requireContext(), getString(R.string.duplcate_video), Toast.LENGTH_SHORT)
                .show()
        }
    }

    @RequiresApi(29)
    private fun saveVideoToGalleryQ(videoFile: File, title: String): Uri? {
        val contentResolver = activity?.contentResolver

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + videoFile.path)
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
            uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                saveVideoToGalleryQ(File(video.path), video.title)
            else
                saveVideoToGallery(File(video.path), video.title)
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
                vm.deleteDownload(download)
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    override fun onDownloadError(position: Int) {
        //NO-OP
    }
}