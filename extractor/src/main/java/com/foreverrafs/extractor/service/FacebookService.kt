package com.foreverrafs.extractor.service

import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface FacebookService {
    @FormUrlEncoded
    @POST("downloader")
    suspend fun downloadVideoHtml(
        @Field("url") videoUrl: String,
    ): ResponseBody
}