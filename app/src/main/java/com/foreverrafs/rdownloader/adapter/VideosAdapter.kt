package com.foreverrafs.rdownloader.adapter

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.foreverrafs.rdownloader.R
import com.foreverrafs.rdownloader.model.FacebookVideo
import com.foreverrafs.rdownloader.util.Tools
import com.foreverrafs.rdownloader.util.shareFile
import kotlinx.android.synthetic.main.item_video__.view.*
import kotlinx.android.synthetic.main.list_empty.view.tvTitle
import java.io.File
import java.util.*
import kotlin.math.abs

class VideosAdapter(private val context: Context) :
    RecyclerView.Adapter<VideosAdapter.VideosViewHolder>() {
    private var videosListChangedListener: VideosListChangedListener? = null
    private val videoList = mutableListOf<FacebookVideo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video__, parent, false)
        return VideosViewHolder(view)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {
        holder.bind(videoList[position])
    }

    fun addVideo(facebookVideo: FacebookVideo) {
        videoList.add(facebookVideo)
        notifyItemInserted(videoList.size)
        videosListChangedListener?.onVideosListChanged(itemCount)
    }

    fun removeVideo(position: Int) {
        videoList.removeAt(position)
        notifyItemRemoved(position)
        videosListChangedListener?.onVideosListChanged(itemCount)
    }

    fun swapVideos(from: Int, to: Int) {
        Collections.swap(videoList, from, to)
        notifyItemMoved(from, to)
    }

    fun addVideosListChangedListener(listener: VideosListChangedListener){
        this.videosListChangedListener = listener
    }


    inner class VideosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(facebookVideo: FacebookVideo) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(facebookVideo.path)

            itemView.imageCover.load(retriever.frameAtTime)

            itemView.tvTitle.text = Html.fromHtml(
                if (facebookVideo.title.isEmpty()) "Facebook Video - ${abs(facebookVideo.hashCode())}" else facebookVideo.title
            )

            itemView.tvDuration.text = Tools.getDurationString(facebookVideo.duration)

            itemView.btnPlay.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(facebookVideo.path), "video/*")

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }

            itemView.btnShareWhatsapp.setOnClickListener {
                context.shareFile(facebookVideo.path, "com.whatsapp_icon")
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

    interface VideosListChangedListener {
        fun onVideosListChanged(size: Int)
    }

}