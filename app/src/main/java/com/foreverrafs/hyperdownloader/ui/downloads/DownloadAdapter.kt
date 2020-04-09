package com.foreverrafs.hyperdownloader.ui.downloads

import android.content.Context
import android.media.MediaMetadataRetriever
import android.text.Html
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.foreverrafs.downloader.downloader.DownloadEvents
import com.foreverrafs.downloader.downloader.DownloadException
import com.foreverrafs.downloader.downloader.VideoDownloader
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.hyperdownloader.R
import com.foreverrafs.hyperdownloader.model.FacebookVideo
import com.foreverrafs.hyperdownloader.util.*
import kotlinx.android.synthetic.main.item_download__.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs


class DownloadAdapter(val interaction: Interaction) :
    RecyclerView.Adapter<DownloadAdapter.DownloadsViewHolder>() {


    private lateinit var context: Context
    private val videoDownloader: VideoDownloader by lazy {
        VideoDownloader.getInstance(context)!!
    }

    val downloads = mutableListOf<DownloadInfo>()

    fun submitList(newList: List<DownloadInfo>) {
        val diffCallback =
            DownloadDiffCallback(
                this.downloads,
                newList
            )

        val diffResult = DiffUtil.calculateDiff(diffCallback)

        downloads.clear()
        downloads.addAll(newList)

        diffResult.dispatchUpdatesTo(this)
    }

    fun deleteDownload(download: DownloadInfo) {
        val index = downloads.indexOf(download)
        downloads.remove(download)
        notifyItemRemoved(index)
        Timber.i("Deleted item at position $index")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadsViewHolder {
        context = parent.context

        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.item_download__, parent, false)

        return DownloadsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DownloadsViewHolder, position: Int) {
        val downloadItem = downloads[position]
        holder.bind(downloadItem)
    }

    inner class DownloadsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private lateinit var downloadItem: DownloadInfo
        private var isDownloading: Boolean = false

        private var downloadId: Int = 0

        fun bind(downloadItem: DownloadInfo) {
            this.downloadItem = downloadItem

            CoroutineScope(Dispatchers.IO).launch {

                val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.ROOT)
                val downloadDate = Date(downloadItem.dateAdded)
                itemView.tvDate.text = formatter.format(downloadDate)

                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(downloadItem.url, HashMap())
                val durationString = getDurationString(downloadItem.duration)
                val image = retriever.frameAtTime

                withContext(Dispatchers.Main) {

                    itemView.image.load(image)

                    itemView.tvDuration.apply {
                        text = durationString
                        visible()
                    }
                }
            }

            val videoTitle = Html.fromHtml(
                if (downloadItem.name.isEmpty()) "Facebook Video - ${abs(downloadItem.hashCode())}" else downloadItem.name
            )

            itemView.tvName.text = videoTitle
            itemView.tvStatus.text = context.getString(R.string.ready)

            itemView.tvMenu.setOnClickListener {
                openPopupMenu()
            }

            itemView.btnStartPause.setOnClickListener {
                toggleDownload()
            }
        }

        private fun getVideoFilePath(downloadItem: DownloadInfo): String {
            return "${videoDownloader.getDownloadDir()}/${downloadItem.name}.mp4"
        }

        private fun toggleDownload() {
            if (isDownloading)
                pauseDownload()
            else
                startDownload()
        }

        private fun openPopupMenu() {
            val popupMenu = PopupMenu(context, itemView.tvMenu)
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
                        interaction.deleteDownload(downloads[adapterPosition])
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

        private fun startDownload() {
            itemView.btnStartPause.setImageResource(R.drawable.ic_pause)

            downloadId = videoDownloader.downloadFile(downloadItem, object :
                DownloadEvents {
                override fun onProgressChanged(downloaded: Long, percentage: Int) {
                    itemView.tvPercentage.text =
                        context.getString(R.string.percentage, percentage)
                    itemView.progressDownload.progress = percentage


                    val downloadedMB = (downloaded.toDouble() / 1024 / 1024)

                    itemView.tvDownloadedSize.text =
                        if (downloadedMB.toInt() > 0) "${downloadedMB.toInt()}  MB" else "${(downloadedMB * 1024).toInt()} KB"
                }

                override fun onPause() {
                    isDownloading = false
                    itemView.progressDownload.visible()
                    itemView.tvStatus.text = context.getString(R.string.paused)
                    itemView.btnStartPause.setImageResource(R.drawable.ic_start)

                }

                override fun onCompleted() {
                    val facebookVideo = FacebookVideo(
                        downloadItem.name, downloadItem.duration,
                        getVideoFilePath(downloadItem), downloadItem.url
                    )


                    interaction.onVideoDownloaded(adapterPosition, facebookVideo)

                    itemView.btnStartPause.setImageResource(R.drawable.ic_start)
                    itemView.btnStartPause.gone()
                    itemView.tvStatus.text = context.getString(R.string.completed)
                    isDownloading = false
                    downloadItem.isCompleted = true

                    animateLayoutChanges()
                }


                override fun onError(error: DownloadException) {
                    Timber.e(error)
                    isDownloading = false
                    itemView.progressDownload.invisible()
                    itemView.tvStatus.text = context.getString(R.string.failed)
                    itemView.btnStartPause.setImageResource(R.drawable.ic_start)

                }

                override fun onCancelled() {
                    isDownloading = false
                    itemView.progressDownload.invisible()
                    itemView.btnStartPause.setImageResource(R.drawable.ic_start)
                    itemView.tvStatus.text = context.getString(R.string.cancelled)
                }

                override fun onWaitingForNetwork() {
                    Timber.i("Waiting for network")
                    isDownloading = false
                    itemView.progressDownload.visible()
                    itemView.btnStartPause.setImageResource(R.drawable.ic_start)
                    itemView.tvStatus.text = context.getString(R.string.connecting)
                }

                override fun onStart() {
                    isDownloading = true
                    itemView.progressDownload.visible()
                    itemView.btnStartPause.setImageResource(R.drawable.ic_pause)
                    itemView.tvStatus.text = context.getString(R.string.downloading)
                }
            })
        }

        private fun animateLayoutChanges() {
            TransitionManager.beginDelayedTransition(itemView as ViewGroup)
            val constraintSet = ConstraintSet()

            constraintSet.apply {
                clone(itemView.constraintLayout)
                connect(
                    R.id.tvDownloadedSize,
                    ConstraintSet.TOP,
                    R.id.tvDate,
                    ConstraintSet.TOP
                )
                applyTo(itemView.constraintLayout)
            }
        }

        private fun pauseDownload() {
            itemView.btnStartPause.setImageResource(R.drawable.ic_start)
            videoDownloader.pauseDownload(downloadId)
        }
    }

    interface Interaction {
        fun onVideoDownloaded(position: Int, video: FacebookVideo)
        fun deleteDownload(download: DownloadInfo)
        fun onDownloadError(position: Int)
    }

    override fun getItemCount(): Int = downloads.size
}
