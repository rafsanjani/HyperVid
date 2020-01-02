package com.foreverrafs.rdownloader.util

import java.util.concurrent.TimeUnit

object Tools {
    fun getDurationString(duration: Long): String {
        return try {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
            val seconds = duration % minutes.toInt()

            "${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
        } catch (exception: ArithmeticException) {
            val seconds = TimeUnit.MILLISECONDS.toSeconds(duration)

            String.format("00:%2d", seconds)
        }
    }
}