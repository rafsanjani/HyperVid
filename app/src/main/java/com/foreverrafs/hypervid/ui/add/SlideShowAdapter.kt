package com.foreverrafs.hypervid.ui.add

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.foreverrafs.hypervid.R
import com.foreverrafs.hypervid.model.SlideShowImage
import kotlinx.android.synthetic.main.item_slideshow.view.*
import load


/* Created by Rafsanjani on 08/04/2020. */

class SlideShowAdapter : RecyclerView.Adapter<SlideShowAdapter.SlideShowViewHolder>() {

    private var data: List<SlideShowImage> = listOf(
        SlideShowImage(
            R.drawable.walkthrough1,
            "Look for a Facebook video",
            "This applies to any public downloadable video from facebook. Some videos can be found at Videos on Watch"
        ),
        SlideShowImage(R.drawable.walkthrough2, "Tap on the upper right menu\n(three dots)", ""),
        SlideShowImage(R.drawable.walkthrough3, "Tap \"Copy Link\"", "")
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideShowViewHolder {
        return SlideShowViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_slideshow, parent, false)
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: SlideShowViewHolder, position: Int) =
        holder.bind(data[position])

    inner class SlideShowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: SlideShowImage) = with(itemView) {
            title.text = item.title
            image.load(item.imageRes)
        }
    }
}