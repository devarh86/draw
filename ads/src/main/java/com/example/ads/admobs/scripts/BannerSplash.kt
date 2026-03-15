package com.example.ads.admobs.scripts

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowMetrics
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.ads.Constants.splashBannerReloadLimit
import com.example.ads.R
import com.example.ads.model.AdConfigModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class BannerSplash {

    private var bannerView: AdView? = null
    private var bannerFailed = 0
    private var isMedium = false
    var adConfig: AdConfigModel? = null
    private fun loadAdaptiveBanner(
        activity: Activity,
        container: ConstraintLayout,
        crossBanner: ImageView?,
        frameLayout: FrameLayout,
        shimmerFrameLayout: ShimmerFrameLayout,
        bnrLoadingFailed: Boolean = false
    ) {

        bannerView = AdView(activity.applicationContext)
        bannerView?.apply {

            adUnitId = when {
                !bnrLoadingFailed && splashBannerReloadLimit >= 2 -> {
                    Log.i("BannerSplash", "loadAdaptiveBanner: config.idHigh")
                    adConfig?.idHigh ?: activity.getString(R.string.splash_banner_low)
                }

                (bnrLoadingFailed && splashBannerReloadLimit == 2 && bannerFailed == 1) || (splashBannerReloadLimit == 1 && !bnrLoadingFailed) -> {
                    Log.i("BannerSplash", "loadAdaptiveBanner: config.idMedium")
                    adConfig?.idMedium ?: activity.getString(R.string.splash_banner_low)
                }

                else -> {
                    Log.i("BannerSplash", "loadAdaptiveBanner: config.idBackUp")
                    adConfig?.idBackUp ?: activity.getString(R.string.splash_banner_low)
                }
            }

//            setAdSize(adSizeBanner(activity))
            setAdSize(AdSize.MEDIUM_RECTANGLE)
            val adRequest = AdRequest.Builder().build()
            loadAd(adRequest)
            adListener = object : AdListener() {
                override fun onAdClicked() {}
                override fun onAdClosed() {}
                override fun onAdOpened() {}
                override fun onAdImpression() {}
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("REASONFAILEDBanner", adError.toString())
                    Log.d("FAHAD_BANNER", "onAdFailedToLoad: ${adError.message}")
                    bannerView = null
                    bannerFailed += 1

                    //  crossBanner?.visibility = View.VISIBLE
                    if (bannerFailed <= splashBannerReloadLimit) {
                        if (splashBannerReloadLimit > 2) bannerFailed = splashBannerReloadLimit.toInt() + 1
                        loadAdaptiveBanner(
                            activity,
                            container,
                            crossBanner,
                            frameLayout,
                            shimmerFrameLayout,
                            true
                        )
                    } else {
                        container.visibility = View.GONE
                        frameLayout.visibility = View.GONE
                        shimmerFrameLayout.visibility = View.GONE
                        bannerFailed = 0
                    }

                }

                override fun onAdLoaded() {
                    bannerFailed = 0
                    Log.d("FAHAD_BANNER", "onAdLoaded")
                    container.visibility = View.VISIBLE
                    frameLayout.visibility = View.VISIBLE
                    crossBanner?.visibility = View.INVISIBLE
                    shimmerFrameLayout.visibility = View.GONE

                    frameLayout.removeAllViews()
                    frameLayout.addView(this@apply)
                }
            }
        }
    }

    fun showAdaptiveBannerAd(
        activity: Activity,
        container: ConstraintLayout,
        crossBanner: ImageView?,
        frameLayout: FrameLayout,
        shimmerFrameLayout: ShimmerFrameLayout,
        loadNewAd: Boolean = false
    ) {
        bannerView?.let {
            frameLayout.visibility = View.VISIBLE
            container.visibility = View.VISIBLE
            shimmerFrameLayout.visibility = View.GONE
            frameLayout.removeAllViews()
            if (it.parent != null) {
                (it.parent as ViewGroup).removeView(it)
            }
            frameLayout.addView(it)
            if (loadNewAd) {
                loadAdaptiveBanner(
                    activity,
                    container,
                    crossBanner,
                    frameLayout,
                    shimmerFrameLayout
                )
            }
        } ?: run {
            loadAdaptiveBanner(
                activity,
                container,
                crossBanner,
                frameLayout,
                shimmerFrameLayout
            )
        }

    }

    fun onResume() {
        bannerView?.resume()
    }

    fun onPause() {
        bannerView?.pause()
    }

    private fun adSizeBanner(activity: Activity): AdSize {
        val adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics =
                activity.windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            bounds.width().toFloat()
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(
                displayMetrics
            )
            displayMetrics.widthPixels.toFloat()
        }
        val density = activity.applicationContext.resources.displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            activity.applicationContext,
            adWidth
        )
    }
}