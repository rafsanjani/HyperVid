package com.foreverrafs.hypervid.analytics

interface AnalyticsEvent {
    val name: String
    val properties: Map<String, Any>
}
