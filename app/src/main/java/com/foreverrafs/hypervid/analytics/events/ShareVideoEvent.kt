package com.foreverrafs.hypervid.analytics.events

import com.foreverrafs.hypervid.analytics.AnalyticsEvent

class ShareVideoEvent(
    private val title: String,
    private val url: String
) : AnalyticsEvent {
    override val name: String
        get() = "share_video"
    override val properties: Map<String, Any>
        get() = mapOf(
            "video_title" to title,
            "video_url" to url
        )
}
