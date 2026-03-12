package com.fahad.newtruelovebyfahad.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.ads.Constants.ADS_SDK_INITIALIZE
import com.example.ads.Constants.InterstitialUnInstall
import com.example.ads.Constants.adGalleryNative
import com.example.ads.Constants.adGalleryNativeFloor
import com.example.ads.Constants.allBannerReloadLimit
import com.example.ads.Constants.enableHomeInterAd
import com.example.ads.Constants.flowUninstall
import com.example.ads.Constants.galleryButtonNewFlow
import com.example.ads.Constants.homeNewInterStrategy
import com.example.ads.Constants.interstitialHomeAfterStartCount
import com.example.ads.Constants.interstitialHomeAlwaysShow
import com.example.ads.Constants.interstitialHomeFloor
import com.example.ads.Constants.interstitialHomeStartCount
import com.example.ads.Constants.interstitialNew
import com.example.ads.Constants.introScreen
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.lfoOneNativeId
import com.example.ads.Constants.lfoTwoNativeId
import com.example.ads.Constants.loadBannerOnBoardFour
import com.example.ads.Constants.loadBannerOnBoardMedium
import com.example.ads.Constants.loadBannerOnBoardOne
import com.example.ads.Constants.loadBannerOnBoardThree
import com.example.ads.Constants.loadBannerOnBoardTwo
import com.example.ads.Constants.loadBannerSplash
import com.example.ads.Constants.loadInterstitialSave
import com.example.ads.Constants.loadInterstitialSplash
import com.example.ads.Constants.loadNativeFullOne
import com.example.ads.Constants.loadNativeFullTwo
import com.example.ads.Constants.loadNativeLfOne
import com.example.ads.Constants.loadNativeLfTwo
import com.example.ads.Constants.loadNativeObFour
import com.example.ads.Constants.loadNativeObOne
import com.example.ads.Constants.loadNativeObThree
import com.example.ads.Constants.loadNativeObTwo
import com.example.ads.Constants.loadNativeOld
import com.example.ads.Constants.loadNativeOnResume
import com.example.ads.Constants.loadSplashAppOpen
import com.example.ads.Constants.nativeReasonUninstall
import com.example.ads.Constants.newAdsConfig
import com.example.ads.Constants.onBoardingOneNativeId
import com.example.ads.Constants.onBoardingThreeNativeId
import com.example.ads.Constants.onBoardingTwoNativeId
import com.example.ads.Constants.openTutorial
import com.example.ads.Constants.popupEventValentine
import com.example.ads.Constants.proCounter
import com.example.ads.Constants.proSplashOrHome
import com.example.ads.Constants.questionScreenEnable
import com.example.ads.Constants.showAllAppOpenAd
import com.example.ads.Constants.showBlendGuideScreen
import com.example.ads.Constants.splashBannerReloadLimit
import com.example.ads.Constants.splashInterAdId
import com.example.ads.Constants.splashTime
import com.example.ads.Constants.splashTimeOut
import com.example.ads.Constants.tutorialScr
import com.example.ads.admobs.scripts.InterstitialNew
import com.example.ads.admobs.utils.MobileAds
import com.example.ads.admobs.utils.loadAppOpenSplash
import com.example.ads.admobs.utils.loadNewInterstitialForPro
import com.example.ads.admobs.utils.onPauseSplashBanner
import com.example.ads.admobs.utils.onResumeSplashBanner
import com.example.ads.admobs.utils.preLoadNative
import com.example.ads.admobs.utils.setOnClick
import com.example.ads.admobs.utils.showAppOpenSplash
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.show
import com.example.ads.model.AdConfigModel
import com.example.ads.utils.bannerSplash
import com.example.ads.utils.fullNativeOne
import com.example.ads.utils.fullNativeTwo
import com.example.ads.utils.languageInterstitial
import com.example.ads.utils.nativeLanguageOne
import com.example.ads.utils.nativeLanguageTwo
import com.example.ads.utils.onBoardNativeOne
import com.example.ads.utils.onBoardNativeThree
import com.example.ads.utils.onBoardNativeTwo
import com.example.ads.utils.question
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Events
import com.example.apponboarding.ui.main.viewModels.LanguageViewModel
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.BuildConfig
import com.fahad.newtruelovebyfahad.databinding.ActivitySplashBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType
import com.project.common.remote_config.RemoteConfigViewModel
import com.project.common.utils.Constants.isProWithInterOn
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.introOnBoardingCompleted
import com.project.common.utils.ConstantsCommon.receivedData
import com.project.common.utils.ConstantsCommon.showQuestionScreenTimeCheck
import com.project.common.utils.ConstantsCommon.surveyCompleted
import com.project.common.utils.getProScreen
import com.project.common.utils.hideNavigation
import com.project.common.utils.setLocale
import com.project.common.viewmodels.DataStoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.EnumMap

