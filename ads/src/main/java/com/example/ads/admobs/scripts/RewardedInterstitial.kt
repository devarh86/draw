package com.example.ads.admobs.scripts

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.ads.Constants.OTHER_AD_ON_DISPLAY
import com.example.ads.Constants.firebaseAnalytics
import com.example.ads.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlin.time.Duration.Companion.seconds

class RewardedInterstitial {

    var timeHandler: Handler? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var TAG = "FAHAD_REWARDED_INTERSTITIAL"
    private var isGranted = false

    fun loadRewardedInterstitial(
        activity: Activity,
        adLoaded: () -> Unit,
        failedAction: () -> Unit
    ) {
        if (rewardedInterstitialAd == null) {
            RewardedInterstitialAd.load(
                activity.applicationContext,
                activity.getString(R.string.rewarded_interstitial),
                AdRequest.Builder().build(),
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        Log.d(TAG, "Ad was loaded.")
                        rewardedInterstitialAd = ad
                        adLoaded.invoke()
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e("REASONFAILED", adError.toString())
                        Log.d(TAG, adError.toString())
                        rewardedInterstitialAd = null
                        failedAction.invoke()
                    }
                })
        }
    }

    fun showRewardedInterstitial(
        activity: Activity,
        rewardGrantedAction: () -> Unit,
        failedAction: () -> Unit
    ) {
        rewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                firebaseAnalytics?.logEvent("reward_ad_clicked", null)
                Log.i("new_event", "reward_ad_clicked")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed fullscreen content.")
                OTHER_AD_ON_DISPLAY = false
                rewardedInterstitialAd = null
                if (isGranted) {
                    firebaseAnalytics?.logEvent("rewarded_inters_video", null)
                    firebaseAnalytics?.logEvent("reward_ads_both", null)
                    Log.i("new_event", "rewarded_inters_video")
                    Log.i("new_event", "reward_ads_both")
                    rewardGrantedAction.invoke()
                } else failedAction.invoke()

                timeHandler = Handler(Looper.getMainLooper())
                timeHandler?.postDelayed({
                    loadRewardedInterstitial(activity, {}, { })
                }, 3.seconds.inWholeMilliseconds)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show fullscreen content.")
                OTHER_AD_ON_DISPLAY = false
                rewardedInterstitialAd = null
                firebaseAnalytics?.logEvent("reward_ad_failed", null)
                Log.i("new_event", "reward_ad_failed")
                timeHandler = Handler(Looper.getMainLooper())
                timeHandler?.postDelayed({
                    loadRewardedInterstitial(activity, {}, { })
                }, 1.seconds.inWholeMilliseconds)
            }

            override fun onAdImpression() {
                firebaseAnalytics?.logEvent("reward_ad_impression", null)
                Log.i("new_event", "reward_ad_impression")
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                firebaseAnalytics?.logEvent("reward_ad_show", null)
                Log.i("new_event", "reward_ad_show")
                Log.d(TAG, "Ad showed fullscreen content.")
                OTHER_AD_ON_DISPLAY = true
            }
        }

        rewardedInterstitialAd?.let { ad ->
            ad.show(activity) {
                isGranted = true
            }
        } ?: run {
            failedAction.invoke()
        }
    }
}