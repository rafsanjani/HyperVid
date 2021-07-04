package com.foreverrafs.hypervid.ui.add

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.extractor.Downloadable
import com.foreverrafs.extractor.Extractor
import com.foreverrafs.hypervid.R
import com.foreverrafs.hypervid.databinding.FragmentAddurlBinding
import com.foreverrafs.hypervid.model.FBVideo
import com.foreverrafs.hypervid.ui.MainViewModel
import com.foreverrafs.hypervid.ui.TabLayoutCoordinator
import com.foreverrafs.hypervid.ui.states.DownloadListState
import com.foreverrafs.hypervid.ui.states.VideoListState
import com.foreverrafs.hypervid.util.EspressoIdlingResource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import disable
import enable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import showToast
import timber.log.Timber
import java.net.URI
import java.net.UnknownHostException

class AddUrlFragment : Fragment(R.layout.fragment_addurl) {
    companion object {
        const val FACEBOOK_URL = "https://www.facebook.com/"
        const val FACEBOOK_URL_MOBILE = "https://m.facebook.com/"
        const val FACEBOOK_URL_SHORT = "https://fb.watch/"

        var tabLayoutCoordinator: TabLayoutCoordinator? = null

        fun newInstance(tabLayoutCoordinator: TabLayoutCoordinator): AddUrlFragment {
            this.tabLayoutCoordinator = tabLayoutCoordinator
            return AddUrlFragment()
        }
    }

    private lateinit var clipboardText: String
    private lateinit var clipboardManager: ClipboardManager

    private var clipBoardData: ClipData? = null
    private val mainViewModel: MainViewModel by activityViewModels()

    private var downloadList = listOf<DownloadInfo>()
    private var videoList = listOf<FBVideo>()

    private val suggestedLinks = mutableListOf<String>()

    private val binding by viewBinding(FragmentAddurlBinding::bind)

