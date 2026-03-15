package com.example.ads.admobs.utils

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.ads.Constants
import com.example.ads.Constants.ADS_SDK_INITIALIZE
import com.example.ads.Constants.OTHER_AD_ON_DISPLAY
import com.example.ads.Constants.appIsForeground
import com.example.ads.Constants.appOpen
import com.example.ads.Constants.appOpenSplash
import com.example.ads.Constants.banner
import com.example.ads.Constants.bannerSplash
import com.example.ads.Constants.bigoPopUpOnClick
import com.example.ads.Constants.failureMsg
import com.example.ads.Constants.interstitial
import com.example.ads.Constants.interstitialOnClick
import com.example.ads.Constants.loadInterstitialSave
import com.example.ads.Constants.mNewInterstitial
import com.example.ads.Constants.native
import com.example.ads.Constants.needToLoadAppOpen
import com.example.ads.Constants.newAdsConfig
import com.example.ads.Constants.rewarded
import com.example.ads.Constants.rewardedInterstitial
import com.example.ads.Constants.rewardedShown
import com.example.ads.Constants.showAllAppOpenAd
import com.example.ads.Constants.showAllInterstitialAd
import com.example.ads.R
import com.example.ads.admobs.scripts.AppOpen
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.isNetworkAvailable
import com.example.ads.crosspromo.helper.show
import com.example.ads.model.AdConfigModel
import com.example.ads.utils.allBanner
import com.example.inapp.helpers.Constants.isProVersion
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.project.common.utils.ConstantsCommon.showInterstitialAd
import java.lang.ref.WeakReference

fun Activity?.loadAppOpen() {

    val appOpenAd = newAdsConfig?.appOpenResume
    appOpenAd?.let {
        loadAppOpenSequentially(
            onAdLoaded = {},
            onAdFailed = {},
            adUnits = listOfNotNull(
                it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this ?: return, R.string.resume_app_open_low),
                it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this ?: return, R.string.resume_app_open_low),
                it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this ?: return, R.string.resume_app_open_low)
            )
        )
    }
}

fun Activity?.loadAppOpenSplash(
    isFromSplash: Boolean = false
) {
    val appOpenAd = newAdsConfig?.splashScreen?.appOpen
    appOpenAd?.let {
        loadAppOpenSequentially(
            isFromSplash = isFromSplash,
            onAdLoaded = {},
            onAdFailed = {},
            adUnits = listOfNotNull(
                it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this ?: return, R.string.splash_app_open_low),
                it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this ?: return, R.string.splash_app_open_low),
                it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this ?: return, R.string.splash_app_open_low)
            )
        )
    }
}


fun Activity?.loadAppOpenSequentially(
    isFromSplash: Boolean = false,
    onAdLoaded: () -> Unit,
    onAdFailed: () -> Unit,
    adUnits: List<String> = listOfNotNull(this?.getString(R.string.splash_app_open_low)),
) {
    if (isProVersion() || adUnits.isEmpty()) {
        onAdFailed.invoke()
        return
    }

    val adUnitId = adUnits.first()
    Log.e("TAGSequentially", "loadAppOpenSequentially: ")
    Log.e("TAGSequentially", "${this?.javaClass?.simpleName}.loadAppOpenSequentially_loading (${adUnitId.substringAfterLast("/")})")

    this?.let { activity ->
        if (!appIsForeground) {
            onAdFailed.invoke()
            return
        }

        if (isFromSplash && newAdsConfig?.splashScreen?.appOpen?.isEnabled != true) {
            onAdFailed.invoke()
            return
        } else if (!isFromSplash && newAdsConfig?.appOpenResume?.isEnabled != true) {
            onAdFailed.invoke()
            return
        }

        val appOpenLoadObject = if (isFromSplash) {
            appOpenSplash
        } else {
            appOpen
        }

        if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS && needToLoadAppOpen) {
            Log.e("TAGSequentially", "${this.javaClass.simpleName}.Attempting to load ad: $adUnitId")
            appOpenLoadObject.loadAd(
                context = activity.applicationContext,
                adUnitId = adUnitId,
                onAdLoaded = { onAdLoaded.invoke() },
                onAdFailed = {
                    Log.e("TAGSequentially", "${this.javaClass.simpleName}.Ad failed: $adUnitId, trying next")
                    loadAppOpenSequentially(isFromSplash = isFromSplash, onAdLoaded, onAdFailed, adUnits.drop(1))
                }
            )
        } else {
            Log.e("TAGSequentially", "${this.javaClass.simpleName}.Initializing Ad SDK for ad: $adUnitId")
            MobileAds().initialize(activity.applicationContext) {
                ADS_SDK_INITIALIZE.set(true)
                loadAppOpenSequentially(isFromSplash = isFromSplash, onAdLoaded, onAdFailed, adUnits)
            }
        }
    } ?: onAdFailed.invoke()
}


