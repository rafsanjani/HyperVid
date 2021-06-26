package com.foreverrafs.hypervid.analytics.events

import com.foreverrafs.hypervid.analytics.AnalyticsEvent
import com.foreverrafs.hypervid.analytics.AnalyticsEventName
import com.foreverrafs.hypervid.analytics.AnalyticsKeys

data class DownloadVideoEvent(
    private val title: String,
    private val url: String,
) : AnalyticsEvent {
    override val name: String
        get() = AnalyticsEventName.DOWNLOAD_VIDEO

    override val properties: Map<String, Any>
        get() = mapOf(
            AnalyticsKeys.VIDEO_TITLE to title,
            AnalyticsKeys.VIDEO_URL to url
        )
}
