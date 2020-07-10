package com.foreverrafs.hypervid.ui.videos

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.foreverrafs.hypervid.R
import com.foreverrafs.hypervid.model.FBVideo
import com.foreverrafs.hypervid.util.ItemTouchCallback
import com.foreverrafs.hypervid.util.getDurationString
import kotlinx.android.synthetic.main.item_video__.view.*
import kotlinx.android.synthetic.main.list_empty.view.tvTitle
import kotlinx.coroutines.*
import load
import shareFile
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


    private val diffCallback = object : DiffUtil.ItemCallback<FBVideo>() {
        override fun areContentsTheSame(oldItem: FBVideo, newItem: FBVideo) = oldItem == newItem
        override fun areItemsTheSame(oldItem: FBVideo, newItem: FBVideo) =
            oldItem.url == newItem.url
    }

    private val asyncDiffer = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video__, parent, false)
        return VideosViewHolder(view)
    }

    private fun swapItems(fromPosition: Int, toPosition: Int) {
        val list = asyncDiffer.currentList.toMutableList()

        Collections.swap(list, fromPosition, toPosition)
        submitList(list)
        Timber.i("Moved from $fromPosition to $toPosition")
    }

    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {
        holder.bind(asyncDiffer.currentList[position])
    }

    inner class VideosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(FBVideo: FBVideo) {
            val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                Timber.e(throwable)
            }

            val job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(FBVideo.path)

                withContext(Dispatchers.Main) {
                    itemView.imageCover.load(retriever.frameAtTime!!)
                }
            }

            job.invokeOnCompletion { error ->
                if (error != null) {
                    Timber.e(error)
                    return@invokeOnCompletion
                }


                val title =
                    if (FBVideo.title.isEmpty()) "Facebook Video - ${abs(FBVideo.hashCode())}" else FBVideo.title

                itemView.tvTitle.text = Html.fromHtml(title)

                itemView.tvDuration.text = getDurationString(FBVideo.duration)
            }


            itemView.btnPlay.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(FBVideo.path), "video/*")

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "Unable to play video. Locate and play it from your gallery",
                        Toast.LENGTH_SHORT
                    ).show()
                    Timber.e(e)
                }
            }

            itemView.btnShareWhatsapp.setOnClickListener {
                context.shareFile(FBVideo.path, "com.whatsapp")
            }

            itemView.btnShare.setOnClickListener {
                val uri = Uri.fromFile(File(FBVideo.path))

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    setDataAndType(Uri.parse(FBVideo.path), "*/*")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val shareIntent = Intent.createChooser(sendIntent, "Share Facebook Video")
                shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(shareIntent)
            }
        }
    }

    fun submitList(videos: List<FBVideo>) {
        asyncDiffer.submitList(videos)
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) =
        swapItems(fromPosition, toPosition)

    override fun onItemDismiss(position: Int) =
        callback.deleteVideo(asyncDiffer.currentList[position])

    override fun getItemCount(): Int {
        return asyncDiffer.currentList.size
    }

    interface VideoCallback {
        fun deleteVideo(video: FBVideo)
    }
}