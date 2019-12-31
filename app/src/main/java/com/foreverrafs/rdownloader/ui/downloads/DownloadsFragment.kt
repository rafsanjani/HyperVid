package com.foreverrafs.rdownloader.ui.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.foreverrafs.rdownloader.R
import com.foreverrafs.rdownloader.adapter.DownloadsAdapter
import kotlinx.android.synthetic.main.fragment_downloads.*

class DownloadsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        downloadListRecyclerView.adapter = DownloadsAdapter.getInstance(context!!)
    }
}