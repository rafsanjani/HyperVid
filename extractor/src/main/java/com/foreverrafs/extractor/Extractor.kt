package com.foreverrafs.extractor

interface Extractor {
    suspend fun extractVideoUrl(facebookUrl: String): Downloadable

    interface ExtractionEvents {
        fun onComplete(downloadable: Downloadable)
        fun onError(error: Exception)
    }
}
