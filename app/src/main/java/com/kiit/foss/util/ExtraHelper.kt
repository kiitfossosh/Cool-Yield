package com.kiit.foss.util

import android.annotation.SuppressLint
import android.content.Context
import java.util.*
import java.util.regex.Pattern

internal object ExtraHelper {
    const val PREF_NAME = "coolyield"
    @JvmStatic
    fun setStringPref(
        context: Context,
        str: String?,
        str2: String?
    ): Boolean {
        val edit =
            context.getSharedPreferences(PREF_NAME, 0).edit()
        edit.putString(str2, str)
        return edit.commit()
    }

    @JvmStatic
    fun getStringPref(context: Context, str: String?): String? {
        return context.getSharedPreferences(PREF_NAME, 0).getString(str, "")
    }

    fun extractUrls(str: String): ArrayList<String> {
        val arrayList = ArrayList<String>()
        @SuppressLint("WrongConstant") val matcher =
            Pattern.compile(
                "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)",
                2
            ).matcher(str)
        while (matcher.find()) {
            arrayList.add(str.substring(matcher.start(0), matcher.end(0)))
        }
        return arrayList
    }
}