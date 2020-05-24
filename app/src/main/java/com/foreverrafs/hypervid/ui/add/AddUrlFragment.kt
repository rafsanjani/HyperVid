package com.foreverrafs.hypervid.ui.add

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.extractor.DownloadableFile
import com.foreverrafs.extractor.FacebookExtractor
import com.foreverrafs.hypervid.R
import com.foreverrafs.hypervid.model.FBVideo
import com.foreverrafs.hypervid.ui.MainActivity
import com.foreverrafs.hypervid.ui.MainViewModel
import com.foreverrafs.hypervid.util.EspressoIdlingResource
import com.foreverrafs.hypervid.util.disable
import com.foreverrafs.hypervid.util.enable
import com.foreverrafs.hypervid.util.showToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_addurl.*
import kotlinx.android.synthetic.main.fragment_addurl.tabLayout
import kotlinx.coroutines.Job
import timber.log.Timber

class AddUrlFragment : Fragment(R.layout.fragment_addurl) {
    companion object {
        const val FACEBOOK_URL = "https://www.facebook.com/"
        const val FACEBOOK_URL_MOBILE = "https://m.facebook.com/"
    }

    private lateinit var clipboardText: String
    private var clipBoardData: ClipData? = null
    private val vm: MainViewModel by activityViewModels()

    private var downloadList = mutableListOf<DownloadInfo>()
    private var videoList = mutableListOf<FBVideo>()


    private val suggestedLinks = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeViews()

        vm.downloadList.observe(viewLifecycleOwner, Observer {
            downloadList = it.toMutableList()
        })

        vm.videosList.observe(viewLifecycleOwner, Observer {
            videoList = it.toMutableList()
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
                if (clipboardText.contains(FACEBOOK_URL))
                    urlInputLayout.editText?.setText(clipboardText)
            }
        }

        //add the download job to the download list when the button is clicked. We don't start downloading
        //immediately. We wait for the user to interact with it in the downloads section before we download.
        btnAddToDownloads.setOnClickListener {
            extractVideo(urlInputLayout.editText?.text.toString())
        }

        urlInputLayout.editText?.addTextChangedListener {
            it?.let {
                if (isValidUrl(it.toString())) {
                    btnAddToDownloads.enable()
                    urlInputLayout.isErrorEnabled = false
                } else {
                    btnAddToDownloads.disable()
                    urlInputLayout.isErrorEnabled = true
                }
            }

        }
    }

    private fun isNotExtracted(url: String) =
        !suggestedLinks.contains(url) && !vm.hasVideo(url) && !vm.hasDownload(
            url
        )

    private lateinit var job: Job

    private fun extractVideo(videoURL: String) {
        EspressoIdlingResource.increment()

        btnAddToDownloads.text = getString(R.string.extracting)
        btnAddToDownloads.disable()
        urlInputLayout.disable()

        job = vm.extractVideoDownloadUrl(
            videoURL,
            listener
        )

        job.invokeOnCompletion {
            Timber.i(it)
        }
    }

    private var listener = object : FacebookExtractor.ExtractionEvents {
        override fun onComplete(downloadableFile: DownloadableFile) {
            val downloadInfo = DownloadInfo(
                downloadableFile.url,
                0,
                downloadableFile.author,
                downloadableFile.duration
            )

            val downloadExists = downloadList.any {
                it.url == downloadInfo.url
            } || videoList.any { it.url == downloadInfo.url }

            if (downloadExists) {
                Timber.e("Download exists. Unable to add to list")
                showToast("Link already extracted")
                resetUi()
                return
            }


            Timber.d("Download URL extraction complete: Adding to List: $downloadInfo")
            showToast("Video added to download queue...")
            vm.saveDownload(downloadInfo)


            resetUi()

            Handler().postDelayed({
                (requireActivity() as MainActivity).viewPager.currentItem = 1

                EspressoIdlingResource.decrement()
            }, 2000)
        }

        override fun onError(exception: Exception) {
            EspressoIdlingResource.decrement()

            showToast("Error loading video from link")
            urlInputLayout.isErrorEnabled = true
            urlInputLayout.error = "Invalid Link"

            btnAddToDownloads.text = getString(R.string.add_to_downloads)
            btnAddToDownloads.enable()
            urlInputLayout.enable()

            Timber.e(exception)
        }
    }

    private fun resetUi() {
        btnPaste.text = getString(R.string.paste_link)
        urlInputLayout.editText?.text?.clear()

        btnAddToDownloads.text = getString(R.string.add_to_downloads)
        btnAddToDownloads.enable()
        urlInputLayout.enable()
        btnAddToDownloads.enable()
        EspressoIdlingResource.decrement()
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
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
            val link = it.toString()

            if (isValidUrl(link) && isNotExtracted(link)) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.title_download_video)
                    .setMessage(getString(R.string.prompt_download_video))
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        urlInputLayout.editText?.setText(link)
                        extractVideo(link)
                        suggestedLinks.add(link)
                    }
                    .setNegativeButton(
                        android.R.string.no
                    ) { _, _ ->
                        suggestedLinks.add(link)
                    }
                    .show()
            } //Added same method for facebook mobile url
            else if (link.contains(FACEBOOK_URL_MOBILE) && isNotExtracted(link)) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.title_download_video)
                    .setMessage(getString(R.string.prompt_download_video))
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        urlInputLayout.editText?.setText(link)
                        extractVideo(link)
                        suggestedLinks.add(link)
                    }
                    .setNegativeButton(
                        android.R.string.no
                    ) { _, _ ->
                        suggestedLinks.add(link)
                    }
                    .show()
            } else {
                Timber.i("Clipboard link has already been downloaded. Suggestion discarded")
            }
        }


        clipboardManager.addPrimaryClipChangedListener {
            clipBoardData = clipboardManager.primaryClip
            val clipText = clipBoardData?.getItemAt(0)?.text

            clipText?.let {
                if (isValidUrl(it.toString()))
                    urlInputLayout?.editText?.setText(clipText.toString())

            } ?: Timber.e("clipText is null")
        }
    }

    private fun isValidUrl(url: String) =
        url.contains(FACEBOOK_URL) or url.contains(FACEBOOK_URL_MOBILE)
}