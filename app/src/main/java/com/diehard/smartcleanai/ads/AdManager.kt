package com.diehard.smartcleanai.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.diehard.smartcleanai.ads.AdManager.AdManager.TEST_BANNER_AD_UNIT_ID
import com.diehard.smartcleanai.ads.AdManager.AdManager.TEST_INTERSTITIAL_AD_UNIT_ID
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Reusable monetization manager for Google Mobile Ads (AdMob) and Firebase Analytics.
 * Handles lifecycle-safe Interstitial preloading, display with 2-minute frequency capping,
 * network connectivity checks, Pro-user ad suppression, and adaptive banner rendering.
 */
object AdManager {

    private const val TAG = "AdManager"

    // Official Google AdMob Test Ad Unit IDs
    object AdManager {
        val TEST_BANNER_AD_UNIT_ID get() =
            com.diehard.smartcleanai.BuildConfig.BANNER_AD_UNIT_ID

        val TEST_INTERSTITIAL_AD_UNIT_ID get() =
            com.diehard.smartcleanai.BuildConfig.INTERSTITIAL_AD_UNIT_ID
    }

    // Custom Firebase Analytics Event Names & Params
    private const val EVENT_AD_IMPRESSION = "ad_impression_custom"
    private const val EVENT_AD_FAILED_TO_LOAD = "ad_failed_to_load"
    const val AD_TYPE_INTERSTITIAL = "interstitial"
    const val AD_TYPE_BANNER = "banner"
    private const val PARAM_AD_TYPE = "ad_type"
    private const val PARAM_AD_UNIT_ID = "ad_unit_id"
    private const val PARAM_ERROR_CODE = "error_code"
    private const val PARAM_ERROR_MESSAGE = "error_message"
    private const val PARAM_TIMESTAMP = "timestamp"

    // 2-minute frequency cap (120,000 milliseconds)
    private const val FREQUENCY_CAP_MS = 2 * 60 * 1000L

    @Volatile
    private var interstitialAd: InterstitialAd? = null

    @Volatile
    private var isInterstitialLoading: Boolean = false

    private var lastInterstitialShowTime: Long = 0L

