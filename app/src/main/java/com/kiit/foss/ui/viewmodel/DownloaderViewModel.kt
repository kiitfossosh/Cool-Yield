package com.kiit.foss.ui.viewmodel

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kiit.foss.util.ExtraHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import kotlin.concurrent.thread


class DownloaderViewModel(val app: Application) : AndroidViewModel(app) {
    private val okHttpClient by lazy { OkHttpClient() }
    val isDownloading = MutableLiveData<Boolean>()

    init {
        System.setProperty(
            "http.agent",
            "Mozilla/5.0 (Windows NT 6.1 Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0"
        )
    }




    fun loadDownloadUrl(url: String) {
        if (isDownloading.value == true) {
            return
        }
        thread {
            isDownloading.postValue(true)
            download(url)?.let { userNamePair ->
                downloadFromUrl(userNamePair.first, userNamePair.second)
            }
            isDownloading.postValue(false)

        }

    }


    private fun download(str: String): Pair<String, String>? {
        try {
            val extractUrls = ExtraHelper.extractUrls(str)
            val pageUrl = extractUrls.firstOrNull() ?: return null
            val document: Document = Jsoup.parse(readText(pageUrl))
            if (str.contains("instagram")) {
                val userName = getUserName(document)
                val videoElement = document.select("meta[property=og:video]").first()
                val imageElement = document.select("meta[property=og:image]").first()
                val srcUrl = when {
                    videoElement != null -> videoElement.attr("content")
                    imageElement != null -> imageElement.attr("content")
                    else -> "NONE"
                }
                return srcUrl to userName
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun readText(url: String): String? {
        val request = Request.Builder().url(url).build()
        return okHttpClient.newCall(request).execute().body()?.string()
    }


    private fun getUserName(document: Document): String {
        var userName = ""
        val select = document.select("script[type=text/javascript]")
        for (i in 0 until select.size) {
            val str: String = (select.get(i) as Element).html().toString()
            if (str.contains("window._sharedData") && str.endsWith(";")) {
                val substring = str.substring(0, str.length - 1)
                if (substring.startsWith("window._sharedData = ")) {
                    try {
                        userName = (JSONObject(
                            substring.replaceFirst(
                                "window._sharedData = ".toRegex(),
                                ""
                            )
                        ).getJSONObject("entry_data")
                            .getJSONArray("PostPage")[0] as JSONObject).getJSONObject("graphql")
                            .getJSONObject("shortcode_media").getJSONObject("owner")["username"]
                            .toString()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return userName
    }


    private fun downloadFromUrl(url: String, userName: String) {
        val fileName: String
        val downloadManager = app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
        request.setNotificationVisibility(1)
        request.setTitle(userName)
        if (url.contains(".jpg")) {
            fileName = StringBuilder()
                .append(userName)
                .append(".jpg").toString()
            val notificationTitle = StringBuilder()
            notificationTitle.append(userName)
            notificationTitle.append("'s photo")
            request.setTitle(notificationTitle.toString())
        } else {
            fileName = StringBuilder().append(userName).append(".mp4").toString()
            val notificationTitle = StringBuilder()
            notificationTitle.append(userName)
            notificationTitle.append("'s video")
            request.setTitle(notificationTitle.toString())
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        val description = StringBuilder()
            .append("CoolYield")
            .append("/")
            .append(fileName)
        request.setDescription(description.toString())
        request.allowScanningByMediaScanner()
        downloadManager.enqueue(request)
    }


}