fun Activity?.showAppOpenSplash(onCompleteAction: () -> Unit) {
    this?.let {
        //
        if (isNetworkAvailable() && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground && !Constants.actionGallery && showAllAppOpenAd && Constants.loadSplashAppOpen) {
            appOpenSplash.showAdIfAvailable(
                this,
                object : AppOpen.OnShowAdCompleteListener {
                    override fun onShowAdComplete() {
                        needToLoadAppOpen = true
                        onCompleteAction.invoke()
                    }

                    override fun adDismiss() {
                        Log.i("TAG", "adDismiss:  adDismiss ")
                        resetCounter()
                        showInterstitialAd = false
                        needToLoadAppOpen = true
                        onCompleteAction.invoke()
                    }

                    override fun onAdFailed() {
                        showInterstitialAd = true
                        needToLoadAppOpen = true
                        onCompleteAction.invoke()
                    }
                }
            )
        } else {
            Constants.actionGallery = false
            onCompleteAction.invoke()
        }
    } ?: run {
        onCompleteAction.invoke()
    }
}

fun Activity?.showAppOpen(onCompleteAction: () -> Unit) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground && !Constants.actionGallery && showAllAppOpenAd) {
            appOpen.showAdIfAvailable(
                this,
                object : AppOpen.OnShowAdCompleteListener {
                    override fun onShowAdComplete() {
                        needToLoadAppOpen = true
                        onCompleteAction.invoke()
                    }

                    override fun adDismiss() {
                        Log.i("TAG", "adDismiss:  adDismiss ")
                        resetCounter()
                        showInterstitialAd = false
                        needToLoadAppOpen = true
                        onCompleteAction.invoke()
                    }

                    override fun onAdFailed() {
                        showInterstitialAd = true
                        needToLoadAppOpen = true
                        onCompleteAction.invoke()
                    }
                }
            )
        } else {
            Constants.actionGallery = false
            onCompleteAction.invoke()
        }
    } ?: run {
        onCompleteAction.invoke()
    }
}

fun Activity?.onResumeBanner(
    container: ConstraintLayout,
    crossBanner: ImageView?,
    frameLayout: FrameLayout,
    shimmerFrameLayout: ShimmerFrameLayout,
    loadNewAd: Boolean = false,
    fromEditor: Boolean = false,
    fromButton: Boolean = false,
) {
    kotlin.runCatching {
        crossBanner?.let {
            crossBanner.visibility = View.INVISIBLE
            /*   if (!crossBanner.hasOnClickListeners()) {
                   crossBanner.setOnClickListener {
                       this?.openUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.xenstudio.garden.photoframe"))
                   }
               }*/
        }
    }
    this?.let {
        if (isNetworkAvailable() && !isProVersion()) {
            banner.onResume()
            showAdaptiveBanner(
                container,
                crossBanner,
                frameLayout,
                shimmerFrameLayout,
                loadNewAd,
                fromEditor = fromEditor,
                fromButton
            )
        } else {
            container.hide()
            frameLayout.hide()
            shimmerFrameLayout.hide()
            container.parent?.let {
                if (it is ConstraintLayout) {
                    it.isVisible = false
                }
            }
        }
    }
}

fun onPauseBanner() {
    if (!isProVersion()) banner.onPause()
}