    /**
     * Checks if the device has an active internet connection.
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Preloads the post-scan Interstitial Ad in the background.
     * Triggered when the user clicks "Smart Scan Now".
     */
    fun preloadInterstitial(
        context: Context,
        adUnitId: String = TEST_INTERSTITIAL_AD_UNIT_ID
    ) {
        if (interstitialAd != null || isInterstitialLoading) {
            Log.d(TAG, "Interstitial ad is already loaded or currently loading.")
            return
        }

        val appContext = context.applicationContext
        if (!isNetworkAvailable(appContext)) {
            Log.d(TAG, "Device is offline. Skipping interstitial preload.")
            return
        }

        isInterstitialLoading = true
        val adRequest = AdRequest.Builder()
            .build()

        InterstitialAd.load(
            appContext,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad successfully preloaded.")
                    interstitialAd = ad
                    isInterstitialLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(
                        TAG,
                        "Interstitial ad failed to preload: ${loadAdError.message} (code: ${loadAdError.code})"
                    )
                    interstitialAd = null
                    isInterstitialLoading = false
                    logAdFailedToLoad(
                        context = appContext,
                        adType = AD_TYPE_INTERSTITIAL,
                        adUnitId = adUnitId,
                        errorCode = loadAdError.code,
                        errorMessage = loadAdError.message
                    )
                }
            }
        )
    }

    /**
     * Shows the full-screen interstitial ad when the scan completes.
     *
     * AdMob's native close button (X) and countdown timer will appear automatically.
     * Enforces a 2-minute frequency cap to avoid spamming the user on back-to-back scans.
     *
     * onComplete() is guaranteed to be called immediately if:
     * 1. isProUser == true
     * 2. Device is offline
     * 3. Frequency cap is active (less than 2 minutes since last interstitial show)
     * 4. Interstitial ad is null / failed to load
     * 5. Host activity is finishing or destroyed
     * 6. User closes/dismisses the ad
     * 7. The ad fails to show
     */
    fun showInterstitialAfterScan(
        activity: Activity,
        isProUser: Boolean,
        adUnitId: String = TEST_INTERSTITIAL_AD_UNIT_ID,
        onComplete: () -> Unit
    ) {
        // Condition 1: Pro user bypass
        if (isProUser) {
            Log.d(TAG, "Pro user active. Skipping interstitial.")
            onComplete()
            return
        }

        // Condition 2: Offline bypass
        if (!isNetworkAvailable(activity)) {
            Log.d(TAG, "Device offline. Skipping interstitial.")
            onComplete()
            return
        }

        // Condition 3: Frequency cap (2 minutes)
        val currentTime = System.currentTimeMillis()
        val timeSinceLastShow = currentTime - lastInterstitialShowTime
        if (timeSinceLastShow < FREQUENCY_CAP_MS) {
            Log.d(
                TAG,
                "Frequency cap active (${timeSinceLastShow / 1000}s / ${FREQUENCY_CAP_MS / 1000}s). Skipping interstitial."
            )
            onComplete()
            return
        }

        // Condition 4: Ad not ready
        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "No preloaded interstitial ad available. Skipping.")
            onComplete()
            // Attempt to preload for future scans
            preloadInterstitial(activity.applicationContext, adUnitId)
            return
        }

        // Condition 5: Activity safety check
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "Host activity is finishing or destroyed. Skipping interstitial.")
            onComplete()
            return
        }

        var hasCompleted = false
        val safeComplete = {
            if (!hasCompleted) {
                hasCompleted = true
                onComplete()
            }
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial showed full screen content.")
                lastInterstitialShowTime = System.currentTimeMillis()
                interstitialAd = null
                logAdImpression(activity, AD_TYPE_INTERSTITIAL, adUnitId)
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial dismissed by user.")
                interstitialAd = null
                safeComplete()
                // Preload next interstitial in background
                preloadInterstitial(activity.applicationContext, adUnitId)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(
                    TAG,
                    "Interstitial failed to show: ${adError.message} (code: ${adError.code})"
                )
                interstitialAd = null
                logAdFailedToLoad(
                    context = activity,
                    adType = AD_TYPE_INTERSTITIAL,
                    adUnitId = adUnitId,
                    errorCode = adError.code,
                    errorMessage = adError.message
                )
                safeComplete()
                preloadInterstitial(activity.applicationContext, adUnitId)
            }

            override fun onAdImpression() {
                Log.d(TAG, "Interstitial impression recorded.")
            }
        }

        ad.show(activity)
    }

    /**
     * Logs custom Firebase Analytics event when an ad impression is recorded.
     */
    fun logAdImpression(
        context: Context,
        adType: String,
        adUnitId: String
    ) {
        try {
            val bundle = Bundle().apply {
                putString(PARAM_AD_TYPE, adType)
                putString(PARAM_AD_UNIT_ID, adUnitId)
                putLong(PARAM_TIMESTAMP, System.currentTimeMillis())
            }
            FirebaseAnalytics.getInstance(context).logEvent(EVENT_AD_IMPRESSION, bundle)
            Log.d(TAG, "Logged Firebase Analytics event: $EVENT_AD_IMPRESSION ($adType)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log Firebase Analytics ad impression", e)
        }
    }

    /**
     * Logs custom Firebase Analytics event when an ad fails to load or show.
     */
    fun logAdFailedToLoad(
        context: Context,
        adType: String,
        adUnitId: String,
        errorCode: Int,
        errorMessage: String
    ) {
        try {
            val bundle = Bundle().apply {
                putString(PARAM_AD_TYPE, adType)
                putString(PARAM_AD_UNIT_ID, adUnitId)
                putInt(PARAM_ERROR_CODE, errorCode)
                putString(PARAM_ERROR_MESSAGE, errorMessage)
                putLong(PARAM_TIMESTAMP, System.currentTimeMillis())
            }
            FirebaseAnalytics.getInstance(context).logEvent(EVENT_AD_FAILED_TO_LOAD, bundle)
            Log.d(
                TAG,
                "Logged Firebase Analytics event: $EVENT_AD_FAILED_TO_LOAD ($adType, code: $errorCode)"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log Firebase Analytics ad failure", e)
        }
    }
}

/**
 * Reusable Jetpack Compose composable for Google AdMob Anchored Adaptive Banner ads.
 *
 * Dynamically computes screen width for [AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize]
 * and cleanly suppresses rendering if [isProUser] is true or device is offline.
 */
@Composable
fun AdaptiveBannerAd(
    modifier: Modifier = Modifier,
    isProUser: Boolean = false,
    adUnitId: String = TEST_BANNER_AD_UNIT_ID
) {
    if (isProUser) return

    val context = LocalContext.current
    if (!AdManager.isNetworkAvailable(context)) return

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    // Calculate anchored adaptive banner ad size based on current orientation and screen width
    val adSize = remember(screenWidthDp) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        key(screenWidthDp) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    AdView(ctx).apply {
                        this.adUnitId = adUnitId
                        setAdSize(adSize)
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                AdManager.logAdImpression(ctx, AdManager.AD_TYPE_BANNER, adUnitId)
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                super.onAdFailedToLoad(loadAdError)
                                AdManager.logAdFailedToLoad(
                                    context = ctx,
                                    adType = AdManager.AD_TYPE_BANNER,
                                    adUnitId = adUnitId,
                                    errorCode = loadAdError.code,
                                    errorMessage = loadAdError.message
                                )
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = { adView ->
                    adView.destroy()
                }
            )
        }
    }
}
