package com.mart.distribution.demo.util

import android.os.Build

/** Heuristic: emulator uses 10.0.2.2 to reach the host; physical devices need a real URL. */
fun isProbablyEmulator(): Boolean {
    val fp = Build.FINGERPRINT
    val model = Build.MODEL
    val product = Build.PRODUCT
    val manufacturer = Build.MANUFACTURER
    return fp.startsWith("generic") ||
        fp.startsWith("unknown") ||
        model.contains("google_sdk", ignoreCase = true) ||
        model.contains("Emulator", ignoreCase = true) ||
        model.contains("Android SDK built for x86", ignoreCase = true) ||
        manufacturer.contains("Genymotion", ignoreCase = true) ||
        (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
        product == "google_sdk" ||
        product.contains("sdk_gphone", ignoreCase = true) ||
        product.contains("emulator", ignoreCase = true)
}