fun Activity?.showAdaptiveBanner(
    container: ConstraintLayout,
    crossBanner: ImageView?,
    frameLayout: FrameLayout,
    shimmerFrameLayout: ShimmerFrameLayout,
    loadNewAd: Boolean = true,
    fromEditor: Boolean = false,
    fromButton: Boolean = false,
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion()) {
            banner.adConfig = this.allBanner()
            if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) banner.showAdaptiveBannerAd(
                this,
                container,
                crossBanner,
                frameLayout,
                shimmerFrameLayout,
                loadNewAd = loadNewAd,
                fromEditor = fromEditor,
                fromButton = fromButton
            )
            else {
                try {
                    MobileAds().initialize(application) { ADS_SDK_INITIALIZE.set(true) }
                } catch (_: Exception) {
                }
                frameLayout.hide()
                shimmerFrameLayout.hide()
            }
        } else {
            container.hide()
            frameLayout.hide()
            shimmerFrameLayout.hide()
        }
    } ?: run {
        container.hide()
        frameLayout.hide()
        shimmerFrameLayout.hide()
    }
}

fun Activity?.loadAndShowNativeOnBoarding(
    loadedAction: (nativeAdView: NativeAdView?) -> Unit,
    failedAction: () -> Unit,
    config: AdConfigModel?,
    nextConfig: AdConfigModel? = null,
    fromReload: Boolean = false,
    same: Boolean = false,
    adImpression: (Boolean) -> Unit = {},
    showContainer: (() -> Unit)? = null,

    ) {
    this?.let {

        if (config != null && config.enable && !isProVersion() && isNetworkAvailable() && appIsForeground) {
            showContainer?.invoke()
            native.adConfig = config
            if (same) {
                native.currentFragmentOrActivity.add(config.currentActivityOrFragment)
            } else {
                native.currentFragmentOrActivity.clear()
                native.currentFragmentOrActivity.add(config.currentActivityOrFragment)
            }
            native.loadAndShowNativeOnBoarding(
                this,
                loadedAction,
                failedAction,
                nextConfig,
                fromReload,
                adImpression = adImpression,
            )
        } else {
            failedAction.invoke()
        }
    } ?: run {
        failedAction.invoke()
    }
}

fun Fragment?.loadAndShowOnBoardingAds(
    obj: OnBoardingAds,
    nativeAdConfigCurrent: AdConfigModel?,
    nativeAdConfigNext: AdConfigModel?
) {
    this?.let {
        val internetAvailable = try {
            it.activity.isNetworkAvailable()
        } catch (_: java.lang.Exception) {
            false
        }
        if (it.isVisible && !it.isDetached) {
            if (!isProVersion() && internetAvailable && Constants.appIsForeground) {
                runCatching {
                    it.activity?.let { activity ->
                        obj.apply {
                            parentContainer.get()?.show()
                            nativeContainer.get()?.show()
                            if (shimmerNativeFrameLayout.get()?.isVisible == true) shimmerNativeFrameLayout.get()
                                ?.startShimmer()

                            activity.loadAndShowNativeOnBoarding(
                                loadedAction = { loadedValue ->
                                    kotlin.runCatching {
                                        if (isVisible && !isDetached) {

                                            nativeContainer.get()?.let { nativeContainer ->
                                                nativeAdContainer.get()
                                                    ?.let { nativeAdContainer ->
                                                        shimmerNativeFrameLayout.get()
                                                            ?.let { shimmerNativeFrameLayout ->
                                                                nativeContainer.show()
                                                                nativeAdContainer.show()
                                                                shimmerNativeFrameLayout.visibility =
                                                                    View.INVISIBLE
                                                                nativeAdContainer.removeAllViews()
                                                                if (loadedValue?.parent != null) {
                                                                    (loadedValue.parent as ViewGroup).removeView(
                                                                        loadedValue
                                                                    )
                                                                }
                                                                if (isVisible && !isDetached) {
                                                                    nativeAdContainer.addView(
                                                                        loadedValue
                                                                    )
                                                                }
                                                            }
                                                    }
                                            }
                                        }
                                    }
                                },
                                failedAction = {

                                    if (isVisible && !isDetached) {

                                        nativeContainer.get()?.let { nativeContainer ->
                                            nativeAdContainer.get()
                                                ?.let { nativeAdContainer ->
                                                    shimmerNativeFrameLayout.get()
                                                        ?.let { shimmerNativeFrameLayout ->
                                                            shimmerNativeFrameLayout.show()
                                                            nativeAdContainer.hide()
                                                            nativeContainer.visibility = View.INVISIBLE
                                                        }
                                                }
                                        }
                                    }
                                },
                                config = nativeAdConfigCurrent,
                                nextConfig = nativeAdConfigNext
                            )
                        }
                    }
                }
            } else {
                obj.apply {
                    parentContainer.get()?.hide()
                    nativeContainer.get()?.visibility = View.INVISIBLE
                    bannerContainer.get()?.hide()
                }
            }
        }
    }
}

