package com.foreverrafs.extractor

import android.media.MediaMetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection

class FacebookExtractor {
    private val USER_AGENT =
        "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36"

    private var eventsListener: ExtractionEvents? = null
    private var exception: java.lang.Exception? = null


    private fun extractFBFileInfo(url: String): DownloadableFile? {
        try {
            val html: String = downloadHtml(url)!!
            return parseHtml(html)
        } catch (e: Exception) {
            exception = e
            Timber.e(e)
        }
        return null
    }

    fun extract(facebookUrl: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val file = extractFBFileInfo(facebookUrl)
            if (file != null) {
                runBlocking(Dispatchers.Main) {
                    eventsListener?.onComplete(file)
                }
            } else {
                runBlocking(Dispatchers.Main) {
                    eventsListener?.onError(exception!!)
                }
            }
        }
    }

    fun addEventsListener(listener: ExtractionEvents) {
        this.eventsListener = listener
    }

    @Throws(IOException::class)
    private fun parseHtml(streamMap: String?): DownloadableFile? {
       val facebookFile = DownloadableFile()

        if (streamMap == null) return null
        if (streamMap.contains("You must log in to continue.")) {
            var result = "Not Public Video"
        } else {
            val metaTAGVideoSRC =
                Pattern.compile("<meta property=\"og:video\"(.+?)\" />")

            val metaTAGVideoSRCPatternMatcher = metaTAGVideoSRC.matcher(streamMap)

            val metaTAGTitle =
                Pattern.compile("<meta property=\"og:title\"(.+?)\" />")
            val metaTAGTitleMatcher = metaTAGTitle.matcher(streamMap)
            val metaTAGDescription =
                Pattern.compile("<meta property=\"og:description\"(.+?)\" />")
            val metaTAGDescriptionMatcher =
                metaTAGDescription.matcher(streamMap)
            val metaTAGType =
                Pattern.compile("<meta property=\"og:video:type\"(.+?)\" />")
            val metaTAGTypeMatcher = metaTAGType.matcher(streamMap)
            if (metaTAGVideoSRCPatternMatcher.find()) {
                val metaTAG = streamMap.substring(
                    metaTAGVideoSRCPatternMatcher.start(),
                    metaTAGVideoSRCPatternMatcher.end()
                )
                val srcFind =
                    Pattern.compile("content=\"(.+?)\"")
                val srcFindMatcher = srcFind.matcher(metaTAG)
                if (srcFindMatcher.find()) {
                    val src =
                        metaTAG.substring(srcFindMatcher.start(), srcFindMatcher.end())
                            .replace("content=", "").replace("\"", "")
                    facebookFile.url = src.replace("&amp;", "&")

                    val openUrl =
                        URL(src).openConnection() as HttpsURLConnection
                    openUrl.connect()
                    val x = openUrl.contentLength.toLong()
                    val fileSizeInKB = x / 1024
                    val fileSizeInMB = fileSizeInKB / 1024
                    facebookFile.size =
                        if (fileSizeInMB > 1) "$fileSizeInMB MB" else "$fileSizeInKB KB"

                    openUrl.disconnect()
                }
            } else {
                return null
            }
            if (metaTAGTitleMatcher.find()) {
                val authorStr =
                    streamMap.substring(metaTAGTitleMatcher.start(), metaTAGTitleMatcher.end())
                Timber.i("AUTHOR :: %s", authorStr)
                facebookFile.author = authorStr.replace("<meta property=\"og:title\" content=\"", "")
                    .replace("\" />", "")
            } else {
                facebookFile.author = "fbdescription"
            }
            if (metaTAGDescriptionMatcher.find()) {
                var name = streamMap.substring(
                    metaTAGDescriptionMatcher.start(),
                    metaTAGDescriptionMatcher.end()
                )
                Timber.i("FILENAME :: %s", name)
                name = name.replace("<meta property=\"og:description\" content=\"", "")
                    .replace("\" />", "")
                facebookFile.filename = name
            } else {
                facebookFile.filename = "fbdescription"
            }
            if (metaTAGTypeMatcher.find()) {
                val extStr =
                    streamMap.substring(metaTAGTypeMatcher.start(), metaTAGTypeMatcher.end())
                Timber.i("EXT :: %s", extStr)
                facebookFile.ext = extStr.replace("<meta property=\"og:video:type\" content=\"", "")
                    .replace("\" />", "").replace("video/", "")
            } else {
                facebookFile.ext = "mp4"
            }
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(
                    facebookFile.url,
                    HashMap()
                )
                facebookFile.duration =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
            } catch (E: Exception) {
                facebookFile.duration = 0L
            }
        }
        return facebookFile
    }

    private fun downloadHtml(url: String): String? { //download html website as a string
        try {
            val facebookUrl = URL(url)
            val urlConnection =
                facebookUrl.openConnection() as HttpURLConnection
            urlConnection.setRequestProperty("User-Agent", USER_AGENT)
            val streamMap = StringBuilder()
            BufferedReader(InputStreamReader(urlConnection.inputStream))
                .use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        streamMap.append(line)
                    }
                }
            urlConnection.disconnect()
            return streamMap.toString()
        } catch (e: Exception) {
            exception = e
        }
        return null
    }

    interface ExtractionEvents {
        fun onComplete(downloadableFile: DownloadableFile)
        fun onError(exception: Exception)
    }
}