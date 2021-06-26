package com.foreverrafs.hypervid.analytics.events

import com.foreverrafs.hypervid.analytics.AnalyticsEvent
import com.foreverrafs.hypervid.analytics.AnalyticsEventName
import com.foreverrafs.hypervid.analytics.AnalyticsKeys

data class ExtractVideoEvent(
    private val title: String,
    private val url: String,
    private val status: Status,
) : AnalyticsEvent {

    companion object {
        private const val CHAR_LIMIT = 40
    }

    override val name: String
        get() = AnalyticsEventName.EXTRACT_VIDEO

    override val properties: Map<String, Any>
        get() = mapOf(
            AnalyticsKeys.VIDEO_TITLE to title.takeLast(CHAR_LIMIT),
            AnalyticsKeys.VIDEO_URL to url.takeLast(CHAR_LIMIT),
            AnalyticsKeys.SUCCESSFUL to (status == Status.Success)
        )
}
