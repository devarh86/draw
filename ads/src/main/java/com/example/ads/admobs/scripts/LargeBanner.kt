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
import androidx.core.view.isVisible
import com.example.ads.Constants.loadBannerOnResume
import com.example.ads.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class LargeBanner {

    private var bannerView: AdView? = null

    private var counter = 0

    private fun loadAdaptiveBanner(
        activity: Activity,
        container: ConstraintLayout,
        crossBanner: ImageView?,
        frameLayout: FrameLayout,
        shimmerFrameLayout: ShimmerFrameLayout,
        failedAction: () -> Unit,
        isFloor: Boolean = false
    ) {
        bannerView = AdView(activity.applicationContext)
        bannerView?.apply {
            adUnitId =
                if (!isFloor) activity.applicationContext.getString(R.string.large_banner) else {
                    activity.applicationContext.getString(R.string.large_banner_new)
                }
            setAdSize(AdSize.MEDIUM_RECTANGLE)
            val adRequest = AdRequest.Builder().build()
            loadAd(adRequest)
            adListener = object : AdListener() {
                override fun onAdClicked() {}
                override fun onAdClosed() {
//                    failedAction.invoke()
                }

                override fun onAdOpened() {}
                override fun onAdImpression() {}
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("REASONFAILED", adError.toString())
                    Log.d("FAHAD_BANNER", "onAdFailedToLoad: ${adError.message}")

                    counter += 1
                    if (counter == 1) {
                        Log.i("TAG", "loadAdaptiveBanner: $isFloor")
                        loadAdaptiveBanner(
                            activity,
                            container,
                            crossBanner,
                            frameLayout,
                            shimmerFrameLayout, failedAction, true
                        )
                    } else {
                        container.visibility = View.GONE
                        frameLayout.visibility = View.GONE
                        shimmerFrameLayout.visibility = View.GONE
                        failedAction.invoke()
//                    crossBanner?.visibility = View.VISIBLE
                    }
                }

                override fun onAdLoaded() {
                    Log.d("FAHAD_BANNER", "onAdLoaded")
                    counter = 0
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
        failedAction: () -> Unit,
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
            if (loadBannerOnResume) {
                loadAdaptiveBanner(
                    activity,
                    container,
                    crossBanner,
                    frameLayout,
                    shimmerFrameLayout,
                    failedAction
                )
            }
        } ?: run {
            container.isVisible = true
            frameLayout.isVisible = true
            shimmerFrameLayout.isVisible = true

            loadAdaptiveBanner(
                activity,
                container,
                crossBanner,
                frameLayout,
                shimmerFrameLayout,
                failedAction
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