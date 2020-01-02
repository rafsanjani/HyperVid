package com.foreverrafs.rdownloader.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.foreverrafs.rdownloader.R
import com.foreverrafs.rdownloader.model.FacebookVideo

class VideosAdapter : RecyclerView.Adapter<VideosAdapter.VideosViewHolder>() {
    val videoList = mutableListOf<FacebookVideo>()

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
    }

    fun removeVideo(position: Int) {
        videoList.removeAt(position)
        notifyItemRemoved(position)
    }


    inner class VideosViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(facebookVideo: FacebookVideo) {

        }

    }

}