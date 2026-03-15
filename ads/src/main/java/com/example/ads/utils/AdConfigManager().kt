package com.example.ads.utils

import android.app.Activity
import androidx.core.content.ContextCompat
import com.example.ads.Constants.enableHomeInterAd
import com.example.ads.Constants.interstitialHomeAfterStartCount
import com.example.ads.Constants.interstitialHomeAlwaysShow
import com.example.ads.Constants.interstitialHomeFloor
import com.example.ads.Constants.interstitialHomeStartCount
import com.example.ads.Constants.introScreenAdButtonPosition
import com.example.ads.Constants.introScreenAdUi
import com.example.ads.Constants.loadNativeFullOne
import com.example.ads.Constants.loadNativeFullTwo
import com.example.ads.Constants.loadNativeLfOne
import com.example.ads.Constants.loadNativeLfTwo
import com.example.ads.Constants.loadNativeMetaLayout
import com.example.ads.Constants.loadNativeObOne
import com.example.ads.Constants.loadNativeObThree
import com.example.ads.Constants.loadNativeObTwo
import com.example.ads.Constants.loadNativeOld
import com.example.ads.Constants.nativeReloadLimit
import com.example.ads.Constants.newAdsConfig
import com.example.ads.R
import com.example.ads.model.AdConfigModel
import com.example.ads.model.AdsClassification
import com.example.ads.model.InterstitialAdModel

fun Activity.allBanner(): AdConfigModel? {
    return try {
        newAdsConfig?.appBanner?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.banner_overall_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.banner_overall_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.banner_overall_low),
                enable = it.isEnabled ?: false,
                whichAd = AdsClassification.BANNER,
            )
        }
    } catch (ex: kotlin.Exception) {
        null
    }
}

fun Activity.bannerSplash(): AdConfigModel? {
    return try {
        newAdsConfig?.splashScreen?.banner?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.splash_banner_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.splash_banner_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.splash_banner_low),
                enable = it.isEnabled ?: false,
                reloadLimit = it.reloadLimit ?: 2,
                whichAd = AdsClassification.BANNER,
            )
        }
    } catch (ex: Exception) {
        null
    }
}

fun Activity.nativeExitConfig(): AdConfigModel? {
    return try {
        newAdsConfig?.exitSaveNative?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.native_exit_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.native_exit_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.native_exit_low),
                reloadLimit = it.reloadLimit ?: 2,
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = false,
                enable = it.isEnabled ?: false,
                adMobLayoutId = R.layout.medium_native_ad,
                currentActivityOrFragment = "EXIT"
            )
        } ?: run {
            null
        }
    } catch (ex: Exception) {
        null
    }
}

fun Activity.nativeDialogsConfig(): AdConfigModel? {
    return try {
        newAdsConfig?.processingNative?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.processing_native_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.processing_native_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.processing_native_low),
                reloadLimit = it.reloadLimit ?: 2,
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = false,
                enable = it.isEnabled ?: false,
                adMobLayoutId = R.layout.medium_native_ad,
                currentActivityOrFragment = "Dialog"
            )
        } ?: run {
            null
        }
    } catch (ex: Exception) {
        null
    }
}

fun Activity.nativeProcessingConfigEnhancer(): AdConfigModel? {
    return try {
        newAdsConfig?.processingNative?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.processing_native_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.processing_native_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.processing_native_low),
                reloadLimit = it.reloadLimit ?: 2,
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = false,
                enable = it.isEnabled ?: false,
                adMobLayoutId = R.layout.medium_native_ad,
                currentActivityOrFragment = "GALLERY_PROCESS"
            )
        } ?: run {
            null
        }
    } catch (ex: Exception) {
        null
    }
}


fun Activity.nativeProcessingConfig(): AdConfigModel? {
    return try {
        newAdsConfig?.processingNative?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.processing_native_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.processing_native_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.processing_native_low),
                reloadLimit = it.reloadLimit ?: 2,
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = false,
                enable = it.isEnabled ?: false,
                adMobLayoutId = R.layout.layout_native_large_process,
                metaLayoutId = R.layout.layout_native_large_process,
                currentActivityOrFragment = "GALLERY_PROCESS"
            )
        } ?: run {
            null
        }
    } catch (ex: Exception) {
        null
    }
}

fun Activity.galleryBottom(): AdConfigModel? {
    return try {
        newAdsConfig?.processingNative?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.processing_native_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.processing_native_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.processing_native_low),
                reloadLimit = it.reloadLimit ?: 2,
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = false,
                enable = it.isEnabled ?: false,
                adMobLayoutId = R.layout.layout_native_small_questions,
                metaLayoutId = R.layout.layout_native_small_questions,
                currentActivityOrFragment = "GALLERY"
            )
        } ?: run {
            null
        }
    } catch (ex: Exception) {
        null
    }
}


