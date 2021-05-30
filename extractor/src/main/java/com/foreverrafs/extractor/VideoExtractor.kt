package com.foreverrafs.extractor

import com.foreverrafs.extractor.service.FacebookService
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Retrofit

class VideoExtractor : Extractor {
    override suspend fun extractVideoUrl(facebookUrl: String): Downloadable {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.getfvid.com/")
            .build()
            .create(FacebookService::class.java)

        val html = retrofit.downloadVideoHtml(facebookUrl)
        return parseHtml(html)
    }

    private fun parseHtml(html: ResponseBody): Downloadable {
        val document = Jsoup.parse(html.string())
        val videoUrl = document
            .body()
            .getElementsByClass("col-md-4 btns-download")
            .first()
            .getElementsByAttribute("href")
            .first().attr("href")

        val title = document.body().getElementsByClass("card-title").text()

        return Downloadable(
            url = videoUrl,
            filename = title
        )
    }
}