@Keep
data class OnBoardingAds(
    val parentContainer: WeakReference<FrameLayout>,

    val nativeContainer: WeakReference<ConstraintLayout>,
    val nativeAdContainer: WeakReference<FrameLayout>,
    val shimmerNativeFrameLayout: WeakReference<ShimmerFrameLayout>,

    val bannerContainer: WeakReference<ConstraintLayout>,
    val bannerAdContainer: WeakReference<FrameLayout>,
    val shimmerBannerFrameLayout: WeakReference<ShimmerFrameLayout>,
    val crossPromoImgView: WeakReference<ImageView>,
    var position: Int = 0,
    var isBanner: Boolean = false,
    var isMedium: Boolean = false,
)

fun Activity?.loadNative(
    loadedAction: (nativeAd: NativeAd?) -> Unit,
    failedAction: () -> Unit,
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) native.loadNative(
                this,
                loadedAction,
                failedAction
            )
            else MobileAds().initialize(this.applicationContext) { ADS_SDK_INITIALIZE.set(true) }
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.preLoadNative(
    config: AdConfigModel?,
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) {
            native.adConfig = config
            if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) native.loadNextNative(
                this
            )
            else MobileAds().initialize(this.applicationContext) { ADS_SDK_INITIALIZE.set(true) }
        }
    } ?: run {

    }
}

fun Activity?.showNative(
    nativeAdLayout: Int,
    nativeAd: NativeAd,
    loadedAction: (nativeAdView: NativeAdView?) -> Unit,
    failedAction: () -> Unit,
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) native.populateNativeAdView(
            this,
            nativeAdLayout,
            nativeAd,
            loadedAction,
            failedAction
        )
        else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}


/*
fun Activity?.loadAndShowNative(
    nativeAdLayout: Int,
    loadedAction: (nativeAdView: NativeAdView?) -> Unit,
    failedAction: () -> Unit,
    adId: String? = this?.getString(R.string.native_advanced_video)
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground  && adId != null) native.loadAndShowNative(
            this,
            nativeAdLayout,
            adId,
            loadedAction,
            failedAction
        )
        else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.loadNative(
    loadedAction: (nativeAd: NativeAd?) -> Unit,
    failedAction: () -> Unit
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) native.loadNative(
                this,
                loadedAction,
                failedAction
            )
            else MobileAds().initialize(this.applicationContext) { ADS_SDK_INITIALIZE.set(true) }
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.showNative(
    nativeAdLayout: Int,
    nativeAd: NativeAd,
    loadedAction: (nativeAdView: NativeAdView?) -> Unit,
    failedAction: () -> Unit
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) native.populateNativeAdView(
            this,
            nativeAdLayout,
            nativeAd,
            loadedAction,
            failedAction
        )
        else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}
*/

fun Activity?.loadNewInterstitialForPro(
    adConfigModel: AdConfigModel?,
    callBack: () -> Unit,
) {
    this?.let {
        adConfigModel?.let { config ->
            if (showAllInterstitialAd && isNetworkAvailable() && !isProVersion() && Constants.appIsForeground && config.enable) {
                if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) {
                    mNewInterstitial?.loadInterstitial(it, config, callBack)
                } else MobileAds().initialize(this.applicationContext) {
                    ADS_SDK_INITIALIZE.set(true)
                    mNewInterstitial?.loadInterstitial(it, config, callBack)
                }
            } else {
                callBack.invoke()
            }
        } ?: run {
            callBack.invoke()
        }
    } ?: run {
        callBack.invoke()
    }
}

