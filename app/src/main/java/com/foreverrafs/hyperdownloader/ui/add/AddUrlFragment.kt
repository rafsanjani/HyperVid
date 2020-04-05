package com.foreverrafs.hyperdownloader.ui.add

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.extractor.DownloadableFile
import com.foreverrafs.extractor.FacebookExtractor
import com.foreverrafs.hyperdownloader.MainViewModel
import com.foreverrafs.hyperdownloader.R
import com.foreverrafs.hyperdownloader.util.disable
import com.foreverrafs.hyperdownloader.util.enable
import com.foreverrafs.hyperdownloader.util.showToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_addurl.*
import timber.log.Timber

class AddUrlFragment private constructor() : Fragment(R.layout.fragment_addurl) {
    private lateinit var clipboardText: String
    private var clipBoardData: ClipData? = null
    private val vm: MainViewModel by activityViewModels()
    private var downloadList: MutableList<DownloadInfo> = mutableListOf()


    companion object {
        const val FACEBOOK_URL = "https://www.facebook.com/"
        private lateinit var pageNavigator: (pageNumber: Int) -> Boolean

        fun newInstance(listener: (pageNumber: Int) -> Boolean = { true }): AddUrlFragment {
            this.pageNavigator = listener
            return AddUrlFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeViews()
        initializeClipboard()

        vm.downloadList.observe(viewLifecycleOwner, Observer {
            this.downloadList = it.toMutableList()
        })
    }

    private fun initializeViews() {
        btnPaste.setOnClickListener {
            if (clipBoardData != null) {
                clipboardText = clipBoardData?.getItemAt(0)?.text.toString()
                urlInputLayout.editText?.setText(clipboardText)
            }
        }

        //add the download job to the download list when the button is clicked. We don't start downloading
        //immediately. We wait for the user to interact with it in the downloads section before we download.
        btnAddToDownloads.setOnClickListener {
            extractVideo(urlInputLayout.editText?.text.toString())
        }
    }

    private fun extractVideo(videoURL: String) {
        btnAddToDownloads.text = getString(R.string.extracting)
        btnAddToDownloads.disable()
        urlInputLayout.disable()

        vm.extractVideoDownloadUrl(
            videoURL,
            object : FacebookExtractor.ExtractionEvents {
                override fun onComplete(downloadableFile: DownloadableFile) {
                    val downloadInfo = DownloadInfo(
                        downloadableFile.url,
                        0,
                        downloadableFile.author,
                        downloadableFile.duration
                    )


                    val downloadExists = downloadList.any {
                        it.url == downloadInfo.url
                    }

                    if (downloadExists) {
                        Timber.e("Download exists. Unable to add to list")
                        showToast("Link already extracted")
                        resetUi()
                        return
                    }


                    Timber.d("Download URL extraction complete: Adding to List: $downloadInfo")
                    showToast("Video added to download queue...")

                    downloadList.add(downloadInfo)
                    vm.setDownloadList(downloadList)

                    resetUi()
                    pageNavigator(1)
                }

                override fun onError(exception: Exception) {
                    Timber.e(exception)
                    showToast("Error loading video from link")
                    urlInputLayout.isErrorEnabled = true

                    btnAddToDownloads.text = getString(R.string.add_to_downloads)
                    btnAddToDownloads.enable()
                    urlInputLayout.enable()
                }
            })
    }

    private fun resetUi() {
        btnPaste.text = getString(R.string.paste_link)
        urlInputLayout.editText?.text?.clear()


        btnAddToDownloads.text = getString(R.string.add_to_downloads)
        btnAddToDownloads.enable()
        urlInputLayout.enable()
        btnAddToDownloads.enable()
    }

    private fun initializeClipboard() {
        val clipboardManager =
            activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipBoardData = clipboardManager.primaryClip

        clipBoardData?.getItemAt(0)?.text?.let {
            if (it.contains(FACEBOOK_URL)) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Download Video?")
                    .setMessage("Video found. Download now?")
                    .setPositiveButton("Yes") { _, _ ->
                        urlInputLayout.editText?.setText(it.toString())
                        extractVideo(it.toString())
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }


        clipboardManager.addPrimaryClipChangedListener {
            clipBoardData = clipboardManager.primaryClip
            val clipText = clipBoardData?.getItemAt(0)?.text

            clipText?.let {
                if (it.contains(FACEBOOK_URL))
                    urlInputLayout.editText?.setText(clipText.toString())
            } ?: Timber.e("clipText is null")
        }
    }
}