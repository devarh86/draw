package com.project.common.viewmodels

import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fahad.newtruelovebyfahad.GetHomeAndTemplateScreenDataQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeAndTemplateViewModel @Inject constructor() : ViewModel() {

    var scrollX: Int = 0
    var scrollY: Int = 0
    var isExpanded: Boolean = true
    private val _homeScreen: MutableLiveData<ViewStates> = MutableLiveData()
    val homeScreen: LiveData<ViewStates> get() = _homeScreen

    private val _templateScreen: MutableLiveData<ViewStates> = MutableLiveData()
    val templateScreen: LiveData<ViewStates> get() = _templateScreen

    val homeList: MutableList<FramesModelHomeAndTemplates> = mutableListOf()

    private var homeAndTemplateJob: Job? = null

    var currentScreen = "home"

    var alreadyWorking = false

    private val templateList: MutableList<FramesModelHomeAndTemplates> = mutableListOf()

    fun getHomeTemplateScreen() {
//        Log.i("TAG", "getHomeAndTemplateScreen: ${homeList.size}")

        if (_homeScreen.value is ViewStates.Offline && homeList.isNotEmpty() && _templateScreen.value is ViewStates.Offline && templateList.isNotEmpty()) {
            _homeScreen.value = ViewStates.UpdateList(homeList)
            _templateScreen.value = ViewStates.UpdateList(templateList)
            return
        }

        when {

            currentScreen == "home" && homeList.isNotEmpty() -> {
                _homeScreen.value = ViewStates.UpdateList(homeList)
                return
            }

            currentScreen == "template" && templateList.isNotEmpty() -> {
                _templateScreen.value = ViewStates.UpdateList(templateList)
                return
            }
        }

        if (alreadyWorking) {
            return
        }

        _homeScreen.postValue(ViewStates.Loading)

        homeAndTemplateJob = viewModelScope.launch(IO) {
            kotlin.runCatching {

            }.onFailure {
                Log.i("TAG", "getHomeAndTemplateScreenErrorViewModel: ${it.message}")
                postErrorToScreens("Please try again")
            }
        }
    }

    private suspend fun processFramesList(
        framesList: GetHomeAndTemplateScreenDataQuery.Screen?,
        targetList: MutableList<FramesModelHomeAndTemplates>,
        screenType: String
    ) {
        framesList?.categories?.forEachIndexed { index, categoryObj ->
            categoryObj?.let { category ->
                val framesModel = FramesModelHomeAndTemplates().apply {
                    categoryId = category.id.toLong()
                    categoryName = category.title
                    apiOption = category.title
                    frames.addAll(category.frames.orEmpty())
                }

                if (screenType == "home" && (category.title == "AI Enhancer\uD83D\uDCAB")) { //AI Enhancer💫
                    framesModel.type = ViewHolderTypes.PHOTOEDITOR
//                    framesModel.thumbnail = R.raw.enhancer_home
                    framesModel.apiOption = framesModel.categoryName
                } else {
                    Log.i("ENHNACER_ANIM", "processFramesList:  ELSE")
                }

                targetList.add(framesModel)
                postUpdateToScreen(framesModel, screenType)

//                if (screenType == "home" && (index == 1 || index == 3)) {
//                    addSpecialCategories(index, targetList)
//                }
            } ?: postErrorToScreens("Please try again")
        } ?: run {
            postErrorToScreens("Please try again")
        }

        postListUpdateToScreen(targetList, screenType)
    }

    private suspend fun addSpecialCategories(
        index: Int,
        targetList: MutableList<FramesModelHomeAndTemplates>
    ) {
        val specialCategory = FramesModelHomeAndTemplates().apply {
            categoryName = if (index == 1) "Overlay" else "Double Exposure"
            type = ViewHolderTypes.PHOTOEDITOR
//            thumbnail = if (index == 1) R.raw.enhancer_home else R.raw.enhancer_home
            apiOption = categoryName
        }

        targetList.add(specialCategory)
        withContext(Main) {
            _homeScreen.postValue(ViewStates.UpdateObject(specialCategory))
        }
    }

    private fun postErrorToScreens(message: String) {
        _homeScreen.postValue(ViewStates.Error(message))
        _templateScreen.postValue(ViewStates.Error(message))
    }

    /*  private fun postSlowInternetToScreens(message: String) {
          _homeScreen.postValue(ViewStates.SlowInternet(message))
          _templateScreen.postValue(ViewStates.SlowInternet(message))
      }
  */
    private suspend fun postUpdateToScreen(
        framesModel: FramesModelHomeAndTemplates,
        screenType: String
    ) {
        withContext(Main) {
            when (screenType) {
                "home" -> _homeScreen.postValue(ViewStates.UpdateObject(framesModel))
                "template" -> _templateScreen.postValue(ViewStates.UpdateObject(framesModel))
            }
        }
    }

    private suspend fun postListUpdateToScreen(
        targetList: MutableList<FramesModelHomeAndTemplates>,
        screenType: String
    ) {
        withContext(Main) {
            when (screenType) {
                "home" -> _homeScreen.postValue(ViewStates.UpdateList(targetList))
                "template" -> _templateScreen.postValue(ViewStates.UpdateList(targetList))
            }
        }
    }

    fun removeFromList() {
        _homeScreen.postValue(
            ViewStates.Offline(
                emptyList<FramesModelHomeAndTemplates>().toMutableList()
            )
        )
        _templateScreen.postValue(
            ViewStates.Offline(
                emptyList<FramesModelHomeAndTemplates>().toMutableList()
            )
        )
    }

    fun resetHomeState() {
        _homeScreen.value = ViewStates.Idle
    }

    fun resetTemplateState() {
        _templateScreen.value = ViewStates.Idle
    }
}

@Keep
sealed class ViewStates() {

    data object Loading : ViewStates()
    class Error(var message: String) : ViewStates()
    class UpdateObject(var objectValue: FramesModelHomeAndTemplates) : ViewStates()

    //    class SlowInternet(var message: String) : ViewStates()
    class UpdateList(var list: MutableList<FramesModelHomeAndTemplates>) : ViewStates()
    class Offline(var list: MutableList<FramesModelHomeAndTemplates>) : ViewStates()
    data object Idle : ViewStates()
}

@Keep
data class FramesModelHomeAndTemplates(
    var categoryId: Long = 0L,
    var categoryName: String = "",
    var frames: MutableList<GetHomeAndTemplateScreenDataQuery.Frame?> = mutableListOf(),
    var type: ViewHolderTypes = ViewHolderTypes.FRAMES,
    var thumbnail: Int = -1,
    var apiOption: String = ""
)

@Keep
enum class ViewHolderTypes {
    FRAMES, PHOTOEDITOR
}