fun Activity?.loadNewInterstitialWithoutStrategyCheck(
    adConfigModel: AdConfigModel?,
    callBack: () -> Unit,
) {
    this?.let {
        callBack.invoke()
        adConfigModel?.let { config ->
            if (showAllInterstitialAd && isNetworkAvailable() && !isProVersion() && Constants.appIsForeground && config.enable) {
                if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) {
                    mNewInterstitial?.loadInterstitial(it, config, callBack)
                } else MobileAds().initialize(this.applicationContext) {
                    ADS_SDK_INITIALIZE.set(true)
                    mNewInterstitial?.loadInterstitial(it, config, callBack)
                }
            } else {
                callBack.invoke()
            }
        } ?: run {
            callBack.invoke()
        }
    } ?: run {
        callBack.invoke()
    }
}

fun Activity?.loadNewInterstitial(
    adConfigModel: AdConfigModel?,
    callBack: () -> Unit,
) {
    this?.let {
        adConfigModel?.let { config ->
            if (showAllInterstitialAd && isNetworkAvailable() && !isProVersion() && Constants.appIsForeground && config.enable) {
                if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) {
                    mNewInterstitial?.loadInterstitial(it, config, callBack)
                } else MobileAds().initialize(this.applicationContext) {
                    ADS_SDK_INITIALIZE.set(true)
                    mNewInterstitial?.loadInterstitial(it, config, callBack)
                }
            } else {
                callBack.invoke()
            }
        } ?: run {
            callBack.invoke()
        }
    } ?: run {
        callBack.invoke()
    }
}

fun Activity?.showNewInterstitialPro(
    adConfigModel: AdConfigModel?,
    callBack: () -> Unit,
) {
    this?.let {
        adConfigModel?.let { config ->
            Log.i("TAG", "showNewInterstitial: $showInterstitialAd")
            if (showAllInterstitialAd && isNetworkAvailable() && !isProVersion() && Constants.appIsForeground && showInterstitialAd && config.enable && !OTHER_AD_ON_DISPLAY) {
                if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) {
                    mNewInterstitial?.showInterstitial(it, config, callBack)
                } else {
                    runCatching {
                        MobileAds().initialize(this.applicationContext) {
                            ADS_SDK_INITIALIZE.set(true)
                        }
                        callBack.invoke()
                    }
                }
            } else {
                showInterstitialAd = true
                callBack.invoke()
            }
        } ?: run {
            showInterstitialAd = true
            callBack.invoke()
        }
    } ?: run {
        showInterstitialAd = true
        callBack.invoke()
    }
}

fun Activity?.showNewInterstitial(
    adConfigModel: AdConfigModel?,
    callBack: () -> Unit,
) {
    this?.let {
        adConfigModel?.let { config ->
            Log.i("TAG", "showNewInterstitial: $showInterstitialAd")
            if (showAllInterstitialAd && isNetworkAvailable() && !isProVersion() && Constants.appIsForeground && showInterstitialAd && config.enable && !OTHER_AD_ON_DISPLAY) {
                if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) {
                    mNewInterstitial?.showInterstitial(it, config, callBack)
                } else {
                    runCatching {
                        MobileAds().initialize(this.applicationContext) {
                            ADS_SDK_INITIALIZE.set(true)
                        }
                        callBack.invoke()
                    }
                }
            } else {
                showInterstitialAd = true
                callBack.invoke()
            }
        } ?: run {
            showInterstitialAd = true
            callBack.invoke()
        }
    } ?: run {
        showInterstitialAd = true
        callBack.invoke()
    }
}


