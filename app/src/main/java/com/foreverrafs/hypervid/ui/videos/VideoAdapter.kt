package com.foreverrafs.hypervid.ui.videos

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.foreverrafs.hypervid.databinding.ItemVideoBinding
import com.foreverrafs.hypervid.model.FBVideo
import com.foreverrafs.hypervid.util.ItemTouchCallback
import com.foreverrafs.hypervid.util.getDurationString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import load
import playVideo
import shareFile
import timber.log.Timber
import visible
import java.util.*

class VideoAdapter(
    private val callback: VideoCallback
) :
    RecyclerView.Adapter<VideoAdapter.VideosViewHolder>(),
    ItemTouchCallback.ItemTouchHelperAdapter {

    private lateinit var context: Context
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val diffCallback = object : DiffUtil.ItemCallback<FBVideo>() {
        override fun areContentsTheSame(oldItem: FBVideo, newItem: FBVideo) = oldItem == newItem
        override fun areItemsTheSame(oldItem: FBVideo, newItem: FBVideo) =
            oldItem.url == newItem.url
    }

    private val asyncDiffer = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        context = parent.context

        return VideosViewHolder(
            ItemVideoBinding.inflate(LayoutInflater.from(context), parent, false)
        )
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

    inner class VideosViewHolder(private val binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val retriever = MediaMetadataRetriever()
        fun bind(video: FBVideo) = with(binding) {

            coroutineScope.launch {
                var coverArt: Bitmap? = null
                var duration = ""

                runCatching {
                    retriever.setDataSource(video.url, HashMap())
                    duration = getDurationString(
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                            .toLong()
                    )

                    coverArt = retriever.frameAtTime
                }.onSuccess {
                    withContext(Dispatchers.Main.immediate) {
                        with(binding) {
                            coverArt?.let { imageCover.load(it) }

                            tvDuration.text = duration
                            tvDuration.visible()
                        }
                    }
                }.onFailure { throwable ->
                    Timber.e(throwable)
                }
            }

            tvTitle.text = video.title

            btnPlay.setOnClickListener {
                context.playVideo(video.path)
            }

            btnShareWhatsapp.setOnClickListener {
                context.shareFile(video.path, "com.whatsapp")
            }

            btnDelete.setOnClickListener {
                callback.deleteVideo(asyncDiffer.currentList[bindingAdapterPosition])
            }

            btnShare.setOnClickListener {
                context.shareFile(path = video.path)
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