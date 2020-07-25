package com.kiit.foss.util

import android.content.Context
import com.google.gson.GsonBuilder
import com.kiit.foss.util.ExtraHelper.getStringPref
import com.kiit.foss.util.ExtraHelper.setStringPref
import okhttp3.OkHttpClient
import org.droidparts.contract.HTTP
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object InstaExtraHelper {
    var BASEURL = "https://i.instagram.com/api/v1/"
    var ds_userId: String? = null
        private set
    var sessionid: String? = null
        private set

    fun getHeader(context: Context?, str: String?): Retrofit? {
        return try {
            if (ds_userId == null) {
                setDs_user_id(
                    getStringPref(context!!, "userid")
                )
            }
            if (sessionid == null) {
                setSessionId(
                    getStringPref(context!!, "sessionid")
                )
            }
            val sb = StringBuilder()
            sb.append("ds_user_id=")
            sb.append(ds_userId)
            sb.append("; sessionid=")
            sb.append(sessionid)
            sb.append(";")
            val sb2 = sb.toString()
            Retrofit.Builder().baseUrl(str)
                .client(OkHttpClient().newBuilder().addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .header("x-ig-capabilities", "3w==")
                            .header("Accept-Language", "en-GB,en-US;q=0.8,en;q=0.6")
                            .header(
                                HTTP.Header.USER_AGENT,
                                "Instagram 9.5.2 (iPhone7,2; iPhone OS 9_3_3; en_US; en-US; scale=2.00; 750x1334) AppleWebKit/420+"
                            )
                            .header("Accept", "*/*")
                            .header("Referer", "https://www.instagram.com/")
                            .header("authority", "i.instagram.com/")
                            .header("Cookie", sb2).build()
                    )
                }.build()).addConverterFactory(
                    GsonConverterFactory.create(
                        GsonBuilder().setLenient().create()
                    )
                ).build()
        } catch (unused: Exception) {
            null
        }
    }

    fun extractSetCookiFb(str: String, context: Context) {
        val sb = StringBuilder()
        for (split in str.split(";".toRegex()).toTypedArray()) {
            for (trim in split.split(";".toRegex()).toTypedArray()) {
                val str2 = trim.trim { it <= ' ' }
                if (str2.startsWith("mid") || str2.startsWith("s_network") || str2.startsWith("csrftoken") || str2.startsWith(
                        "sessionid"
                    ) || str2.startsWith("ds_user_id")
                ) {
                    sb.append(str2)
                    sb.append("; ")
                    if (str2.startsWith("sessionid")) {
                        val split2 =
                            str2.split("=".toRegex()).toTypedArray()
                        if (split2.size == 2) {
                            sessionid = split2[1]
                        }
                        //                        else {
//                            sessionId = BuildConfig.VERSION_NAME;
//                        }
                    }
                    if (str2.startsWith("ds_user_id")) {
                        val split3 =
                            str2.split("=".toRegex()).toTypedArray()
                        if (split3.size == 2) {
                            ds_userId = split3[1]
                        }
                        //                        else {
//                            ds_user_id = BuildConfig.VERSION_NAME;
//                        }
                    }
                }
            }
        }
        setStringPref(
            context,
            ds_userId, "userid")
        setStringPref(context,
            sessionid, "sessionid")
    }

    fun setSessionId(str: String?) {
        sessionid = str
    }

    fun setDs_user_id(str: String?) {
        ds_userId = str
    }
}