fun Activity?.loadInterstitial(
    loadedAction: () -> Unit,
    failedAction: () -> Unit,
    withSplash: Boolean = false,
    withSave: Boolean = false,
    config: AdConfigModel? = null,
) {
    this?.let {
        interstitial.adConfig = config
        if (showAllInterstitialAd && isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) interstitial.loadInterstitial(
                this.applicationContext,
                loadedAction,
                failedAction,
                withSplash,
                withSave
            )
            else MobileAds().initialize(this.applicationContext) {
                ADS_SDK_INITIALIZE.set(true)
                interstitial.loadInterstitial(
                    this.applicationContext,
                    loadedAction,
                    failedAction
                )
            }
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.showInterstitial(
    loadedAction: () -> Unit,
    failedAction: () -> Unit,
    isSave: Boolean = false, /*this is a temporary check for ANR controlling ---  check othe checks as well in this class on isSessionBase Method*/
    reload: Boolean = true,
    showAd: Boolean = false,
    onCheck: Boolean = false,
) {
    this?.let {
        if (showAllInterstitialAd && isSave && isNetworkAvailable() && showInterstitialAd && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground && !rewardedShown/*&& isSessionBase()*/) {
            interstitial.showInterstitial(
                this,
                loadedAction,
                failedAction,
                reload
            )
//            interstitialAdCounter += 1
        } else if (showAllInterstitialAd && showAd && onCheck && isNetworkAvailable() && showInterstitialAd && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground && !rewardedShown && interstitialAdCounter.isThird()) {
            interstitial.showInterstitial(
                this,
                loadedAction,
                failedAction,
                reload
            )
            interstitialAdCounter += 1
        } else {
            showInterstitialAd = true
            failedAction.invoke()

            if (showAd) {
                interstitialAdCounter += 1
            }

        }
    } ?: run {
        failedAction.invoke()
        interstitialAdCounter += 1
    }
}

private var bigoAdCounter = 1

fun Int.isThirdBigo(): Boolean = (this % bigoPopUpOnClick).toInt() == 0

private var interstitialAdCounter = 1
private var onClick = interstitialOnClick.toInt()

fun setOnClick() {
    onClick = interstitialOnClick.toInt()
}

fun Int.isThird(): Boolean = this % onClick == 0

fun resetCounter() {
    interstitialAdCounter = 1
}


fun Activity?.loadRewardedSave(
    loadedAction: () -> Unit,
    failedAction: () -> Unit,
) {

    val appRewardAd = newAdsConfig?.rewarded
    appRewardAd?.let {
        loadRewardAdSequentiallySave(
            onAdLoaded = { loadedAction() },
            onAdFailed = { failedAction() },
            adUnits = listOfNotNull(
                it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this ?: return, R.string.rewarded_low),
                it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this ?: return, R.string.rewarded_low),
                it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this ?: return, R.string.rewarded_low)
            )
        )
    } ?: run {
        failedAction.invoke()
    }
    /*this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) rewarded.loadRewarded(
                this,
                loadedAction,
                failedAction
            )
            else MobileAds().initialize(this.applicationContext) { ADS_SDK_INITIALIZE.set(true) }
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }*/
}

fun Activity?.loadRewardAdSequentiallySave(
    onAdLoaded: () -> Unit,
    onAdFailed: () -> Unit,
    adUnits: List<String> = listOfNotNull(this?.getString(R.string.rewarded_low)),
) {
    if (isProVersion() || adUnits.isEmpty()) {
        onAdFailed.invoke()
        return
    }

    val adUnitId = adUnits.first()
    Log.e("TAGRewardSequentially", "loadRewardSequentially: ")
    Log.e("TAGRewardSequentially", "${this?.javaClass?.simpleName}.loadRewardSequentially_loading (${adUnitId.substringAfterLast("/")})")

    this?.let { activity ->
        if (!appIsForeground) {
            onAdFailed.invoke()
            return
        }

        if (newAdsConfig?.rewarded?.isEnabled == false) {
            onAdFailed.invoke()
            return
        }

        val appRewardObject = rewarded

        if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) {
            Log.e("TAGRewardSequentially", "${this.javaClass.simpleName}.Attempting to load ad: $adUnitId")
            appRewardObject.loadRewarded(
                activity = this,
                adUnitId = adUnitId,
                adLoaded = { onAdLoaded.invoke() },
                failedAction = {
                    Log.e("TAGRewardSequentially", "${this.javaClass.simpleName}.Ad failed: $adUnitId, trying next")
                    loadRewardAdSequentiallySave(onAdLoaded, onAdFailed, adUnits.drop(1))
                }
            )
        } else {
            Log.e("TAGRewardSequentially", "${this.javaClass.simpleName}.Initializing Ad SDK for ad: $adUnitId")
            MobileAds().initialize(activity.applicationContext) {
                ADS_SDK_INITIALIZE.set(true)
                loadRewardAdSequentiallySave(onAdLoaded, onAdFailed, adUnits)
            }
        }
    } ?: onAdFailed.invoke()
}

