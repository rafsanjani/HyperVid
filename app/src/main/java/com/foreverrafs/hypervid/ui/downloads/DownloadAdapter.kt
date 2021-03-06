package com.foreverrafs.hypervid.ui.downloads

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.foreverrafs.downloader.downloader.DownloadEvents
import com.foreverrafs.downloader.downloader.DownloadException
import com.foreverrafs.downloader.downloader.VideoDownloader
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.hypervid.R
import com.foreverrafs.hypervid.databinding.ItemDownloadBinding
import com.foreverrafs.hypervid.model.FBVideo
import com.foreverrafs.hypervid.util.getDurationString
import gone
import invisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import load
import timber.log.Timber
import visible
import java.text.SimpleDateFormat
import java.util.*

class DownloadAdapter constructor(
    private val downloadEventsListener: VideoDownloadEvents,
    private val videoDownloader: VideoDownloader
) :
    RecyclerView.Adapter<DownloadAdapter.DownloadsViewHolder>() {

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val diffCallback = object : DiffUtil.ItemCallback<DownloadInfo>() {
        override fun areContentsTheSame(oldItem: DownloadInfo, newItem: DownloadInfo): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: DownloadInfo, newItem: DownloadInfo): Boolean {
            return oldItem.url == newItem.url
        }
    }

    private val downloadsListDiffer = AsyncListDiffer(this, diffCallback)

    private lateinit var context: Context

    fun submitList(newList: List<DownloadInfo>) {
        downloadsListDiffer.submitList(newList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadsViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)

        return DownloadsViewHolder(
            ItemDownloadBinding.inflate(inflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: DownloadsViewHolder, position: Int) {
        val downloadItem = downloadsListDiffer.currentList[position]
        holder.bind(downloadItem)
    }

    inner class DownloadsViewHolder(private val binding: ItemDownloadBinding) :
        RecyclerView.ViewHolder(binding.root), DownloadEvents {

        private lateinit var downloadItem: DownloadInfo
        private var isDownloading: Boolean = false

        private var downloadId: Int = 0

        fun bind(downloadItem: DownloadInfo) = with(binding) {
            this@DownloadsViewHolder.downloadItem = downloadItem

            val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.ROOT)
            val downloadDate = Date(downloadItem.dateAdded)
            val retriever = MediaMetadataRetriever()

            tvDate.text = formatter.format(downloadDate)

            coroutineScope.launch {
                var coverArt: Bitmap? = null
                var duration = ""

                runCatching {
                    retriever.setDataSource(downloadItem.url, HashMap())
                    duration = getDurationString(
                        retriever.extractMetadata(METADATA_KEY_DURATION)!!.toLong()
                    )

                    coverArt = retriever.frameAtTime
                }.onSuccess {
                    withContext(Dispatchers.Main.immediate) {
                        with(binding) {
                            coverArt?.let { image.load(it) }

                            tvDuration.text = duration
                            tvDuration.visible()
                        }
                    }
                }.onFailure { throwable ->
                    Timber.e(throwable)
                }
            }

            val videoTitle = downloadItem.name

            tvName.text = videoTitle
            tvStatus.text = context.getString(R.string.ready)

            tvMenu.setOnClickListener {
                openPopupMenu()
            }

            btnStartPause.setOnClickListener {
                toggleDownload()
            }
        }

        private fun toggleDownload() {
            if (isDownloading) {
                pauseDownload()
            } else {
                startDownload()
            }
        }

        private fun openPopupMenu() {
            val popupMenu = PopupMenu(context, binding.tvMenu)
            popupMenu.menuInflater.inflate(R.menu.download_menu, popupMenu.menu)

            if (downloadItem.isCompleted) {
                popupMenu.menu.removeItem(R.id.startPause)
                popupMenu.menu.removeItem(R.id.stop)
            }

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.startPause -> {
                        toggleDownload()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.delete -> {
                        downloadEventsListener.deleteDownload(downloadsListDiffer.currentList[bindingAdapterPosition])
                        return@setOnMenuItemClickListener true
                    }
                    R.id.stop -> {
                        stopDownload()
                        return@setOnMenuItemClickListener true
                    }

                    else -> {
                        Timber.d("Nothing to do hoss")
                        return@setOnMenuItemClickListener true
                    }
                }
            }

            popupMenu.show()
        }

        private fun stopDownload() {
            videoDownloader.cancelDownload(downloadId)
        }

        override fun onProgressChanged(downloaded: Long, percentage: Int) = with(binding) {
            tvPercentage.text =
                context.getString(R.string.percentage, percentage)
            progressDownload.progress = percentage

            val downloadedMB = (downloaded.toDouble() / 1024 / 1024)

            tvDownloadedSize.text =
                if (downloadedMB.toInt() > 0) "${downloadedMB.toInt()}  MB" else "${(downloadedMB * 1024).toInt()} KB"
        }

        override fun onPause() = with(binding) {
            isDownloading = false
            progressDownload.visible()
            tvStatus.text = context.getString(R.string.paused)
            btnStartPause.setImageResource(R.drawable.ic_start)
        }

        override fun onCompleted(path: String): Unit = with(binding) {
            val facebookVideo = FBVideo(
                title = downloadItem.name,
                path = path,
                downloadUrl = downloadItem.url,
                originalUrl = downloadItem.originalUrl
            )

            downloadEventsListener.onVideoDownloaded(
                bindingAdapterPosition,
                facebookVideo,
                downloadItem
            )

            btnStartPause.setImageResource(R.drawable.ic_start)
            btnStartPause.gone()
            tvStatus.text = context.getString(R.string.completed)
            isDownloading = false
            downloadItem.isCompleted = true

            animateLayoutChanges()
        }

        override fun onError(error: DownloadException) = with(binding) {
            Timber.e(error)
            isDownloading = false
            progressDownload.invisible()
            tvStatus.text = context.getString(R.string.failed)
            btnStartPause.setImageResource(R.drawable.ic_start)
        }

        override fun onCancelled() = with(binding) {
            isDownloading = false
            progressDownload.invisible()
            btnStartPause.setImageResource(R.drawable.ic_start)
            tvStatus.text = context.getString(R.string.cancelled)
        }

        override fun onWaitingForNetwork() = with(binding) {
            Timber.i("Waiting for network")
            isDownloading = false
            progressDownload.visible()
            btnStartPause.setImageResource(R.drawable.ic_start)
            tvStatus.text = context.getString(R.string.connecting)
        }

        override fun onStart() = with(binding) {
            isDownloading = true
            progressDownload.visible()
            btnStartPause.setImageResource(R.drawable.ic_pause)
            tvStatus.text = context.getString(R.string.downloading)
        }

        private fun startDownload() {
            binding.btnStartPause.setImageResource(R.drawable.ic_pause)
            downloadId = videoDownloader.downloadFile(downloadItem, this@DownloadsViewHolder)
        }

        private fun animateLayoutChanges() = with(binding) {
            TransitionManager.beginDelayedTransition(itemView as ViewGroup)
            val constraintSet = ConstraintSet()

            constraintSet.apply {
                clone(constraintLayout)
                connect(
                    R.id.tvDownloadedSize,
                    ConstraintSet.TOP,
                    R.id.tvDate,
                    ConstraintSet.TOP
                )
                applyTo(constraintLayout)
            }
        }

        private fun pauseDownload() {
            binding.btnStartPause.setImageResource(R.drawable.ic_start)
            videoDownloader.pauseDownload(downloadId)
        }
    }

    interface VideoDownloadEvents {
        fun onVideoDownloaded(position: Int, video: FBVideo, download: DownloadInfo)
        fun deleteDownload(download: DownloadInfo)
        fun onDownloadError(position: Int)
    }

    override fun getItemCount(): Int = downloadsListDiffer.currentList.size
}