@SuppressLint("CustomSplashScreen")
const val TAG = "SplashActivity"

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private var _binding: ActivitySplashBinding? = null
    private val binding get() = _binding!!
    private var splashTimerJob: Job? = null
    private var isConsentFormCompleted = false
    private val dataStoreViewModel by viewModels<DataStoreViewModel>()
    private var showAppOpen = false
    private val languageViewModel by viewModels<LanguageViewModel>()
    private val remoteConfigModel by viewModels<RemoteConfigViewModel>()

    private fun initUninstallIntent() {
        receivedData = intent.getStringExtra("shortcut_extra_key")
        if (receivedData != null) {
            Log.d(TAG, "initUninstallIntent :Received from shortcut: $receivedData")
        } else {
            Log.d(TAG, "initUninstallIntent: receivedData is null")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            if (isProVersion.hasObservers()) {
                isProVersion.removeObservers(this@SplashActivity as LifecycleOwner)
            }

            isProVersion.observe(this@SplashActivity) {
                if (it) {
                    _binding?.bannerContainer?.hide()
                }
            }
        } catch (ex: Exception) {
            Log.e("error", "onCreate: ", ex)
        }

        hideNavigation()
        runCatching {
            nextWork()
        }
    }

    private fun nextWork() {

        kotlin.runCatching {

            languageViewModel.selectedLanguage.observe(this@SplashActivity) { selectedLanguage ->
                selectedLanguage?.let {
                    Log.d(TAG, "onCreate: selectedLanguage $selectedLanguage")
                    Log.d(
                        TAG,
                        "onCreate: selectedLanguage.languageCode ${selectedLanguage.languageCode}"
                    )
                    languageCode = selectedLanguage.languageCode
                    setLocale(languageCode)
                }
            }
        }
        initUninstallIntent()
        ConstantsCommon.isNetworkAvailable = isNetworkAvailable()
        ADS_SDK_INITIALIZE.set(false)
        interstitialNew = InterstitialNew()

        if (isNetworkAvailable()) {
            initConsentForum()
        } else {
            initObserver()
        }

        firebaseAnalytics?.logEvent(Events.Screens.SPLASH, Bundle().apply {
            putString(Events.ParamsKeys.ACTION, Events.ParamsValues.DISPLAYED)
        })
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            hideNavigation()
        }
    }

    override fun onPause() {
        super.onPause()
        onPauseSplashBanner()
    }

    private fun initConsentForum() {
        try {
            MobileAds().apply {
                checkAdmobConsent(this@SplashActivity, onCompletion = {
                    if (!isConsentFormCompleted) {
                        isConsentFormCompleted = true

                        Log.i(TAG, "initConsentForum: firstCall")
                        if (!showAppOpen && !isProVersion() && isNetworkAvailable()) {
                            _binding?.apply {
                                bannerContainer.show()
                                onResumeSplashBanner(
                                    adBannerContainer,
                                    crossBannerIv,
                                    bannerLayout.adContainer,
                                    bannerLayout.shimmerViewContainer,
                                    config = bannerSplash()
                                )
                            }
                        } else {
                            if (!isFinishing && !isDestroyed && _binding != null) {
                                binding.bannerContainer.hide()
                            }
                        }

                        remoteConfigModel.getRemoteConfigSplash(this@SplashActivity)
                        remoteConfigModel.adConfig.observe(this@SplashActivity) { newConfig ->
                            newConfig?.let {
                                newAdsConfig = it
                                remoteConfig(it)
                                lifecycleScope.launch(Main) {

                                    if (loadSplashAppOpen) {
                                        loadAppOpenSplash(true)
                                    }

                                    if (!isProVersion() && !showAppOpen) {
                                        if (loadBannerSplash) {
                                            _binding?.bannerContainer?.show()
                                        } else {
                                            _binding?.bannerContainer?.hide()
                                        }
                                    }

                                    kotlin.runCatching {
                                        dataStoreViewModel.questionScreenCompleted.observeOnce(this@SplashActivity) {
                                            showQuestionScreenTimeCheck = !it
                                            lifecycleScope.launch(if (it) Main else IO) {
                                                if (!it) {
                                                    dataStoreViewModel.readCurrentTime()
                                                        .let { completedTimeInterval ->
                                                            showQuestionScreenTimeCheck =
                                                                completedTimeInterval && questionScreenEnable
                                                        }
                                                }

                                                withContext(Main) {
                                                    if (showQuestionScreenTimeCheck) {
                                                        preLoadNative(question())
                                                    }
                                                }
                                            }
                                        }
                                    }


                                }

                                dataStoreViewModel.introComplete.observeOnce(this@SplashActivity) { it ->
                                    Log.i(TAG, "initConsentForum: introComplete")
                                    if (!it) {
                                        preLoadNative(getNextConfig())
                                        initObserver()
                                    } else {
                                        initObserver()
                                    }
                                }

                            }

                        }

                        val consentMap: MutableMap<ConsentType, FirebaseAnalytics.ConsentStatus> = EnumMap(ConsentType::class.java)
                        consentMap[ConsentType.ANALYTICS_STORAGE] = FirebaseAnalytics.ConsentStatus.GRANTED
                        consentMap[ConsentType.AD_STORAGE] = FirebaseAnalytics.ConsentStatus.GRANTED
                        consentMap[ConsentType.AD_USER_DATA] = FirebaseAnalytics.ConsentStatus.GRANTED
                        consentMap[ConsentType.AD_PERSONALIZATION] = FirebaseAnalytics.ConsentStatus.GRANTED
                        firebaseAnalytics?.setConsent(consentMap)

                    }
                }, consentCompletionCallback = {})
            }
        } catch (ex: Exception) {
            Log.e("TAG", "onCreate: ", ex)
        }
    }

    private fun getNextConfig(): AdConfigModel? {

        return if (loadNativeLfOne) {
            nativeLanguageOne()
        } else if (loadNativeLfTwo) {
            nativeLanguageTwo()
        } else if (loadNativeObOne) {
            onBoardNativeOne()
        } else if (loadNativeFullOne) {
            fullNativeOne()
        } else if (loadNativeObTwo) {
            onBoardNativeTwo()
        } else if (loadNativeFullTwo) {
            fullNativeTwo()
        } else if (loadNativeObThree) {
            onBoardNativeThree()
        } else {
            null
        }
    }

    private fun initObserver() {
        initData()
    }

    private fun initData() {
        if (isNetworkAvailable()) {
            ConstantsCommon.updateInternetStatusFrames.postValue(true)
        } else {

            ConstantsCommon.updateInternetStatusFrames.postValue(false)
        }

        Log.i("TAG", "initData: initData")

        initTimer(splashTime)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        _binding?.apply {
            kotlin.runCatching {
                root.setBackgroundColor(
                    ContextCompat.getColor(
                        this@SplashActivity,
                        com.project.common.R.color.container_clr_activity
                    )
                )

            }
        }
    }

    private fun initTimer(waitTime: Long = 2000) {
        splashTimerJob = lifecycleScope.launch(IO) {
            //delay(waitTime)
            withContext(Main) {
                lifecycleScope.launch {
                    delay(waitTime)
                    navigateToAfterNative()
                }
                Log.i("TAG", "initData: initTimer")


            }
        }
    }

    private fun navigateToAfterNative() {
        runCatching {
            val myCallback: () -> Unit = {
                Log.i("TAG", "initData: initCallback")
                initIntro()
            }

            if (BuildConfig.DEBUG) {
                myCallback.invoke()
                return@runCatching
            }

            if (loadSplashAppOpen) {
                _binding?.bannerContainer?.visibility = View.INVISIBLE
                showAppOpenSplash {
                    myCallback.invoke()
                }
            }

        }
    }

    private fun initIntro() {

        kotlin.runCatching {
            dataStoreViewModel.blendObGuideCompleted.observeOnce(this) {
                lifecycleScope.launch(Main) {
                    showBlendGuideScreen = !it
                    if (showBlendGuideScreen) {
                        showBlendGuideScreen = tutorialScr
                    }
                    if (showBlendGuideScreen && openTutorial) {
                        lifecycleScope.launch(Main) {
                            //  loadAppOpenBlendGuide()
                        }
                    }

                    dataStoreViewModel.introComplete.observeOnce(this@SplashActivity) { intro ->
                        dataStoreViewModel.questionScreenCompleted.observeOnce(this@SplashActivity) {
                            showQuestionScreenTimeCheck = !it
                            lifecycleScope.launch(if (it) Main else IO) {
                                if (!it) {
                                    dataStoreViewModel.readCurrentTime()
                                        .let { completedTimeInterval ->
                                            showQuestionScreenTimeCheck =
                                                completedTimeInterval && questionScreenEnable
                                        }
                                }
                                withContext(Main) {
                                    dataStoreViewModel.introCounter.observeOnce(this@SplashActivity) { counter ->

                                        kotlin.runCatching {

                                            if (!intro && !introScreen) {
                                                dataStoreViewModel.updateIntroComplete()
                                            }

                                            introOnBoardingCompleted = intro

                                            dataStoreViewModel.surveyComplete.observeOnce(this@SplashActivity) { survey ->

                                                lifecycleScope.launch(Main) {

                                                    surveyCompleted = survey

                                                    if (!intro && introScreen) {
                                                        if (!proSplashOrHome) {

                                                            kotlin.runCatching {
                                                                val intent = Intent(
                                                                    applicationContext,
                                                                    com.example.apponboarding.ui.main.activity.LanguageActivity::class.java
                                                                )
                                                                startActivity(intent)
                                                                finish()
                                                            }
                                                            //
                                                        } else if (proSplashOrHome && !isProVersion()) {
                                                            loadNewInterstitialForPro(languageInterstitial()) {}

                                                            kotlin.runCatching {
                                                                sendEvent(true)
                                                                val intent = Intent()
                                                                intent.setClassName(
                                                                    applicationContext,
                                                                    getProScreen()
                                                                )
                                                                intent.putExtra("show_ad", true)
                                                                intent.putExtra("is_intro_complete", !intro)
                                                                kotlin.runCatching {
                                                                    this@SplashActivity.setLocale(languageCode)
                                                                }
                                                                startActivity(intent)
                                                                finish()
                                                            }
                                                        } else {

                                                            kotlin.runCatching {
                                                                val intent = Intent(
                                                                    applicationContext,
                                                                    com.example.apponboarding.ui.main.activity.LanguageActivity::class.java
                                                                )
                                                                startActivity(intent)
                                                                finish()
                                                            }
                                                        }


                                                    } else if (isProVersion()) {
                                                        kotlin.runCatching {
                                                            val intent = Intent(
                                                                applicationContext,
                                                                MainActivity::class.java
                                                            )
                                                            startActivity(intent)
                                                            finish()
                                                        }
                                                    } else {
                                                        dataStoreViewModel.appSessions.observeOnce(
                                                            this@SplashActivity
                                                        ) {
                                                            it.let {
                                                                Log.d(
                                                                    "FAHAD",
                                                                    "onCreate: observeTwice $it"
                                                                )
                                                                if (it > 0 && !isProVersion()) {
                                                                    runCatching {
                                                                        if (proSplashOrHome && !isProVersion()) {
                                                                            kotlin.runCatching {
                                                                                sendEvent(true)
                                                                                val intent = Intent()
                                                                                intent.setClassName(
                                                                                    applicationContext,
                                                                                    getProScreen()
                                                                                )
                                                                                intent.putExtra("show_ad", true)
                                                                                intent.putExtra("is_intro_complete", !intro)
                                                                                kotlin.runCatching {
                                                                                    this@SplashActivity.setLocale(languageCode)
                                                                                }
                                                                                startActivity(intent)
                                                                                finish()
                                                                            }
                                                                        } else {
                                                                            kotlin.runCatching {
                                                                                val intent = Intent(
                                                                                    applicationContext,
                                                                                    MainActivity::class.java
                                                                                )
                                                                                startActivity(intent)
                                                                                finish()
                                                                            }
                                                                        }

                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendEvent(premium: Boolean) {
        if (premium) {
            firebaseAnalytics?.logEvent(
                Events.Screens.MAIN,
                Bundle().apply {
                    putString(
                        Events.ParamsKeys.ACTION,
                        Events.ParamsValues.ROBO_OPEN
                    )
                    putString(
                        Events.ParamsKeys.OPENING_SCREEN,
                        Events.Screens.PREMIUM
                    )
                })
        } else {
            firebaseAnalytics?.logEvent(
                Events.Screens.MAIN,
                Bundle().apply {
                    putString(
                        Events.ParamsKeys.ACTION,
                        Events.ParamsValues.ROBO_OPEN
                    )
                    putString(
                        Events.ParamsKeys.OPENING_SCREEN,
                        Events.Screens.PREMIUM_OFFER
                    )
                })
        }
    }


    private fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
        observe(owner, object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                observer(value)
            }
        })
    }

    private fun remoteConfig(adConfigModel: com.project.common.remote_config.AdConfigModel) {
        kotlin.runCatching {
            loadBannerOnBoardOne = false
            loadBannerOnBoardTwo = false
            loadBannerOnBoardThree = false
            loadBannerOnBoardFour = false
            loadBannerOnBoardMedium = false
            proSplashOrHome = adConfigModel.splashScreen?.splashProHome ?: false
            allBannerReloadLimit = (newAdsConfig?.appBanner?.reloadLimit ?: 2L).toLong()
            loadBannerSplash = newAdsConfig?.splashScreen?.banner?.isEnabled ?: false
            splashBannerReloadLimit = newAdsConfig?.splashScreen?.banner?.reloadLimit ?: 2
            loadInterstitialSplash = newAdsConfig?.splashScreen?.interstitial?.isEnabled ?: false
            enableHomeInterAd = adConfigModel.appInterstitial?.isEnabled ?: false
            isProWithInterOn = enableHomeInterAd // to on off pro with inter
            loadSplashAppOpen = adConfigModel.splashScreen?.appOpen?.isEnabled ?: false
            interstitialHomeFloor = adConfigModel.appInterstitial?.floor ?: 2
            interstitialHomeStartCount = adConfigModel.appInterstitial?.startCount ?: 0
            interstitialHomeAfterStartCount = adConfigModel.appInterstitial?.afterStartCount ?: 1
            interstitialHomeAlwaysShow = adConfigModel.appInterstitial?.alwaysShow ?: false
            splashTime = (adConfigModel.splashScreen?.time ?: 5000L).toLong()
            //  splashTimeOut = (adConfigModel.splashScreen?.timeout ?: 10000L).toLong()
            splashTimeOut = (adConfigModel.splashScreen?.timeout ?: 7000L).toLong()
            // set in remote json
            Log.d("SPLASH_TIME_ISSUE", "remoteConfig splash time  :$splashTime ")
            Log.d("SPLASH_TIME_ISSUE", "remoteConfig time out  :$splashTimeOut ")
            loadNativeLfOne = adConfigModel.languageScreen?.nativeBeforeSelection?.isEnabled ?: true
            loadNativeLfTwo = adConfigModel.languageScreen?.nativeAfterSelection?.isEnabled ?: true
            loadNativeObOne = adConfigModel.onboarding1Native?.isEnabled ?: true
            loadNativeObTwo = adConfigModel.onboarding2Native?.isEnabled ?: true
            loadNativeObThree = adConfigModel.onboarding3Native?.isEnabled ?: true
            loadNativeObFour = false
            showAllAppOpenAd = adConfigModel.appOpenResume?.isEnabled ?: true
            onBoardingOneNativeId = adConfigModel.onboarding1Native?.adUnitIds?.getOrNull(0) ?: "ca-app-pub-4276074242154795/6026210694"
            onBoardingTwoNativeId = adConfigModel.onboarding2Native?.adUnitIds?.getOrNull(0)
                ?: "ca-app-pub-4276074242154795/8852923006"
            onBoardingThreeNativeId = adConfigModel.onboarding3Native?.adUnitIds?.getOrNull(0)
                ?: "ca-app-pub-4276074242154795/1799627163"
            lfoOneNativeId =
                adConfigModel.languageScreen?.nativeBeforeSelection?.adUnitIds?.getOrNull(0)
                    ?: "ca-app-pub-4276074242154795/2355269438"
            lfoTwoNativeId =
                adConfigModel.languageScreen?.nativeAfterSelection?.adUnitIds?.getOrNull(0)
                    ?: "ca-app-pub-4276074242154795/8729106095"
            splashInterAdId = adConfigModel.splashScreen?.interstitial?.adUnitIds?.getOrNull(0)
                ?: "ca-app-pub-4276074242154795/7571205893"
            loadNativeOnResume = true
            homeNewInterStrategy = true
            flowUninstall = false
            nativeReasonUninstall = false
            galleryButtonNewFlow = true
            proCounter = 4L
            InterstitialUnInstall = false
            adGalleryNativeFloor = 4
            adGalleryNative = true
            loadNativeOld = true
            popupEventValentine = false
            loadNativeFullOne = false
            loadNativeFullTwo = false
            questionScreenEnable = false
            tutorialScr = false
            showBlendGuideScreen = false
            loadInterstitialSave = true
            setOnClick()


            Log.i("TAG", "//remoteConfig: completed")
            //onCompletion.invoke()
        }
    }


}