fun Activity?.loadRewarded(
    loadedAction: () -> Unit,
    failedAction: () -> Unit,
) {

    val appRewardAd = newAdsConfig?.rewarded
    appRewardAd?.let {
        loadRewardAdSequentially(
            onAdLoaded = { loadedAction() },
            onAdFailed = { failedAction() },
            adUnits = listOfNotNull(
                it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this ?: return, R.string.rewarded_low),
                it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this ?: return, R.string.rewarded_low),
                it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this ?: return, R.string.rewarded_low)
            )
        )
    } ?: run {
        failedAction.invoke()
    }
    /*this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) rewarded.loadRewarded(
                this,
                loadedAction,
                failedAction
            )
            else MobileAds().initialize(this.applicationContext) { ADS_SDK_INITIALIZE.set(true) }
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }*/
}

fun Activity?.loadRewardAdSequentially(
    onAdLoaded: () -> Unit,
    onAdFailed: () -> Unit,
    adUnits: List<String> = listOfNotNull(this?.getString(R.string.rewarded_low)),
) {
    if (isProVersion() || adUnits.isEmpty()) {
        onAdFailed.invoke()
        return
    }

    val adUnitId = adUnits.first()
    Log.e("TAGRewardSequentially", "loadRewardSequentially: ")
    Log.e("TAGRewardSequentially", "${this?.javaClass?.simpleName}.loadRewardSequentially_loading (${adUnitId.substringAfterLast("/")})")

    this?.let { activity ->
        if (!appIsForeground) {
            onAdFailed.invoke()
            return
        }

        if (newAdsConfig?.rewarded?.isEnabled == false) {
            onAdFailed.invoke()
            return
        }

        val appRewardObject = rewarded

        if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) {
            Log.e("TAGRewardSequentially", "${this.javaClass.simpleName}.Attempting to load ad: $adUnitId")
            appRewardObject.loadRewarded(
                activity = this,
                adUnitId = adUnitId,
                adLoaded = { onAdLoaded.invoke() },
                failedAction = {
                    Log.e("TAGRewardSequentially", "${this.javaClass.simpleName}.Ad failed: $adUnitId, trying next")
                    loadRewardAdSequentially(onAdLoaded, onAdFailed, adUnits.drop(1))
                }
            )
        } else {
            Log.e("TAGRewardSequentially", "${this.javaClass.simpleName}.Initializing Ad SDK for ad: $adUnitId")
            MobileAds().initialize(activity.applicationContext) {
                ADS_SDK_INITIALIZE.set(true)
                loadRewardAdSequentially(onAdLoaded, onAdFailed, adUnits)
            }
        }
    } ?: onAdFailed.invoke()
}


