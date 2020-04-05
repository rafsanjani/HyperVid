package com.foreverrafs.rdownloader.ui.videos

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.foreverrafs.rdownloader.R
import com.foreverrafs.rdownloader.model.FacebookVideo
import com.foreverrafs.rdownloader.util.ItemTouchCallback
import com.foreverrafs.rdownloader.util.getDurationString
import com.foreverrafs.rdownloader.util.load
import com.foreverrafs.rdownloader.util.shareFile
import kotlinx.android.synthetic.main.item_video__.view.*
import kotlinx.android.synthetic.main.list_empty.view.tvTitle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.math.abs

class VideoAdapter(
    private val context: Context,
    private val callback: VideoCallback
) :
    RecyclerView.Adapter<VideoAdapter.VideosViewHolder>(),
    ItemTouchCallback.ItemTouchHelperAdapter {

    val videos = mutableListOf<FacebookVideo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video__, parent, false)
        return VideosViewHolder(view)
    }

    private fun swapItems(fromPosition: Int, toPosition: Int) {
        Collections.swap(videos, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        Timber.i("Moved from $fromPosition to $toPosition")
    }

    fun deleteVideo(video: FacebookVideo) {
        val videoIndex = videos.indexOf(video)
        videos.remove(video)
        notifyItemRemoved(videoIndex)
        Timber.i("Deleted item at position $videoIndex")
    }

    fun addVideo(video: FacebookVideo) {
        videos.add(0, video)
        notifyItemInserted(0)
    }

    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {
        holder.bind(videos[position])
    }

    inner class VideosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(facebookVideo: FacebookVideo) {

            CoroutineScope(Dispatchers.IO).launch {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(facebookVideo.path)

                withContext(Dispatchers.Main) {
                    itemView.imageCover.load(retriever.frameAtTime)
                }
            }

            itemView.tvTitle.text = Html.fromHtml(
                if (facebookVideo.title.isEmpty()) "Facebook Video - ${abs(facebookVideo.hashCode())}" else facebookVideo.title
            )

            itemView.tvDuration.text = getDurationString(facebookVideo.duration)

            itemView.btnPlay.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(facebookVideo.path), "video/*")

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }

            itemView.btnShareWhatsapp.setOnClickListener {
                context.shareFile(facebookVideo.path, "com.whatsapp")
            }

            itemView.btnShare.setOnClickListener {
                val uri = Uri.fromFile(File(facebookVideo.path))

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    setDataAndType(Uri.parse(facebookVideo.path), "*/*")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val shareIntent = Intent.createChooser(sendIntent, "Share Facebook Video")
                shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(shareIntent)
            }
        }
    }

    fun setList(newVideos: List<FacebookVideo>) {
        val diffCallback =
            VideoDiffCallback(
                this.videos,
                newVideos
            )

        val diffResult = DiffUtil.calculateDiff(diffCallback)

        videos.clear()
        videos.addAll(newVideos)

        diffResult.dispatchUpdatesTo(this)
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) =
        swapItems(fromPosition, toPosition)

    override fun onItemDismiss(position: Int) = callback.deleteVideo(videos[position])

    override fun getItemCount(): Int {
        return videos.size
    }

    interface VideoCallback {
        fun deleteVideo(video: FacebookVideo)
    }
}