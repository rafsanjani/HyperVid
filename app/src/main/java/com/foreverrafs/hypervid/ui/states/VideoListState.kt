package com.foreverrafs.hypervid.ui.states

import com.foreverrafs.hypervid.model.FBVideo

sealed class VideoListState {
    object Loading : VideoListState()
    data class Error(val exception: Throwable) : VideoListState()
    data class Videos(val videos: List<FBVideo>) : VideoListState()
}