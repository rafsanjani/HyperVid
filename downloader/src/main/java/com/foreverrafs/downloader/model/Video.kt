package com.foreverrafs.downloader.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Video(
    var title: String, var size: Long, var url: String
) : Parcelable {

}