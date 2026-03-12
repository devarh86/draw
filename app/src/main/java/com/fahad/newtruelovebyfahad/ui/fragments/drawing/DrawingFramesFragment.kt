package com.fahad.newtruelovebyfahad.ui.fragments.drawing

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.ads.admobs.utils.loadNewInterstitial
import com.example.ads.admobs.utils.showNewInterstitial
import com.example.ads.utils.homeInterstitial
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.databinding.FragmentDrawingFramesBinding
import com.fahad.newtruelovebyfahad.ui.fragments.common.CategoriesRVAdapter
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.DrawingFramesRV
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.google.android.gms.ads.nativead.NativeAd
import com.project.common.datastore.FrameDataStore
import com.project.common.utils.enums.MainMenuBlendOptions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DrawingFramesFragment : Fragment() {

    private var _binding: FragmentDrawingFramesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context
    private lateinit var mActivity: AppCompatActivity
    private lateinit var navController: NavController
    private var categoryTagsAdapter: CategoriesRVAdapter? = null

    @Inject
    lateinit var frameDataStore: FrameDataStore
    private var framesAdapter: DrawingFramesRV? = null
    private var categoriesFramesSubData: LinkedHashMap<String, List<DrawingFramesRV.FrameModel>>? = linkedMapOf()
    private var nativeAd: NativeAd? = null

    private var event = ""
    var option: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()

        option = "Drawing"

        event = when (option) {

            MainMenuBlendOptions.DRAWING.title -> {
                Events.ParamsValues.HomeScreen.DRAWING
            }

            else -> {
                Events.ParamsValues.HomeScreen.DOUBLE_EXPOSURE
            }
        }

        categoryTagsAdapter = CategoriesRVAdapter(emptyList()) { tag, position ->
            _binding?.framesRv?.scrollToPosition(0)
            _binding?.categoryTagsRv?.scrollToPosition(position)
            categoriesFramesSubData?.get(tag)?.let {
                if (it.isNotEmpty()) {
                    categoryTagsAdapter?.select()
                    if (_binding?.framesRv?.isComputingLayout != true) {
                        framesAdapter?.clearData()
                    }
                    framesAdapter?.updateDataList(it)
                    framesAdapter?.categoryName = tag.lowercase()
                } else {
                    categoryTagsAdapter?.unselect()
                }
            } ?: run {
                categoryTagsAdapter?.unselect()
            }
        }

        framesAdapter = DrawingFramesRV(mContext, arrayListOf(), nativeAd, onClick = { frameBody, position ->
            Log.d("DrawingFramesFragment", "onCreate: ${frameBody.baseUrl + frameBody.thumb}")

            activity?.showNewInterstitial(activity?.homeInterstitial()) {
                activity?.loadNewInterstitial(activity?.homeInterstitial()) {}
                kotlin.runCatching {
                    navController.navigate(
                        DrawingFramesFragmentDirections.actionDrawingFramesFragmentToHowToDrawFragment(
                            frameBody.baseUrl + frameBody.thumb
                        )
                    )
                }
            }

        }, onFavouriteClick = {

        }, onPurchaseTypeTagClick = {})
    }

    fun hideScreenAds() {
        if (isProVersion()) {
            framesAdapter?.hideRvAd()
        }
    }

    fun showScreenAds() {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrawingFramesBinding.inflate(inflater, container, false)
        binding.initViews()
        return binding.root
    }

    private fun FragmentDrawingFramesBinding.initViews() {
        categoriesFramesSubData?.clear()
        initObservers()
        initRecyclerViews()


        backPress.setSingleClickListener {
            kotlin.runCatching {
                navController.navigateUp()
            }
        }
    }

    private fun FragmentDrawingFramesBinding.initObservers() {

    }

    private fun FragmentDrawingFramesBinding.initRecyclerViews() {
        categoryTagsRv.adapter = categoryTagsAdapter
        framesRv.adapter = framesAdapter
    }

    private fun checkInternet() {
        _binding?.apply {
            if (!mActivity.isNetworkAvailable()) {
                tryNowPlaceholder.visible()
                noResultFoundTv.visible()
                loadingView.stopShimmer()
                loadingView.gone()
                framesRv.invisible()
                categoryTagsRv.invisible()
            } else {
                tryNowPlaceholder.gone()
                noResultFoundTv.gone()
                loadingView.stopShimmer()
                loadingView.gone()
                framesRv.visible()
                if (!whichCategory()) categoryTagsRv.visible()
            }
        }
    }

    private fun whichCategory(): Boolean {
        return when (option) {
            MainMenuBlendOptions.DRAWING.title -> true
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}