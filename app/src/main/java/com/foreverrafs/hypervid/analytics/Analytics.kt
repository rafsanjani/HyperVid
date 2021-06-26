package com.foreverrafs.hypervid.analytics

interface Analytics {
    fun trackEvent(event: AnalyticsEvent)
}
