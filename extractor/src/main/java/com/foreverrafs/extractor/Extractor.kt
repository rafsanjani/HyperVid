package com.foreverrafs.extractor

private const val TAG = "FacebookExtractor"

interface Extractor {
    suspend fun extractVideoUrl(facebookUrl: String): Downloadable

    interface ExtractionEvents {
        fun onComplete(downloadable: Downloadable)
        fun onError(error: Exception)
    }
}

