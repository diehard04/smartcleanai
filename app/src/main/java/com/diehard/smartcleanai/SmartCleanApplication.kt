package com.diehard.smartcleanai

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.diehard.smartcleanai.ads.AdManager
import com.diehard.smartcleanai.worker.GalleryScanWorker
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmartCleanApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        // Initialize AdMob SDK and register emulator as test device (runs on UI thread)
        MobileAds.initialize(this) {}
        val requestConfig = RequestConfiguration.Builder().apply {
            if (BuildConfig.DEBUG) {
                setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
            }
        }.build()
        MobileAds.setRequestConfiguration(requestConfig)
        // Preload interstitial now
        AdManager.preloadInterstitial(this)

        // Enqueue periodic weekly background auto-scan on background dispatcher so main thread is not blocked
        CoroutineScope(Dispatchers.Default).launch {
            GalleryScanWorker.enqueuePeriodicScan(this@SmartCleanApplication)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }
}
