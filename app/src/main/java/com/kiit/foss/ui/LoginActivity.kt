package com.kiit.foss.ui

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.kiit.foss.util.FBextraHelper
import com.kiit.foss.util.InstaExtraHelper.extractSetCookiFb
import com.meghdut.coolyield.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val webChrome = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            progressBar.isVisible = newProgress in 1..100
            webView.isVisible = !(newProgress in 1..100)
        }
    }

    private val webViewClass = object : WebViewClient() {
        fun storeData(webView: WebView, str: String): Boolean {
            if (str.contains("play.google.com/store/apps/details?id=com.instagram.android")) {
                val intent = Intent("android.intent.action.VIEW")
                intent.data = Uri.parse("market://details?id=com.instagram.android")
                startActivity(intent)
            } else if (str.contains("https://m.facebook.com/v2.2/dialog/oauth?channel")) {
                webView.loadUrl("https://www.instagram.com/accounts/login/")
                return false
            } else {
                webView.loadUrl(str)
                val cookie = CookieManager.getInstance().getCookie(str)
                if (FBextraHelper.mValadateCookie(cookie)) {
                    extractSetCookiFb(cookie, applicationContext)
                    webView.stopLoading()
                    try {
                        finish()
                    } catch (unused: Exception) {
                    }
                    val intent2 = Intent(
                        applicationContext, MainActivity::class.java
                    )
                    finish()
                    startActivity(intent2)
                    overridePendingTransition(
                        R.anim.left_in,
                        R.anim.left_out
                    )
                }
            }
            return true
        }


        override fun onReceivedError(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?,
            webResourceError: WebResourceError?
        ) {
            println("com.kiit.foss.ui>>LoginActivity>onReceivedError  ")
        }

        @TargetApi(21)
        override fun shouldOverrideUrlLoading(
            webView: WebView,
            webResourceRequest: WebResourceRequest
        ): Boolean {
            return storeData(webView, webResourceRequest.url.toString())
        }

        override fun shouldOverrideUrlLoading(webView: WebView, str: String): Boolean {
            return storeData(webView, str)
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun startWebView() {
        FBextraHelper.createCookieManger(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        if (VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            webView.getSettings().setMixedContentMode(0)
        }
        webView.webViewClient = webViewClass
        webView.webChromeClient = webChrome
        webView.loadUrl("https://www.instagram.com/accounts/login/")

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }
}