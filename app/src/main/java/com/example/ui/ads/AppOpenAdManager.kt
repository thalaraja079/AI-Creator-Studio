package com.example.ui.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.example.data.repository.AppConfigRepository
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenAdManager(
    private val application: Application,
    private val adConfigRepository: AppConfigRepository
) : Application.ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var currentActivity: Activity? = null

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    fun loadAd() {
        if (isLoadingAd || isAdAvailable()) return
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            application,
            adConfigRepository.getAppOpenAdUnitId(),
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoadingAd = false
                }
            }
        )
    }

    private fun isAdAvailable(): Boolean = appOpenAd != null

    fun showAdIfAvailable(activity: Activity) {
        if (!adConfigRepository.getAdsEnabled()) return
        if (isShowingAd || !isAdAvailable()) {
            loadAd()
            return
        }
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                loadAd()
            }
            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                appOpenAd = null
                isShowingAd = false
                loadAd()
            }
            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }
        }
        appOpenAd?.show(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        showAdIfAvailable(activity)
    }

    // Boilerplate ActivityLifecycleCallbacks
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
