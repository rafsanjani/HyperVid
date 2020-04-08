package com.foreverrafs.hyperdownloader.ui.add

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.extractor.DownloadableFile
import com.foreverrafs.extractor.FacebookExtractor
import com.foreverrafs.hyperdownloader.MainViewModel
import com.foreverrafs.hyperdownloader.R
import com.foreverrafs.hyperdownloader.adapter.SlideShowAdapter
import com.foreverrafs.hyperdownloader.model.FacebookVideo
import com.foreverrafs.hyperdownloader.util.disable
import com.foreverrafs.hyperdownloader.util.enable
import com.foreverrafs.hyperdownloader.util.showToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_addurl.*
import timber.log.Timber

class AddUrlFragment : Fragment(R.layout.fragment_addurl) {
    private lateinit var clipboardText: String
    private var clipBoardData: ClipData? = null
    private val vm: MainViewModel by activityViewModels()
    private var downloadList = mutableListOf<DownloadInfo>()
    private var videoList = mutableListOf<FacebookVideo>()
    private val suggestedLinks = mutableListOf<String>()


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

        vm.downloadList.observe(viewLifecycleOwner, Observer {
            this.downloadList = it.toMutableList()
        })

        vm.videosList.observe(viewLifecycleOwner, Observer {
            this.videoList = it.toMutableList()
        })

        initSlideShow()
    }

    private fun initSlideShow() {
        val adapter = SlideShowAdapter()
        slideShowPager.adapter = adapter

        TabLayoutMediator(tabLayout, slideShowPager) { tab, position ->
            tab.text = "${position + 1}"

        }.attach()

        timer.start()
    }

    private val timer = object : CountDownTimer(60 * 1000, 5 * 1000) {
        override fun onTick(millisUntilFinished: Long) {
            slideShowPager?.let {
                if (slideShowPager.currentItem + 1 <= 2)
                    slideShowPager.currentItem = slideShowPager.currentItem + 1
                else
                    slideShowPager.currentItem = 0
            }
        }

        override fun onFinish() {
            Timber.i("Stopping slideshow")
        }
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

        val listener = object : FacebookExtractor.ExtractionEvents {
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
        }

        vm.extractVideoDownloadUrl(
            videoURL,
            listener
        )
    }

    private fun resetUi() {
        btnPaste.text = getString(R.string.paste_link)
        urlInputLayout.editText?.text?.clear()


        btnAddToDownloads.text = getString(R.string.add_to_downloads)
        btnAddToDownloads.enable()
        urlInputLayout.enable()
        btnAddToDownloads.enable()
    }

    override fun onResume() {
        super.onResume()
        initializeClipboard()
    }

    private fun initializeClipboard() {
        val clipboardManager =
            activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipBoardData = clipboardManager.primaryClip

        clipBoardData?.getItemAt(0)?.text?.let {
            if (it.contains(FACEBOOK_URL) && !suggestedLinks.contains(it) /*&& vm.hasVideo(it.toString())*/) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.title_download_video)
                    .setMessage(getString(R.string.prompt_dowload_video))
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        val link = it.toString()
                        urlInputLayout.editText?.setText(link)
                        extractVideo(link)
                        suggestedLinks.add(link)
                    }
                    .setNegativeButton(
                        android.R.string.no,
                        null
                    ) //todo: add to suggested links even when discarded
                    .show()
            } else {
                Timber.i("Clipboard link has already been downloaded. Suggestion discarded")
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