fun Activity?.showRewarded(
    loadedAction: () -> Unit,
    failedAction: () -> Unit,
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) rewarded.showRewarded(
                this,
                rewardGrantedAction = {
                    resetCounter()
                    showInterstitialAd = false
                    loadedAction.invoke()
                }, {
                    kotlin.runCatching {
                        Toast.makeText(it, failureMsg, Toast.LENGTH_SHORT).show()
                    }
                    failedAction.invoke()
                }
            )
            else {
                MobileAds().initialize(this.applicationContext) { ADS_SDK_INITIALIZE.set(true) }
                kotlin.runCatching {
                    Toast.makeText(it, failureMsg, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            if (!isProVersion()) {
                kotlin.runCatching {
                    Toast.makeText(it, failureMsg, Toast.LENGTH_SHORT).show()
                }
            }
            failedAction.invoke()
        }
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.loadRewardedInterstitial(
    loadedAction: () -> Unit,
    failedAction: () -> Unit,
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) rewardedInterstitial.loadRewardedInterstitial(
                this,
                loadedAction,
                failedAction
            )
            else MobileAds().initialize(this.applicationContext) { ADS_SDK_INITIALIZE.set(true) }
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.showRewardedInterstitial(
    showRewardAd: Boolean = false,
    loadedAction: () -> Unit,
    failedAction: () -> Unit,
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) {
                if (newAdsConfig?.rewarded?.isEnabled == true || showRewardAd) {
                    rewarded.showRewarded(this, rewardGrantedAction = {
                        rewardedShown = true
                        showInterstitialAd = false
                        resetCounter()
                        loadedAction.invoke()
                    }, {
                        kotlin.runCatching {
                            Toast.makeText(it, failureMsg, Toast.LENGTH_SHORT).show()
                        }
                        failedAction.invoke()
                    })
                } else {
                    failedAction.invoke()
                    /* rewardedInterstitial.showRewardedInterstitial(
                         this,
                         rewardGrantedAction = {
                             rewardedShown = true
                             showInterstitialAd = false
                             resetCounter()
                             loadedAction.invoke()
                         },
                         {
                             kotlin.runCatching {
                                 Toast.makeText(it, failureMsg, Toast.LENGTH_SHORT).show()
                             }
                             failedAction.invoke()
                         }
                     )*/
                }
            } else {
                MobileAds().initialize(this.applicationContext) { ADS_SDK_INITIALIZE.set(true) }
                if (!isProVersion()) {
                    kotlin.runCatching {
                        Toast.makeText(it, failureMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            if (!isProVersion()) {
                kotlin.runCatching {
                    Toast.makeText(it, failureMsg, Toast.LENGTH_SHORT).show()
                }
            }
            failedAction.invoke()
        }
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.showSplashAdaptiveBanner(
    container: ConstraintLayout,
    crossBanner: ImageView?,
    frameLayout: FrameLayout,
    shimmerFrameLayout: ShimmerFrameLayout,
    loadNewAd: Boolean = false,
    config: AdConfigModel? = null
) {

    this?.let {
        if (isNetworkAvailable() && !isProVersion()) {
            if (ADS_SDK_INITIALIZE.get() && Constants.CAN_LOAD_ADS) {
                bannerSplash.adConfig = config
                bannerSplash.showAdaptiveBannerAd(
                    this,
                    container,
                    crossBanner,
                    frameLayout,
                    shimmerFrameLayout,
                    loadNewAd = loadNewAd
                )
            } else {
                try {
                    MobileAds().initialize(application) { ADS_SDK_INITIALIZE.set(true) }
                } catch (_: Exception) {
                    Log.e("TAG", "showSplashAdaptiveBanner: ex")
                }
                frameLayout.hide()
                shimmerFrameLayout.hide()
            }
        } else {
            container.hide()
            frameLayout.hide()
            shimmerFrameLayout.hide()
        }
    } ?: run {
        container.hide()
        frameLayout.hide()
        shimmerFrameLayout.hide()
    }
}

fun Activity?.onResumeSplashBanner(
    container: ConstraintLayout,
    crossBanner: ImageView?,
    frameLayout: FrameLayout,
    shimmerFrameLayout: ShimmerFrameLayout,
    loadNewAd: Boolean = false,
    config: AdConfigModel? = null
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion()) {
            bannerSplash.onResume()
            showSplashAdaptiveBanner(
                container,
                crossBanner,
                frameLayout,
                shimmerFrameLayout,
                loadNewAd,
                config
            )
        } else {
            container.hide()
            frameLayout.hide()
            shimmerFrameLayout.hide()
        }
    }
}

fun onPauseSplashBanner() {
    if (!isProVersion()) bannerSplash.onPause()
}

fun Activity?.showInterstitialSplash(
    loadedAction: () -> Unit,
    failedAction: () -> Unit,
) {
    this?.let {
        // if ((loadInterstitialSplash) && showAllInterstitialAd && isNetworkAvailable() && showInterstitialAd && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground && !rewardedShown) {
        if (isNetworkAvailable() && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground && !rewardedShown) {
            interstitial.showSplashInterstitial(
                this,
                loadedAction,
                failedAction,
                false
            )
        } else {
            showInterstitialAd = true
            failedAction.invoke()
        }
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.showInterstitialSave(
    loadedAction: () -> Unit,
    failedAction: () -> Unit
) {
    this?.let {
        if ((loadInterstitialSave) && isNetworkAvailable() && showInterstitialAd && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground && !rewardedShown) {
            interstitial.showInterstitial(
                this,
                loadedAction,
                failedAction,
                false,
                forSave = true

            )
        } else {
            showInterstitialAd = true
            failedAction.invoke()
        }
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.showInterstitialEnhance(
    loadedAction: () -> Unit,
    failedAction: () -> Unit,
) {
    this?.let {
        if ((loadInterstitialSave) && isNetworkAvailable() && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground) {
            interstitial.showInterstitial(
                this,
                loadedAction,
                failedAction,
                false,
                forSave = true

            )
        } else {
            showInterstitialAd = true
            failedAction.invoke()
        }
    } ?: run {
        failedAction.invoke()
    }
}