    private val dismissDialog: AlertDialog by lazy {
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage("This app cannot function properly without allowing all the requested permissions")
            .setIcon(R.drawable.ic_error)
            .setPositiveButton(R.string.exit) { _, _ ->
                requireActivity().finish()
            }
            .create()
    }

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                extractVideo(binding.urlInputLayout.editText?.text.toString())
            } else {
                dismissDialog.show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeViews()

        if (mainViewModel.isFirstRun) {
            showDisclaimer()
        }

        mainViewModel.videosListState.collectWhenStarted { videoListState ->
            when (videoListState) {
                is VideoListState.Error -> {
                    Timber.e(videoListState.exception)
                }
                VideoListState.Loading -> {
                    Timber.d("Video state loading")
                }
                is VideoListState.Videos -> {
                    videoList = videoListState.videos
                }
            }
        }

        mainViewModel.downloadState.collectWhenStarted { downloadState ->
            when (downloadState) {
                is DownloadListState.DownloadList -> {
                    downloadList = downloadState.downloads.toMutableList()
                }
                is DownloadListState.Error -> {
                    Timber.e(downloadState.exception)
                }
                DownloadListState.Loading -> {
                    Timber.d("Download state loading")
                }
            }
        }

        initSlideShow()
    }

    inline fun <T> Flow<T>.collectWhenStarted(crossinline action: suspend (value: T) -> Unit) {
        val flow = this

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect {
                    action(it)
                }
            }
        }
    }

    private fun initSlideShow() = with(binding) {
        val adapter = SlideShowAdapter()
        slideShowPager.adapter = adapter

        TabLayoutMediator(tabLayout, slideShowPager) { tab, position ->
            tab.text = "${position + 1}"
        }.attach()

        timer.start()
    }

    private val timer = object : CountDownTimer(60 * 1000L, 5 * 1000L) {
        override fun onTick(millisUntilFinished: Long) = with(binding) {
            slideShowPager.let {
                if (slideShowPager.currentItem + 1 <= 2) {
                    slideShowPager.currentItem = slideShowPager.currentItem + 1
                } else {
                    slideShowPager.currentItem = 0
                }
            }
        }

        override fun onFinish() {
            Timber.i("Stopping slideshow")
        }
    }

    private fun initializeViews() = with(binding) {
        btnPaste.setOnClickListener {
            if (clipBoardData != null) {
                clipboardText = clipBoardData?.getItemAt(0)?.text.toString()
                if (clipboardText.contains(FACEBOOK_URL)) {
                    urlInputLayout.editText?.setText(clipboardText)
                }
            }
        }

        // add the download job to the download list when the button is clicked. We don't start downloading
        // immediately. We wait for the user to interact with it in the downloads section before we download.
        btnAddToDownloads.setOnClickListener {
            requestStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        urlInputLayout.editText?.doAfterTextChanged {
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

    private fun downloadExist(url: String): Boolean =
        downloadList.any { URI.create(url).path == URI.create(it.url).path }

    private fun videoExist(url: String): Boolean =
        videoList.any { URI.create(url).path == URI.create(it.downloadUrl).path }

    private fun isExtracted(url: String): Boolean = downloadExist(url) || videoExist(url)

    private fun extractVideo(videoURL: String) {
        EspressoIdlingResource.increment()
        if (downloadList.any { it.url == videoURL }) {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage("Video already extracted")
                .setTitle("Download exists")
                .setPositiveButton("OK", null)
                .show()

            return
        }

        with(binding) {
            btnAddToDownloads.text = getString(R.string.extracting)
            btnAddToDownloads.disable()
            urlInputLayout.disable()
        }

        mainViewModel.extractVideoDownloadUrl(
            videoURL,
            extractionListener
        ).invokeOnCompletion {
            Timber.i(it)
        }
    }

    private var extractionListener = object : Extractor.ExtractionEvents {
        override fun onComplete(downloadable: Downloadable) {
            val downloadInfo = DownloadInfo(
                url = downloadable.downloadUrl,
                name = downloadable.filename,
                downloadId = 0,
                originalUrl = downloadable.originalUrl
            )

            // Check if the extracted link exists either in the download list or the videos list.
            if (isExtracted(downloadable.downloadUrl)) {
                Timber.e("Download exists. Unable to add to list")
                showToast("Link already extracted")
                resetUi()
                return
            }

            Timber.d("Download URL extraction complete: Adding to List: $downloadInfo")
            showToast("Video added to download queue...")
            mainViewModel.saveDownload(downloadInfo)
            // TODO: 17/06/2021 add analytics here

            resetUi()

            tabLayoutCoordinator?.navigateToTab(1)
        }

        override fun onError(error: Exception) = with(binding) {
            EspressoIdlingResource.decrement()
            val noInternet = error is UnknownHostException

            showToast(if (noInternet) "No Internet connection" else "Error loading video from link")
            urlInputLayout.isErrorEnabled = true
            urlInputLayout.error = if (noInternet) "No internet connection" else "Invalid Link"

            btnAddToDownloads.text = getString(R.string.add_to_downloads)
            btnAddToDownloads.enable()
            urlInputLayout.enable()

            Timber.e(error)
        }
    }

    private fun resetUi() = with(binding) {
        btnPaste.text = getString(R.string.paste_link)
        urlInputLayout.editText?.text?.clear()

        btnAddToDownloads.text = getString(R.string.add_to_downloads)
        btnAddToDownloads.enable()
        urlInputLayout.enable()
        EspressoIdlingResource.decrement()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        tabLayoutCoordinator = null
        if (::clipboardManager.isInitialized) {
            clipboardManager.removePrimaryClipChangedListener { clipBoardListener }
        }
    }

    override fun onResume() {
        super.onResume()
        if (dismissDialog.isShowing) {
            return
        }

        if (mainViewModel.isFirstRun) {
            return
        }

        initializeClipboard()
    }

    private val clipBoardListener = ClipboardManager.OnPrimaryClipChangedListener {
        clipBoardData = clipboardManager.primaryClip
        val clipText = clipBoardData?.getItemAt(0)?.text

        clipText?.let {
            if (isValidUrl(it.toString())) {
                binding.urlInputLayout.editText?.setText(clipText.toString())
            }
        } ?: Timber.e("clipText is null")
    }

    private fun initializeClipboard() {
        clipboardManager = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipBoardData = clipboardManager.primaryClip

        clipBoardData?.getItemAt(0)?.text?.let {
            val link = it.toString()
            if (suggestedLinks.contains(link)) {
                Timber.d("Link already in suggestion list, ignoring")
                return@let
            }

            if (isValidUrl(link) && !isExtracted(link)) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.title_download_video)
                    .setMessage(getString(R.string.prompt_download_video))
                    .setPositiveButton(R.string.yes) { _, _ ->
                        binding.urlInputLayout.editText?.setText(link)
                        requestStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        suggestedLinks.add(link)
                    }
                    .setNegativeButton(
                        R.string.no
                    ) { _, _ ->
                        suggestedLinks.add(link)
                    }
                    .show()
            } else {
                Timber.i("Clipboard link has already been downloaded. Suggestion discarded")
            }
        }

        clipboardManager.addPrimaryClipChangedListener {
            clipBoardListener
        }
    }

    private fun showDisclaimer() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(
                getString(R.string.message_copyright_notice)
            )
            .setTitle(getString(R.string.title_copyright_notice))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mainViewModel.isFirstRun = false
                    initializeClipboard()
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                requireActivity().finish()
            }.show()
    }

    private fun isValidUrl(url: String) =
        url.contains(FACEBOOK_URL) or url.contains(FACEBOOK_URL_MOBILE) or url.contains(
            FACEBOOK_URL_SHORT
        )
}
