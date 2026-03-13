package com.fahad.newtruelovebyfahad.ui.fragments.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.abdul.pencil_sketch.main.activity.PencilSketchActivity
import com.example.ads.admobs.utils.loadNewInterstitial
import com.example.ads.admobs.utils.showNewInterstitial
import com.example.ads.dialogs.ExitModel
import com.example.ads.dialogs.createExitDialog
import com.example.ads.utils.homeInterstitial
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FragmentHomeBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.FrameObject
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.activities.pro.slider.SliderList.getImageListHome
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.HomeForYouAdapter
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.HomeSliderAdapter
import com.fahad.newtruelovebyfahad.utils.Permissions
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.navigateFragment
import com.fahad.newtruelovebyfahad.utils.printLog
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.showToast
import com.fahad.newtruelovebyfahad.utils.visible
import com.project.common.datastore.FrameDataStore
import com.project.common.utils.ConstantsCommon.isNetworkAvailable
import com.project.common.utils.enums.MainMenuOptions
import com.project.common.utils.eventForGalleryAndEditor
import com.project.common.utils.getProScreen
import com.project.common.utils.setDrawable
import com.project.common.utils.setOnSingleClickListener
import com.project.common.viewmodels.HomeAndTemplateViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeForYouFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private var navController: NavController? = null
    private val slideHandler = Handler(Looper.getMainLooper())
    private var slideRunnable: Runnable? = null
    private var sliderPageChangeCallback: ViewPager2.OnPageChangeCallback? = null
    var exitModel: ExitModel? = null
    private var forYouFramesAdapter: HomeForYouAdapter? = null
    private val homeViewModel: HomeAndTemplateViewModel by viewModels()

    @Inject
    lateinit var frameDataStore: FrameDataStore

    val TAG = "HomeForYouFragment"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        kotlin.runCatching { navController = findNavController() }

        eventForGalleryAndEditor(Events.Screens.HOME, "", true)

        // Simple adapter — only drawable images, click navigates to HowToDrawFragment with the image path
        forYouFramesAdapter = HomeForYouAdapter(onClick = { item, position ->
            Log.i(TAG, "onCreate: clicked position $position path=${item.path}")
            activity?.showNewInterstitial(activity?.homeInterstitial()) {
                activity?.loadNewInterstitial(activity?.homeInterstitial()) {}
                kotlin.runCatching {
                    navController?.navigate(
                        HomeForYouFragmentDirections.actionHomeForYouFragmentToHowToDrawFragment(
                            item.path
                        )
                    )
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    fun hideScreenAds() {
        if (isProVersion()) {
            _binding?.proBtn?.gone()
        } else {
            _binding?.proBtn?.gone()
        }
    }

    fun showScreenAd() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.initViews()

    }

    private fun FragmentHomeBinding.initSliderViewPager() {
        val list = getImageListHome(mContext ?: return)

        val adapter = HomeSliderAdapter(
            list
        ) { parent ->
            selectCategory(parent)
        }

        topPager.adapter = adapter

        // ✅ Build dots dynamically
        dotContainer.removeAllViews()
        val dots = Array(adapter.itemCount) { ImageView(context) }
        dots.forEachIndexed { index, imageView ->
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.marginStart = 4
            params.marginEnd = 4
            imageView.layoutParams = params
            imageView.setImageDrawable(context.setDrawable(com.project.common.R.drawable.round_un_selected))
            dotContainer.addView(imageView)
        }

        fun updateDots(selectedPosition: Int) {
            dots.forEach {
                it.setImageDrawable(context.setDrawable(com.project.common.R.drawable.round_un_selected))
            }
            if (selectedPosition in dots.indices) {
                dots[selectedPosition].setImageDrawable(context.setDrawable(com.project.common.R.drawable.round_select))
            }
        }

        if (dots.isNotEmpty()) {
            updateDots(topPager.currentItem.coerceIn(0, dots.lastIndex))
        }

        // Handle page change
        sliderPageChangeCallback?.let { topPager.unregisterOnPageChangeCallback(it) }
        sliderPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                updateDots(position)

                slideRunnable?.let {
                    slideHandler.removeCallbacks(it)
                    slideHandler.postDelayed(it, 4000)
                }
            }
        }
        sliderPageChangeCallback?.let { topPager.registerOnPageChangeCallback(it) }

        // ✅ Auto slide runnable
        slideRunnable = Runnable {
            topPager.adapter?.let {
                val currentItem = topPager.currentItem
                val nextItem = if (currentItem == it.itemCount - 1) 0 else currentItem + 1
                topPager.setCurrentItem(nextItem, nextItem != 0)
            }
        }
    }

    private fun FragmentHomeBinding.initViews() {

        if (this@HomeForYouFragment.isAdded && !this@HomeForYouFragment.isDetached) initObserver()

        initRecyclerViews()

        initListeners()
        initSliderViewPager()

        mActivity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (mActivity is MainActivity) {
                        exitModel = mActivity?.createExitDialog()
                    }
                }
            })
    }

    private fun FragmentHomeBinding.initObserver() {

        try {
            isProVersion.observe(viewLifecycleOwner) {
                if (it) {
                    _binding?.let {
                        hideScreenAds()
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.e("error", "onViewCreated: ", ex)
        }

        _binding?.let {
            it.loadingView.visible()
            it.framesRv.gone()
        }
        // Observe drawable images from ViewModel and submit to adapter
        homeViewModel.drawableImages.observe(viewLifecycleOwner) { resIds ->
            val packageName = mContext?.packageName ?: return@observe
            val items = resIds.map { resId ->
                HomeForYouAdapter.DrawableItem(
                    drawableResId = resId,
                    path = "android.resource://$packageName/$resId"
                )
            }
            forYouFramesAdapter?.submitList(items)

            _binding?.let {
                it.loadingView.gone()
                it.framesRv.visible()
            }

        }

        // Trigger loading the drawable list
        homeViewModel.loadDrawableImages()

    }

    private fun FragmentHomeBinding.initRecyclerViews() {
        framesRv.adapter = forYouFramesAdapter
    }


    private fun FragmentHomeBinding.initListeners() {

        menuContainer.setSingleClickListener {

            activity?.showNewInterstitial(activity?.homeInterstitial()) {
                activity?.loadNewInterstitial(activity?.homeInterstitial()) {}
                try {
                    activity?.navigateFragment(
                        HomeForYouFragmentDirections.actionHomeForYouFragmentToSettingFragment(), R.id.homeForYouFragment
                    )
                } catch (ex: Exception) {
                    Log.e("error", "initListeners: ", ex)
                }
            }

        }

        proBtn.setSingleClickListener {
            activity?.let {
                startActivity(Intent().apply {
                    setClassName(
                        it.applicationContext, getProScreen()
                    )
                    putExtra("from_frames", false)
                })
            }
        }

        arDrawing.setSingleClickListener {
            eventForCategoryClick(
                FrameObject(
                    screenName = "home", categoryName = Events.ParamsValues.HomeScreen.DRAWING, from = "ardrawing_btn", frameBody = ""
                )
            )

            selectCategory(MainMenuOptions.DRAWING.title)
        }

        importGallery.setSingleClickListener {
            if (isNetworkAvailable) {

                eventForCategoryClick(
                    FrameObject(
                        screenName = "home", categoryName = Events.ParamsValues.HomeScreen.IMPORT_GALLERY, from = "import_gallery_btn", frameBody = ""
                    )
                )

                selectCategory(MainMenuOptions.IMPORT_GALLERY.title)

            } else {
                context?.let { cntx ->
                    Toast.makeText(
                        cntx, com.project.common.R.string.no_internet_connect_found_please_try_again, Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }

        myWork.setOnSingleClickListener {

            activity?.showNewInterstitial(activity?.homeInterstitial()) {
                activity?.loadNewInterstitial(activity?.homeInterstitial()) {}
                kotlin.runCatching {
                    navController?.navigate(
                        HomeForYouFragmentDirections.actionGlobalSavedFragment()
                    )
                }
            }

        }

        learnDrawing.setOnSingleClickListener {

            eventForCategoryClick(
                FrameObject(
                    screenName = "home", categoryName = Events.ParamsValues.HomeScreen.LEARNING, from = "learning_btn", frameBody = ""
                )
            )

            selectCategory(MainMenuOptions.LEARNING.title)
        }

    }

    private fun selectCategory(from: String) {

        runCatching {
            when (from) {
                MainMenuOptions.DRAWING.title -> {

                    try {
                        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA
                        )
                        else arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
                        )
                        (mActivity as Permissions).checkAndRequestPermissions(*permissions, action = {
                            runCatching {
                                activity?.showNewInterstitial(activity?.homeInterstitial()) {
                                    activity?.loadNewInterstitial(activity?.homeInterstitial()) {}
                                    kotlin.runCatching {
                                        navController?.navigate(
                                            HomeForYouFragmentDirections.actionHomeForYouFragmentToDrawingFramesFragment(
                                                from, from
                                            )
                                        )
                                    }
                                }
                            }
                        }, declineAction = {})
                    } catch (ex: Exception) {
                        printLog(ex.message.toString())
                    }

                }

                MainMenuOptions.IMPORT_GALLERY.title -> {

                    try {
                        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA
                        )
                        else arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
                        )
                        (mActivity as Permissions).checkAndRequestPermissions(*permissions, action = {
                            runCatching {
                                activity?.showNewInterstitial(activity?.homeInterstitial()) {
                                    activity?.loadNewInterstitial(activity?.homeInterstitial()) {}
                                    kotlin.runCatching {
                                        val intent = Intent(mActivity, PencilSketchActivity::class.java)
                                        intent.putExtra("fromImport", true)
                                        getParentActivity()?.getActivityLauncher()?.launch(intent) //mActivity.startActivity(intent)
                                    }
                                }
                            }
                        }, declineAction = {})
                    } catch (ex: Exception) {
                        printLog(ex.message.toString())
                    }

                }

                MainMenuOptions.LEARNING.title -> {

                    context?.showToast(ContextCompat.getString(mContext ?: return, com.project.common.R.string.coming_soon))


//                    try {
//                        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
//                            Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA
//                        )
//                        else arrayOf(
//                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
//                        )
//                        (mActivity as Permissions).checkAndRequestPermissions(*permissions, action = {
//                            runCatching {
//                                activity?.showNewInterstitial(activity?.homeInterstitial()) {
//                                    activity?.loadNewInterstitial(activity?.homeInterstitial()) {}
//                                    kotlin.runCatching {
//                                        navController?.navigate(HomeForYouFragmentDirections.actionHomeForYouFragmentToLearningFramesFragment())
//                                    }
//                                }
//                            }
//                        }, declineAction = {})
//                    } catch (ex: Exception) {
//                        printLog(ex.message.toString())
//                    }

                }

                else -> {}
            }
        }
    }

    private fun getParentActivity(): MainActivity? {
        activity?.let {
            if (it is MainActivity) {
                return it
            }
        }
        return null
    }

    override fun onResume() {
        super.onResume()
        slideRunnable?.let { slideHandler.removeCallbacks(it) }
        slideRunnable?.let { slideHandler.postDelayed(it, 50) }
    }

    override fun onDestroyView() {
        slideRunnable?.let { slideHandler.removeCallbacks(it) }
        sliderPageChangeCallback?.let { callback ->
            _binding?.topPager?.unregisterOnPageChangeCallback(callback)
        }
        sliderPageChangeCallback = null
        super.onDestroyView()

        _binding = null
    }

    override fun onPause() {
        super.onPause()
        slideRunnable?.let { slideHandler.removeCallbacks(it) }
        exitModel?.dialog?.apply { if (!isDetached && isVisible && isShowing) dismiss() }
    }

    fun eventForCategoryClick(frameBody: FrameObject) {
        val bundle = Bundle().apply {
            putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)

            putString(Events.ParamsKeys.FROM, frameBody.from)

            if (frameBody.categoryName.isNotBlank()) putString(Events.ParamsKeys.CATNAME, frameBody.categoryName)

            if (frameBody.subCategoryName.isNotBlank()) putString(Events.ParamsKeys.SUB_SCREEN, frameBody.subCategoryName)
        }
        firebaseAnalytics?.logEvent(frameBody.screenName, bundle)

        Log.i(
            "firebase_events_clicks", "events: screenName: ${frameBody.screenName} bundle:  $bundle"
        )
    }

}