fun getLayoutIntro(isMeta: Boolean): Int {

    val layoutAdmob = if (introScreenAdButtonPosition == "Above")
        R.layout.layout_native_large_button_above_intro
    else {
        R.layout.layout_native_large_intro
    }
    val layoutMeta = if (introScreenAdButtonPosition == "Above")
        R.layout.meta_native_layout_intro
    else {
        R.layout.meta_native_layout_intro_bottom
    }
    return if (!isMeta) {
        if (introScreenAdUi == "full_layout_meta") {
            layoutMeta
        } else {
            layoutAdmob
        }
    } else {
        if (introScreenAdUi == "full_layout_admob") {
            layoutAdmob
        } else {
            layoutMeta
        }
    }
}

fun Activity.nativeLanguageSetting(): AdConfigModel? {
    return try {
        newAdsConfig?.languageScreen?.nativeAfterSelection?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.native_language_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.native_language_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.native_language_low),
                reloadLimit = it.reloadLimit ?: nativeReloadLimit.toInt(),
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = loadNativeMetaLayout,
                enable = loadNativeLfTwo,
                adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
                metaLayoutId = R.layout.meta_native_layout_onboarding,
                currentActivityOrFragment = "LF_SETTING"
            )
        } ?: run {
            null
        }
    } catch (ex: Exception) {
        null
    }
}

fun nativeSplash(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = "ca-app-pub-4276074242154795/1176659959",
            idMedium = "ca-app-pub-4276074242154795/2853252441",
            idBackUp = "ca-app-pub-4276074242154795/1540170771",
            reloadLimit = 2,
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = loadNativeMetaLayout,
            enable = true,
            adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
            metaLayoutId = R.layout.meta_native_layout_onboarding,
            currentActivityOrFragment = "nativeSplash"
        )
    } catch (ex: Exception) {
        null
    }
}

fun Activity.nativeLanguageOne(): AdConfigModel? {
    return try {
        newAdsConfig?.languageScreen?.nativeBeforeSelection?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.native_language_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.native_language_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.native_language_low),
                reloadLimit = it.reloadLimit ?: nativeReloadLimit.toInt(),
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = loadNativeMetaLayout,
                enable = loadNativeLfOne,
                adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
                metaLayoutId = R.layout.meta_native_layout_onboarding,
                currentActivityOrFragment = "LFO1"
            )
        } ?: run {
            null
        }

    } catch (ex: Exception) {
        null
    }
}

fun Activity.nativeLanguageTwo(): AdConfigModel? {
    return try {
        newAdsConfig?.languageScreen?.nativeAfterSelection?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.native_language_select_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.native_language_select_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.native_language_select_low),
                reloadLimit = it.reloadLimit ?: nativeReloadLimit.toInt(),
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = loadNativeMetaLayout,
                enable = loadNativeLfTwo,
                adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
                metaLayoutId = R.layout.meta_native_layout_onboarding,
                currentActivityOrFragment = "LFO2"
            )
        } ?: run {
            null
        }
    } catch (ex: Exception) {
        null
    }
}

fun Activity.onBoardNativeOne(): AdConfigModel? {
    return try {
        newAdsConfig?.onboarding1Native?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.on_boarding_one_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.on_boarding_one_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.on_boarding_one_low),
                reloadLimit = it.reloadLimit ?: nativeReloadLimit.toInt(),
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = loadNativeMetaLayout,
                enable = loadNativeObOne,
                adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
                metaLayoutId = R.layout.meta_native_layout_onboarding,
                currentActivityOrFragment = "OB1"
            )
        } ?: run {
            null
        }

    } catch (ex: Exception) {
        null
    }
}

fun Activity.onBoardNativeTwo(): AdConfigModel? {
    return try {
        newAdsConfig?.onboarding2Native?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.on_boarding_two_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.on_boarding_two_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.on_boarding_two_low),
                reloadLimit = it.reloadLimit ?: nativeReloadLimit.toInt(),
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = loadNativeMetaLayout,
                enable = loadNativeObTwo,
                adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
                metaLayoutId = R.layout.meta_native_layout_onboarding,
                currentActivityOrFragment = "OB2"
            )
        } ?: run {
            null
        }

    } catch (ex: Exception) {
        null
    }
}

fun Activity.onBoardNativeThree(): AdConfigModel? {
    return try {
        newAdsConfig?.onboarding3Native?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.on_boarding_three_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.on_boarding_three_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.on_boarding_three_low),
                reloadLimit = it.reloadLimit ?: nativeReloadLimit.toInt(),
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = loadNativeMetaLayout,
                enable = loadNativeObThree,
                adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
                metaLayoutId = R.layout.meta_native_layout_onboarding,
                currentActivityOrFragment = "OB3"
            )
        } ?: run {
            null
        }

    } catch (ex: Exception) {
        null
    }
}

