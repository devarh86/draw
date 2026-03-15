package com.fahad.newtruelovebyfahad.ui.activities.main


import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.ads.Constants.CAN_LOAD_ADS
import com.example.ads.Constants.failureMsg
import com.example.ads.Constants.homeMenuShowAd
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.myWorkMenuShowAd
import com.example.ads.Constants.native
import com.example.ads.admobs.utils.loadAppOpen
import com.example.ads.admobs.utils.loadNewInterstitialForPro
import com.example.ads.admobs.utils.loadNewInterstitialWithoutStrategyCheck
import com.example.ads.admobs.utils.loadRewarded
import com.example.ads.admobs.utils.onPauseBanner
import com.example.ads.admobs.utils.onResumeBanner
import com.example.ads.admobs.utils.showAppOpen
import com.example.ads.admobs.utils.showNewInterstitial
import com.example.ads.admobs.utils.showRewardedInterstitial
import com.example.ads.dialogs.createDownloadingDialog
import com.example.ads.dialogs.createProFramesDialog
import com.example.ads.utils.homeInterstitial
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Constants.parentScreen
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.MyApp
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.ActivityMainBinding
import com.fahad.newtruelovebyfahad.ui.fragments.home.HomeForYouFragment
import com.fahad.newtruelovebyfahad.utils.InternetConnectivityReceiver
import com.fahad.newtruelovebyfahad.utils.Permissions
import com.fahad.newtruelovebyfahad.utils.enums.FrameThumbType
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.interfaces.InternetConnectivityListener
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.datastore.FrameDataStore
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.isNetworkAvailable
import com.project.common.utils.getProScreen
import com.project.common.utils.hideNavigation
import com.project.common.utils.setLocale
import com.project.common.utils.setString
import com.project.common.viewmodels.DataStoreViewModel
import com.project.common.viewmodels.HomeAndTemplateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : Permissions(), InternetConnectivityListener {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var navController: NavController? = null
    private val mainViewModel by viewModels<MainViewModel>()
    private val dataStoreViewModel by viewModels<DataStoreViewModel>()
    var showAppOpen = false
    var hideCollapsibleBanner = false
    var receivedData1: String? = null

    @Inject
    lateinit var frameDataStore: FrameDataStore
    //private var mLoaderCallback: LoaderCallbackInterface? = null

    private var alreadyPro = false

    companion object {
        var isFirstTime: Boolean = false
    }

    fun reLoadBannerAdForFeature() {
        loadBannerAd(fromButton = true)
    }

    private fun initNavigationGraph() {
        try {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

            navController = navHostFragment?.navController

            setupSmoothBottomMenu()
            navController?.addOnDestinationChangedListener { _, destination, _ ->

                when (destination.id) {
                    R.id.homeForYouFragment,
                        -> {
                        binding.bottomBar.gone()
                        binding.crossPromoAdsCv.gone()
                        binding.adTv.gone()
                        binding.addIcon.gone()

                        binding.bannerContainer.gone()
                    }

                    else -> {
                        binding.bottomBar.gone()
                        binding.addIcon.gone()
                        binding.crossPromoAdsCv.gone()
                        binding.adTv.gone()
                        loadBannerAd()
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.e("error", "initNavigationGraph: ", ex)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("TAG", "onStop: mainActivity onDestroy")
    }

    private fun initUninstallIntentValue() {
        receivedData1 = intent.getStringExtra("shortcut_extra_key1")
        if (receivedData1 != null) {
            Log.d("UNISNATLLINT", "Received from shortcut: $receivedData1")

        } else {
            Log.d("UNISNATLLINT", "initGetValue: receivedData1 is null")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUninstallIntentValue()
        kotlin.runCatching {
            setLocale(languageCode)
        }
        _binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)


        loadNewInterstitialWithoutStrategyCheck(homeInterstitial()) {}


        kotlin.runCatching {
            dataStoreViewModel.initFun()
            lifecycleScope.launch(IO) {
                dataStoreViewModel.updateIntroComplete()
            }
        }

        kotlin.runCatching {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            registerForFullScreenIntent(notificationManager)
        }

        kotlin.runCatching {
            failureMsg = setString(com.project.common.R.string.try_again)
        }

        isNetworkAvailable = isNetworkAvailable()

        mainViewModel.isSplashVisible = false

        initNavigationGraph()

        checkFragment(hideViews = false)

        kotlin.runCatching {
            dataStoreViewModel.incrementAppSession()
            dataStoreViewModel.writeCurrentTime()
        }

        firebaseAnalytics?.logEvent(Events.NetworkKeys.INTERNET, Bundle().apply {
            putString(
                Events.NetworkParams.INTERNET_STATE, if (isNetworkAvailable()) "true" else "false"
            )
        })

        try {
            if (isProVersion.hasObservers()) {
                isProVersion.removeObservers(this@MainActivity as LifecycleOwner)
            }

            alreadyPro = isProVersion()

            isProVersion.observe(this@MainActivity) {
                if (!alreadyPro) {
                    checkFragment(it)
                }
                alreadyPro = it
            }
        } catch (ex: Exception) {
            Log.e("error", "onCreate: ", ex)
        }

        firebaseAnalytics?.logEvent(Events.Screens.MAIN, Bundle().apply {
            putString(
                Events.ParamsKeys.SUB_SCREEN, Events.SubScreens.SLIDER_MENU
            )
            putString(
                Events.ParamsKeys.ACTION, Events.ParamsValues.DISPLAYED
            )
            putString(
                Events.ParamsKeys.CROSS_PROMO_AD_TITLE, "FloraGarden".replace(".", "_").lowercase()
            )
            putString(
                Events.ParamsKeys.CROSS_PROMO_AD_PLACEMENT, "MainMenu".replace(".", "_").lowercase()
            )
            putString(
                Events.ParamsKeys.CROSS_PROMO_AD_TYPE, "Banner".replace(".", "_").lowercase()
            )
        })


        try {
            application.let {
                runCatching {
                    if (it is MyApp) {
                        it.billing.firebaseAnalytics = firebaseAnalytics
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.e("error", "onCreate: ", ex)
        }

        if (isNetworkAvailable()) {
            ConstantsCommon.updateInternetStatusFeature.postValue(true)
            ConstantsCommon.updateInternetStatusFrames.postValue(true)
        } else {
            ConstantsCommon.updateInternetStatusFeature.postValue(false)
            ConstantsCommon.updateInternetStatusFrames.postValue(false)
        }

        initNetworkCallbacks()


        try {
            if (CAN_LOAD_ADS) {
                loadNewInterstitialForPro(homeInterstitial()) {}

                loadAppOpen()

                loadRewarded(loadedAction = {}, failedAction = {})
            }
        } catch (ex: Exception) {
            Log.e("TAG", "onCreate: ", ex)
        }
        hideNavigation()


        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
//                    val intent = result.data
                    kotlin.runCatching {
                        isRecreate = true
                        Log.i("TAG", "onResumesuper: recreate")
                    }
                }
            }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            hideNavigation()
        }
    }

    private fun loadBannerAd(fromButton: Boolean = false) {
        kotlin.runCatching {
            runOnUiThread {

                // Check if current destination should hide banner ad
                val currentDestinationId = navController?.currentDestination?.id
                val shouldHideBanner = currentDestinationId == R.id.homeForYouFragment

                if (shouldHideBanner) {
                    _binding?.bannerContainer?.visibility = View.GONE
                    return@runOnUiThread
                }

                if (!showAppOpen && !isProVersion()) {
                    _binding?.let {
                        _binding?.bannerContainer?.visibility = View.VISIBLE
                        onResumeBanner(
                            binding.adBannerContainer,
                            binding.crossBannerIv,
                            binding.bannerLayout.adContainer,
                            binding.bannerLayout.shimmerViewContainer,
                            fromButton = fromButton
                        )
                    }
                } else {
                    try {
                        if (isProVersion()) {
                            _binding?.bannerContainer?.visibility = View.GONE
                        } else {
                            _binding?.bannerContainer?.visibility = View.INVISIBLE
                        }
                    } catch (ex: java.lang.Exception) {
                        Log.e("error", "onResume: ", ex)
                    }
                }
            }
        }
    }

    fun getActivityLauncher(): ActivityResultLauncher<Intent> {
        return activityLauncher
    }


    private val activityLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val fromDraft = result.data?.getBooleanExtra("from_draft", false) ?: false
            val backDecision = result.data?.getBooleanExtra("backpress", false) ?: false
            val resultBack = true//result.data?.getBooleanExtra("resultBack", false) ?: false
            val where = result.data?.getStringExtra("where") ?: ""
            if (backDecision) {

                kotlin.runCatching {
                    navController?.navigateUp()
                }

            } else {
                if (where.isNotEmpty() && where == "home") {
                    showHomeScreen()
                }
            }
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

    fun getSplashVisible(): Boolean {
        return try {
            mainViewModel.isSplashVisible
        } catch (ex: java.lang.Exception) {
            true
        }
    }

    fun showHomeScreen() {
        try {
            ConstantsCommon.isSavedScreenHomeClicked = false
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomBar)
            navController?.let { controller ->
                if (controller.currentDestination?.id != R.id.homeForYouFragment) {
                    val poppedToHome = controller.popBackStack(R.id.homeForYouFragment, false)
                    if (!poppedToHome) {
                        controller.navigate(R.id.homeForYouFragment)
                    }
                }
            }
            bottomNavigationView?.selectedItemId = R.id.homeForYouFragment
        } catch (_: Exception) {
        }
    }

    fun showAppOpenAd() {
        binding.let {
            it.bannerContainer.invisible()
            showAppOpen = true
            checkFragment(hideViews = true)
            Handler(Looper.getMainLooper()).postDelayed({
                showAppOpen {
                    binding.let {
                        Handler(Looper.getMainLooper()).postDelayed({
                            //binding.bannerContainer.visible()
                            checkFragment(hideViews = false)
                            showAppOpen = false
                            loadBannerAd()
                            loadAppOpen()
                        }, 800L)
                    }
                }
            }, 500L)
        }
    }

    fun goProBottom(show: Boolean) {
        try {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            when (val fragment = navHostFragment?.childFragmentManager?.fragments?.get(0)) {


                else -> {}
            }
        } catch (_: Exception) {
        }
    }

    private fun checkFragment(hideViews: Boolean = false) {
        try {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            when (val fragment = navHostFragment?.childFragmentManager?.fragments?.get(0)) {

                is HomeForYouFragment -> {
                    if (hideViews) fragment.hideScreenAds() else fragment.showScreenAd()
                }


                else -> {
                    Log.d("AppOpen", "onForegroundEntered: else")
                }
            }
        } catch (_: Exception) {
        }
    }

    private var isRecreate = false
    private var resultLauncher: ActivityResultLauncher<Intent>? = null

    fun changeLanguage() {
        try {
            native.nativeAd = null
            val intent = Intent(
                applicationContext,
                com.example.apponboarding.ui.main.activity.LanguageActivitySetting::class.java
            )
            intent.putExtra("from_setting", true)
            resultLauncher?.launch(intent)
        } catch (ex: java.lang.Exception) {
            Log.e("error", ": ", ex)
        }
    }

    private fun setupSmoothBottomMenu() {
        try {
            navController?.let {
                _binding?.bottomBar?.setupWithNavController(it)
            }
            var lastSelected: Int? = null
            _binding?.addIcon?.setSingleClickListener {

            }
            fun handleMenuItemSelection(showAd: Boolean, itemId: Int) {
                try {
                    if (lastSelected != null && lastSelected != itemId) {
                        lastSelected = itemId
                        if (itemId == R.id.nav_mywork || itemId == R.id.homeForYouFragment) {
                            Log.i("ISWORKID", "handleMenuItemSelection:yes ")
                            showNewInterstitial(homeInterstitial()) {
                                loadNewInterstitialWithoutStrategyCheck(homeInterstitial()) {}
                            }
                        }
                        loadBannerAd()
                    } else if (lastSelected == null) {
                        lastSelected = itemId
                        if (itemId == R.id.nav_mywork || itemId == R.id.homeForYouFragment) {
                            Log.i("ISWORKID", "handleMenuItemSelection:yes-B ")
                            showNewInterstitial(homeInterstitial()) {
                                loadNewInterstitialWithoutStrategyCheck(homeInterstitial()) {}
                            }
                        }
                        loadBannerAd()
                    }
                } catch (ex: java.lang.Exception) {
                    Log.e("error", "handleMenuItemSelection: ", ex)
                }
            }

            kotlin.runCatching {
                _binding?.bottomBar?.menu?.children?.forEach {
                    it.setOnMenuItemClickListener {
                        when (it.itemId) {

                            R.id.homeForYouFragment -> {
                                handleMenuItemSelection(homeMenuShowAd, it.itemId)
                            }

                            R.id.nav_mywork -> handleMenuItemSelection(myWorkMenuShowAd, it.itemId)
                        }
                        false
                    }
                }
            }

        } catch (ex: java.lang.Exception) {
            Log.e("error", "setupSmoothBottomMenu: ", ex)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isRecreate) {

            Log.i("TAG", "onResumesuper: onResume")

            loadBannerAd()

        } else {
            recreate()
        }
    }

    override fun onPause() {
        super.onPause()
        onPauseBanner()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController?.navigateUp() == true || super.onSupportNavigateUp()
    }

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var internetConnectivityReceiver: InternetConnectivityReceiver? = null
    private var onCreateInitialize = false
    override fun onStart() {
        super.onStart()
        initNetworkCallbacks()
    }

    override fun onStop() {
        super.onStop()
        Log.i("TAG", "onStop: mainActivity OnStop")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
            connectivityManager = null
            networkCallback = null
        } else {
            unregisterReceiver(internetConnectivityReceiver)
            internetConnectivityReceiver = null
        }
    }

    private fun initNetworkCallbacks() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (connectivityManager == null && networkCallback == null) {
                connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                networkCallback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        loadBannerAd()
                        runOnUiThread {
                            isNetworkAvailable = true
                            ConstantsCommon.updateInternetStatusFeature.postValue(true)
                            ConstantsCommon.updateInternetStatusFrames.postValue(true)
                        }
                    }

                    override fun onLost(network: Network) {
                        runOnUiThread {
                            isNetworkAvailable = false
                            ConstantsCommon.updateInternetStatusFeature.postValue(false)
                            ConstantsCommon.updateInternetStatusFrames.postValue(false)
                        }
                    }
                }

                val networkRequest =
                    NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build()

                networkCallback?.let {
                    connectivityManager?.registerNetworkCallback(networkRequest, it)
                }

                if (onCreateInitialize) {
                    isNetworkAvailable = isNetworkAvailable()
                    if (!isNetworkAvailable) {
                        isNetworkAvailable = false
                        ConstantsCommon.updateInternetStatusFeature.postValue(false)
                        ConstantsCommon.updateInternetStatusFrames.postValue(false)
                    }
                }
            }

            onCreateInitialize = true

        } else {
            if (internetConnectivityReceiver == null) {
                internetConnectivityReceiver = InternetConnectivityReceiver()
                internetConnectivityReceiver?.setConnectivityListener(this@MainActivity)
                val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                registerReceiver(internetConnectivityReceiver, intentFilter)
            }
        }
    }

    override fun onConnectivityChanged(isConnected: Boolean) {
        isNetworkAvailable = isConnected
        ConstantsCommon.updateInternetStatusFeature.postValue(isConnected)
        ConstantsCommon.updateInternetStatusFrames.postValue(isConnected)
        if (isConnected) {
            loadBannerAd()
        } else {
            Log.i("TAG", "onConnectivityChanged: initDataOffline")
        }
    }

    fun navigate(directions: NavDirections, currentId: Int) {

//        try {
        if (findNavController(binding.navHostFragment.id).currentDestination?.id == currentId) {
            findNavController(binding.navHostFragment.id).navigate(directions)
        }
//        } catch (ex: Exception) {
//            Log.e("error", "navigate: ", ex)
//        }
    }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.CAMERA
    )
    else arrayOf(

        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    var downloadDialog: BottomSheetDialog? = null

    fun eventForFrameClick(frameBody: FrameObject) {

        parentScreen = frameBody.categoryName

        val bundle = Bundle().apply {
            putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
            putString(Events.ParamsKeys.FROM, frameBody.from)

            if (frameBody.categoryName.isNotBlank())
                putString(Events.ParamsKeys.CATNAME, frameBody.categoryName)

            if (frameBody.subCategoryName.isNotBlank())
                putString(Events.ParamsKeys.SUB_SCREEN, frameBody.subCategoryName)

            putString(Events.ParamsKeys.FRAME_ID, frameBody.id.toString())
            putString(Events.ParamsKeys.FRAME_NAME, frameBody.name)
        }

        firebaseAnalytics?.logEvent(frameBody.screenName, bundle)

        Log.i(
            "firebase_events_clicks",
            "events: screenName: ${frameBody.screenName} bundle:  $bundle"
        )
    }

    fun eventForCategoryClick(frameBody: FrameObject) {
        val bundle = Bundle().apply {
            putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)

            putString(Events.ParamsKeys.FROM, frameBody.from)

            if (frameBody.categoryName.isNotBlank())
                putString(Events.ParamsKeys.CATNAME, frameBody.categoryName)

            if (frameBody.subCategoryName.isNotBlank())
                putString(Events.ParamsKeys.SUB_SCREEN, frameBody.subCategoryName)
        }
        firebaseAnalytics?.logEvent(frameBody.screenName, bundle)

        Log.i(
            "firebase_events_clicks",
            "events: screenName: ${frameBody.screenName} bundle:  $bundle"
        )
    }

    fun frameClick(frameBody: FrameObject, updateRecycler: () -> Unit) {

        if (!isNetworkAvailable) {
            kotlin.runCatching {
                Toast.makeText(
                    this,
                    "Please connect to internet",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        kotlin.runCatching {
            Log.d("WHOLEOBJ", "frameClick:${frameBody.subCategoryName},${frameBody.id} ")


            checkAndRequestPermissions(*permissions, action = {

                eventForFrameClick(frameBody)

                if (!isProVersion() && frameBody.tags?.isNotEmpty() == true && frameBody.tags != "Free" && !ConstantsCommon.rewardedAssetsList.contains(
                        frameBody.id
                    )
                ) {
                    createProFramesDialog(
                        frameBody.rewardedAd,
                        thumb = "${frameBody.baseUrl}${frameBody.thumb}",
                        thumbType = ContextCompat.getDrawable(
                            this, when (frameBody.thumbtype.lowercase()) {
                                FrameThumbType.PORTRAIT.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_portrait
                                FrameThumbType.LANDSCAPE.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_landscape
                                FrameThumbType.SQUARE.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_squre
                                else -> com.project.common.R.drawable.frame_placeholder_portrait
                            }
                        ),
                        action = {
                            downloadDialog = createDownloadingDialog(
                                frameBody.baseUrl, frameBody.thumb, frameBody.thumbtype
                            )

                            showRewardedInterstitial(
                                frameBody.rewardedAd,
                                loadedAction = {
                                    lifecycleScope.launch(IO) {
                                        frameDataStore.writeUnlockedId(frameBody.id)
                                        ConstantsCommon.rewardedAssetsList.add(frameBody.id)
                                        withContext(Main) {
                                            updateRecycler.invoke()
                                        }
                                    }

                                },
                                failedAction = {
                                    downloadDialog?.apply { if (isShowing) dismiss() }
                                })
                        },
                        goProAction = {
                            try {
                                startActivity(
                                    Intent().apply {
                                        setClassName(
                                            applicationContext,
                                            getProScreen()
                                        )
                                    }
                                )
                            } catch (ex: Exception) {
                                Log.e("error", "onCreate: ", ex)
                            }
                        },
                        dismissAction = {},
                        frameBody.tags.lowercase() == "paid"
                    )
                } else {

                    downloadDialog = createDownloadingDialog(
                        frameBody.baseUrl, frameBody.thumb, frameBody.thumbtype
                    )

                    showNewInterstitial(homeInterstitial()) {
                        loadNewInterstitialWithoutStrategyCheck(homeInterstitial()) {}
                    }

                }
            }, declineAction = {

            })
        }
    }


}

data class FrameObject(
    var id: Int = 0,
    var name: String = "",
    var screenName: String = "",
    var subCategoryName: String = "",
    var categoryName: String = "",
    var tags: String = "",
    var baseUrl: String = "",
    var thumb: String = "",
    var thumbtype: String = "",
    var interstitialAd: Boolean = true,
    var rewardedAd: Boolean = false,
    var frameBody: Any,
    var from: String = "",
)
