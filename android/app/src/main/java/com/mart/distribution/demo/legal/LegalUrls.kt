package com.mart.distribution.demo.legal

import com.mart.distribution.demo.BuildConfig

object LegalUrls {
    const val PRIVACY_POLICY_ASSET = "file:///android_asset/legal/privacy-policy.html"

    fun privacyPolicyWebUrl(): String =
        BuildConfig.API_BASE_URL.trimEnd('/') + "/privacy-policy.html"
}
