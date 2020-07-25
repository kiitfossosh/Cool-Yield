package com.kiit.foss.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build.VERSION
import android.text.TextUtils
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import java.lang.ref.WeakReference

object FBextraHelper {

    @SuppressLint("Deprecated")
    fun createCookieManger(context: Context) {
        if (VERSION.SDK_INT >= 22) {
            try {
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            } catch (unused: Exception) {
            }
            return
        }
    }

    fun mValadateCookie(str: String): Boolean {
        return !TextUtils.isEmpty(str) && str.contains("sessionid") && str.contains("ds_user_id")
    }


}