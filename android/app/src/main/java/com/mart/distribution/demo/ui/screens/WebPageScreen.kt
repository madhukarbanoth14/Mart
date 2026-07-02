package com.mart.distribution.demo.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mart.distribution.demo.legal.LegalUrls
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.theme.WholesaleBg

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPageScreen(
    url: String,
    title: String,
    onBack: () -> Unit,
    assetFallbackUrl: String? = null,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(WholesaleBg),
    ) {
        FmAppHeader(title = title, onBack = onBack)
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = false
                        domStorageEnabled = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                    }
                    webViewClient =
                        object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?,
                            ): Boolean {
                                val target = request?.url?.toString().orEmpty()
                                return when {
                                    target.startsWith("http://") ||
                                        target.startsWith("https://") ||
                                        target.startsWith("file:///android_asset/") -> false
                                    else -> true
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?,
                            ) {
                                val fallback = assetFallbackUrl ?: return
                                if (failingUrl != fallback) {
                                    view?.loadUrl(fallback)
                                }
                            }
                        }
                    loadUrl(url)
                }
            },
            update = { webView ->
                if (webView.url != url) {
                    webView.loadUrl(url)
                }
            },
        )
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    WebPageScreen(
        url = LegalUrls.privacyPolicyWebUrl(),
        title = "Privacy Policy",
        onBack = onBack,
        assetFallbackUrl = LegalUrls.PRIVACY_POLICY_ASSET,
    )
}
