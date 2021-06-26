package com.foreverrafs.hypervid.util

import java.time.Duration
import java.util.*

fun getDurationString(millis: Long): String {
    val duration = Duration.ofMillis(millis)

    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()
    val seconds = duration.minusHours(hours).minusMinutes(minutes).seconds

    if (hours != 0L) {
        return "${hours.to2dp}:${minutes.to2dp}:${seconds.to2dp}"
    }

    if (minutes != 0L) {
        return "${minutes.to2dp}:${seconds.to2dp}"
    }

    return "00:${seconds.to2dp}"
}

private val Long.to2dp
    get() = String.format(Locale.ENGLISH, "%02d", this)