fun Activity.onBoardNativeFour(): AdConfigModel? {
    /*return try {
        newAdsConfig?.onboarding4Native?.let {
            AdConfigModel(
                idHigh = onBoardingFourNativeId,
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.on_boarding_four_medium),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.on_boarding_four_back_up),
                reloadLimit = it.reloadLimit ?: nativeReloadLimit.toInt(),
                whichAd = AdsClassification.NATIVE,
                isAboveCtr = false,
                isMetaLayout = loadNativeMetaLayout,
                enable = loadNativeObFour,
                adMobLayoutId = if (loadNativeOld) R.layout.layout_native_large_onboarding_old else R.layout.layout_native_large_onboarding,
                metaLayoutId = R.layout.meta_native_layout_onboarding,
                currentActivityOrFragment = "OB4"
            )
        } ?: run {
            null
        }

    } catch (ex: Exception) {
        null
    }*/

    return null
}

fun Activity.fullNativeOne(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.full_native_one_low),
            idMedium = ContextCompat.getString(this, R.string.full_native_one_low),
            idBackUp = ContextCompat.getString(this, R.string.full_native_one_low),
            reloadLimit = nativeReloadLimit.toInt(),
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = loadNativeMetaLayout,
            enable = loadNativeFullOne,
            adMobLayoutId = R.layout.custom_native_admob_free_size,
            metaLayoutId = R.layout.custom_native_admob_free_size_meta,
            currentActivityOrFragment = "FN1"
        )
    } catch (ex: Exception) {
        null
    }
}

fun Activity.fullNativeTwo(): AdConfigModel? {
    return try {
        AdConfigModel(
            idHigh = ContextCompat.getString(this, R.string.full_native_two_low),
            idMedium = ContextCompat.getString(this, R.string.full_native_two_low),
            idBackUp = ContextCompat.getString(this, R.string.full_native_two_low),
            reloadLimit = nativeReloadLimit.toInt(),
            whichAd = AdsClassification.NATIVE,
            isAboveCtr = false,
            isMetaLayout = loadNativeMetaLayout,
            enable = loadNativeFullTwo,
            adMobLayoutId = R.layout.custom_native_admob_free_size,
            metaLayoutId = R.layout.custom_native_admob_free_size_meta,
            currentActivityOrFragment = "FN2"
        )
    } catch (ex: Exception) {
        null
    }
}

fun Activity.languageInterstitial(): AdConfigModel? {
    return try {
        val interAd = newAdsConfig?.languageScreen?.interstitial
        interAd?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.language_inter_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.language_inter_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.language_inter_low),
                enable = it.isEnabled ?: false,
                reloadLimit = it.floor ?: 2,
                whichAd = AdsClassification.INTERSTITIAL,
                currentActivityOrFragment = "LANGUAGE_INTER",
                interstitialAdModel = InterstitialAdModel().apply {
                    interstitialAdFirstShowCount = it.startCount ?: 1
                    interstitialAdAlwaysShow = it.alwaysShow ?: false
                    interstitialAdAfterFirstShowSteps = it.afterStartCount ?: 1
                    interstitialAdCurrentCounter = if (interstitialAdFirstShowCount > 0) 0 else 1
                }
            )
        }
    } catch (ex: Exception) {
        null
    }
}

fun Activity.homeInterstitial(): AdConfigModel? {
    return try {
        val interAd = newAdsConfig?.appInterstitial
        interAd?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.all_inter_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.all_inter_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.all_inter_low),
                enable = enableHomeInterAd,
                reloadLimit = interstitialHomeFloor,
                whichAd = AdsClassification.INTERSTITIAL,
                currentActivityOrFragment = "ALL_INTER",
                interstitialAdModel = InterstitialAdModel().apply {
                    interstitialAdFirstShowCount = interstitialHomeStartCount
                    interstitialAdAlwaysShow = interstitialHomeAlwaysShow
                    interstitialAdAfterFirstShowSteps = interstitialHomeAfterStartCount
                    interstitialAdCurrentCounter = if (interstitialAdFirstShowCount > 0) 0 else 1
                }
            )
        }
    } catch (ex: Exception) {
        null
    }
}

fun Activity.startedInterstitial(): AdConfigModel? {
    return try {
        val interAd = newAdsConfig?.startedInterstitial
        interAd?.let {
            AdConfigModel(
                idHigh = it.adUnitIds?.getOrNull(0) ?: ContextCompat.getString(this, R.string.started_inter_low),
                idMedium = it.adUnitIds?.getOrNull(1) ?: ContextCompat.getString(this, R.string.started_inter_low),
                idBackUp = it.adUnitIds?.getOrNull(2) ?: ContextCompat.getString(this, R.string.started_inter_low),
                enable = it.isEnabled == true,
                reloadLimit = it.floor ?: 2,
                whichAd = AdsClassification.INTERSTITIAL,
                currentActivityOrFragment = "STARTED_INTER",
                interstitialAdModel = InterstitialAdModel().apply {
                    interstitialAdFirstShowCount = it.startCount ?: 1
                    interstitialAdAlwaysShow = it.alwaysShow ?: false
                    interstitialAdAfterFirstShowSteps = it.afterStartCount ?: 1
                    interstitialAdCurrentCounter = if (interstitialAdFirstShowCount > 0) 0 else 1
                }
            )
        }

    } catch (ex: Exception) {
        null
    }
}
