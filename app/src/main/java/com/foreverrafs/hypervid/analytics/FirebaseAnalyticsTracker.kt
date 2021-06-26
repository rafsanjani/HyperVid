package com.foreverrafs.hypervid.analytics

import androidx.core.os.bundleOf
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class FirebaseAnalyticsTracker : Analytics {
    override fun trackEvent(event: AnalyticsEvent) {
        Firebase.analytics.logEvent(event.name, bundleOf(*event.properties.toList().toTypedArray()))
    }
}
