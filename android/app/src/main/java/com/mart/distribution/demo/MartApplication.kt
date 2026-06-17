package com.mart.distribution.demo

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import java.util.Locale

class MartApplication : Application(), ImageLoaderFactory {
    lateinit var container: AppContainer
        private set

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(wrapEnglish(base))
    }

    override fun onCreate() {
        super.onCreate()
        Locale.setDefault(Locale.ENGLISH)
        container = AppContainer(this)
    }

    private fun wrapEnglish(context: Context): Context {
        val locale = Locale.ENGLISH
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("product_image_cache"))
                    .maxSizeBytes(64L * 1024 * 1024)
                    .build()
            }
            .crossfade(200)
            .respectCacheHeaders(true)
            .build()
}
