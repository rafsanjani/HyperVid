package com.foreverrafs.rdownloader.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.foreverrafs.downloader.DownloadEvents
import com.foreverrafs.downloader.DownloadException
import com.foreverrafs.downloader.VideoDownloader
import com.foreverrafs.downloader.model.DownloadInfo
import com.foreverrafs.rdownloader.R
import com.foreverrafs.rdownloader.util.disable
import com.foreverrafs.rdownloader.util.invisible
import com.foreverrafs.rdownloader.util.visible
import kotlinx.android.synthetic.main.item_download__.view.*
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.util.concurrent.TimeUnit

class DownloadsAdapter private constructor(private val context: Context) :
    RecyclerView.Adapter<DownloadsAdapter.DownloadsViewHolder>() {

    private var downloadList = mutableListOf<DownloadInfo>()
    private val videoDownloader: VideoDownloader = VideoDownloader.getInstance(context)!!

    companion object {
        private var instance: DownloadsAdapter? = null
        fun getInstance(context: Context): DownloadsAdapter {
            if (instance == null) {
                instance = DownloadsAdapter(context)
            }

            return instance!!
        }
    }

    fun clearDownloads() {
        downloadList.clear()
        notifyDataSetChanged()
    }


    fun addDownload(downloadInfo: DownloadInfo) {
        downloadList.add(downloadInfo)
        notifyItemInserted(downloadList.size)
    }

    fun removeDownload(position: Int) {
        downloadList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadsViewHolder {
        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.item_download__, parent, false)

        return DownloadsViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return downloadList.size
    }

    override fun onBindViewHolder(holder: DownloadsViewHolder, position: Int) {
        val downloadItem = downloadList[position]
        holder.bind(downloadItem)
    }

    inner class DownloadsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private lateinit var downloadItem: DownloadInfo
        private var isDownloading: Boolean = false

        private var downloadId: Int = 0

        fun bind(downloadItem: DownloadInfo) {
            this.downloadItem = downloadItem

            itemView.tvName.text = downloadItem.name

            val formatter = DateTimeFormat.forPattern("MMMM d, yyyy")

            itemView.tvDate.text =
                downloadItem.dateAdded.toString(formatter)

            itemView.image.setImageBitmap(downloadItem.image)
            itemView.tvStatus.text = context.getString(R.string.ready)

            itemView.tvDuration.text = getDuration(downloadItem.duration)
            itemView.image.load(downloadItem.image)

            itemView.tvMenu.setOnClickListener {
                openPopupMenu()
            }

            itemView.btnStartPause.setOnClickListener {
                toggleDownload()
            }
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
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.startPause -> {
                        toggleDownload()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.delete -> {
                        deleteFromDownloads()
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

        }

        private fun deleteFromDownloads() {

        }

        private fun getDuration(duration: Long): String {
            return try {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
                val seconds = duration % minutes.toInt()

                "${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
            } catch (exception: ArithmeticException) {
                "00:00"
            }
        }

        private fun startDownload() {
            downloadId = videoDownloader.downloadFile(downloadItem, object : DownloadEvents {
                override fun onDownloadProgressChanged(currentBytes: Long, totalBytes: Long) {
                    downloadItem.totalBytes = totalBytes

                    val percentage = currentBytes.toDouble() / totalBytes * 100
                    itemView.tvPercentage.text = "${percentage.toInt()} %"
                    itemView.progressDownload.progress = percentage.toInt()

                    itemView.tvDownloadedBytes.text =
                        (currentBytes / 1024 / 1024).toInt().toString() + " MB"
                }

                override fun onDownloadPaused() {
                    isDownloading = false
                    itemView.progressDownload.visible()
                    itemView.tvStatus.text = context.getString(R.string.paused)
                    itemView.btnStartPause.setImageDrawable(context.getDrawable(R.drawable.ic_start))

                }

                override fun onDownloadCompleted() {
                    itemView.progressDownload.visible()

                    itemView.progressDownload.progressDrawable.setColorFilter(
                        Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    itemView.btnStartPause.setImageDrawable(context.getDrawable(R.drawable.ic_start))
                    itemView.btnStartPause.disable()
                    itemView.tvStatus.text = context.getString(R.string.completed)
                    isDownloading = false
                }

                override fun onDownloadError(error: DownloadException) {
                    Timber.e(error)
                    isDownloading = false
                    itemView.progressDownload.invisible()
                    itemView.tvStatus.text = context.getString(R.string.failed)
                    itemView.btnStartPause.setImageDrawable(context.getDrawable(R.drawable.ic_start))

                }

                override fun onDownloadCancelled() {
                    isDownloading = false
                    itemView.progressDownload.invisible()
                    itemView.btnStartPause.setImageDrawable(context.getDrawable(R.drawable.ic_start))
                    itemView.tvStatus.text = context.getString(R.string.cancelled)
                }

                override fun onDownloadStart() {
                    isDownloading = true
                    itemView.progressDownload.visible()
                    itemView.btnStartPause.setImageDrawable(context.getDrawable(R.drawable.ic_pause))
                    itemView.tvStatus.text = context.getString(R.string.downloading)
                }
            })
        }

        private fun pauseDownload() {
            videoDownloader.pauseDownload(downloadId)
        }

    }


}