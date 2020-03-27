package com.foreverrafs.rdownloader.adapter

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
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.foreverrafs.downloader.downloader.DownloadEvents
import com.foreverrafs.downloader.downloader.DownloadException
import com.foreverrafs.downloader.downloader.VideoDownloader
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.rdownloader.R
import com.foreverrafs.rdownloader.model.FacebookVideo
import com.foreverrafs.rdownloader.util.Tools
import com.foreverrafs.rdownloader.util.gone
import com.foreverrafs.rdownloader.util.invisible
import com.foreverrafs.rdownloader.util.visible
import kotlinx.android.synthetic.main.item_download__.view.*
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import kotlin.math.abs


class DownloadsAdapter(private val context: Context) :
    ListAdapter<DownloadInfo, DownloadsAdapter.DownloadsViewHolder>(object :
        DiffUtil.ItemCallback<DownloadInfo>() {
        override fun areItemsTheSame(oldItem: DownloadInfo, newItem: DownloadInfo): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DownloadInfo, newItem: DownloadInfo): Boolean {
            return oldItem.url == newItem.url
        }
    }) {


    private val videoDownloader: VideoDownloader = VideoDownloader.getInstance(context)!!
    private var downloadCompletedListener: DownloadCompletedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadsViewHolder {
        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.item_download__, parent, false)

        return DownloadsViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: DownloadsViewHolder, position: Int) {
        val downloadItem = getItem(position)
        holder.bind(downloadItem)
    }


    interface DownloadCompletedListener {
        fun onDownloadCompleted(facebookVideo: FacebookVideo)
    }

    inner class DownloadsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private lateinit var downloadItem: DownloadInfo
        private var isDownloading: Boolean = false

        private var downloadId: Int = 0

        fun bind(downloadItem: DownloadInfo) {
            this.downloadItem = downloadItem

            itemView.tvName.text = Html.fromHtml(
                if (downloadItem.name.isEmpty()) "Facebook Video - ${abs(downloadItem.hashCode())}" else downloadItem.name
            )

            val formatter = DateTimeFormat.forPattern("MMMM d, yyyy")

            itemView.tvDate.text =
                downloadItem.dateAdded.toString(formatter)

            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(downloadItem.url, HashMap())

            itemView.image.load(retriever.frameAtTime)

            itemView.tvStatus.text = context.getString(R.string.ready)

            itemView.tvDuration.text = Tools.getDurationString(downloadItem.duration)

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
                        stopDownload()
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
                    itemView.tvPercentage.text = context.getString(R.string.percentage, percentage)
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
                        getVideoFilePath(downloadItem)
                    )

                    downloadCompletedListener?.onDownloadCompleted(facebookVideo)

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

}