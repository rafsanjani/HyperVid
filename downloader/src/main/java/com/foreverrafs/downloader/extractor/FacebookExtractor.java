package com.foreverrafs.downloader.extractor;

import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public final class FacebookExtractor extends AsyncTask<String, Integer, FacebookFile> {
    private ExtractionEventsListenener eventsListener;

    public interface ExtractionEventsListenener {
        void onExtractionComplete(FacebookFile facebookFile);

        void onExtractionFail(Exception exception);
    }

    public void addExtractionEventsListenener(ExtractionEventsListenener eventsListenener) {
        this.eventsListener = eventsListenener;
    }

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36";


    private FacebookFile parseHtml(String url) {
        String result = "";
        String filename = "";
        FacebookFile facebookFile = new FacebookFile();
        try {
            URL getUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) getUrl.openConnection();

            BufferedReader reader = null;
            urlConnection.setRequestProperty("User-Agent", USER_AGENT);
            StringBuilder streamMap = new StringBuilder();
            try {
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    streamMap.append(line);
                }
            } catch (Exception exception) {
                eventsListener.onExtractionFail(exception);
            } finally {
                if (reader != null)
                    reader.close();
                urlConnection.disconnect();
            }

            if (streamMap.toString().contains("You must log in to continue.")) {
                result = "Not Public Video";
            } else {

                Pattern metaTAGVideoSRC = Pattern.compile("<meta property=\"og:video\"(.+?)\" />");
                Matcher metaTAGVideoSRCPatternMatcher = metaTAGVideoSRC.matcher(streamMap);

                Pattern metaTAGTitle = Pattern.compile("<meta property=\"og:title\"(.+?)\" />");
                Matcher metaTAGTitleMatcher = metaTAGTitle.matcher(streamMap);

                Pattern metaTAGDescription = Pattern.compile("<meta property=\"og:description\"(.+?)\" />");
                Matcher metaTAGDescriptionMatcher = metaTAGDescription.matcher(streamMap);

                Pattern metaTAGType = Pattern.compile("<meta property=\"og:video:type\"(.+?)\" />");
                Matcher metaTAGTypeMatcher = metaTAGType.matcher(streamMap);


                if (metaTAGVideoSRCPatternMatcher.find()) {
                    String metaTAG = streamMap.substring(metaTAGVideoSRCPatternMatcher.start(), metaTAGVideoSRCPatternMatcher.end());
                    Pattern srcFind = Pattern.compile("content=\"(.+?)\"");
                    Matcher srcFindMatcher = srcFind.matcher(metaTAG);
                    if (srcFindMatcher.find()) {
                        String src = metaTAG.substring(srcFindMatcher.start(), srcFindMatcher.end()).replace("content=", "").replace("\"", "");
                        facebookFile.setUrl(src.replace("&amp;", "&"));

                        HttpsURLConnection openUrl = (HttpsURLConnection) new URL(src).openConnection();
                        openUrl.connect();
                        long x = openUrl.getContentLength();
                        long fileSizeInKB = x / 1024;
                        long fileSizeInMB = fileSizeInKB / 1024;
                        facebookFile.setSize(fileSizeInMB > 1 ? fileSizeInMB + " MB" : fileSizeInKB + " KB");
                        openUrl.disconnect();
                    }
                } else {
                    return null;
                }
                if (metaTAGTitleMatcher.find()) {
                    String author = streamMap.substring(metaTAGTitleMatcher.start(), metaTAGTitleMatcher.end());
                    Log.e("Extractor", "AUTHOR :: " + author);

                    author = author.replace("<meta property=\"og:title\" content=\"", "").replace("\" />", "");

                    facebookFile.setAuthor(author);
                } else {
                    facebookFile.setAuthor("fbdescription");
                }

                if (metaTAGDescriptionMatcher.find()) {
                    String name = streamMap.substring(metaTAGDescriptionMatcher.start(), metaTAGDescriptionMatcher.end());

                    Log.e("Extractor", "FILENAME :: " + name);


                    name = name.replace("<meta property=\"og:description\" content=\"", "").replace("\" />", "");

                    facebookFile.setFilename(name);
                } else {
                    facebookFile.setFilename("fbdescription");
                }

                if (metaTAGTypeMatcher.find()) {
                    String ext = streamMap.substring(metaTAGTypeMatcher.start(), metaTAGTypeMatcher.end());
                    Log.e("Extractor", "EXT :: " + ext);

                    ext = ext.replace("<meta property=\"og:video:type\" content=\"", "").replace("\" />", "").replace("video/", "");

                    facebookFile.setExt(ext);
                } else {
                    facebookFile.setExt("mp4");
                }

                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(facebookFile.getUrl(), new HashMap<String, String>());
                    facebookFile.setDuration(Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
                    facebookFile.setCoverImage(retriever.getFrameAtTime());
                } catch (Exception E) {
                    facebookFile.setDuration(0L);
                }
            }

            return facebookFile;
        } catch (Exception E) {
            E.printStackTrace();
            return null;
        }
    }

    @Override
    protected FacebookFile doInBackground(String... urls) {
        return parseHtml(urls[0]);
    }

    @Override
    protected void onPostExecute(FacebookFile facebookFile) {
        super.onPostExecute(facebookFile);
        if (facebookFile != null) {


            Log.e("FB", "URL :: " + facebookFile.getUrl());
            Log.e("FB", "Author :: " + facebookFile.getAuthor());
            Log.e("FB", "Ext :: " + facebookFile.getExt());
            eventsListener.onExtractionComplete(facebookFile);
        }
    }
}
