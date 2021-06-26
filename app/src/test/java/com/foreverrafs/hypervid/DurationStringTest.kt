package com.foreverrafs.hypervid

import com.foreverrafs.hypervid.util.getDurationString
import com.google.common.truth.Truth
import org.junit.Test

class DurationStringTest {
    @Test
    fun testDurationSecondsOnly() {
        val duration = 12893L
        val date = getDurationString(duration)

        Truth.assertThat(date).isEqualTo("00:12")
    }

    @Test
    fun testDurationMinutesOnly() {
        val duration = 120918L
        val date = getDurationString(duration)

        Truth.assertThat(date).isEqualTo("02:00")
    }

    @Test
    fun testDurationMinutesAndSeconds() {
        val duration = 364000L
        val date = getDurationString(duration)

        Truth.assertThat(date).isEqualTo("06:04")
    }

    @Test
    fun testDurationHoursOnly() {
        val duration = 3600000L
        val date = getDurationString(duration)

        Truth.assertThat(date).isEqualTo("01:00:00")
    }

    @Test
    fun testDurationHoursAndMinutesAndSeconds() {
        val duration = 3710000L
        val date = getDurationString(duration)

        Truth.assertThat(date).isEqualTo("01:01:50")
    }
}
