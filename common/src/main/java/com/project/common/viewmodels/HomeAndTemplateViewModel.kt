package com.project.common.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeAndTemplateViewModel @Inject constructor() : ViewModel() {
    private val _drawableImages: MutableLiveData<List<Int>> = MutableLiveData()
    val drawableImages: LiveData<List<Int>> get() = _drawableImages

    /**
     * Loads the static drawable images (d_img_1 … d_img_6) from the common module
     * and posts them to [drawableImages] LiveData.
     */
    fun loadDrawableImages() {
        val images = listOf(
            com.project.common.R.drawable.d_img_1,
            com.project.common.R.drawable.d_img_2,
            com.project.common.R.drawable.d_img_3,
            com.project.common.R.drawable.d_img_4,
            com.project.common.R.drawable.d_img_5,
            com.project.common.R.drawable.d_img_6,
            com.project.common.R.drawable.cute_1,
            com.project.common.R.drawable.cute_2,
            com.project.common.R.drawable.cute_3,
            com.project.common.R.drawable.cartoon_1,
            com.project.common.R.drawable.cartoon_2,
            com.project.common.R.drawable.cartoon_3,
            com.project.common.R.drawable.animal_1,
            com.project.common.R.drawable.animal_2,
            com.project.common.R.drawable.animal_3,
            com.project.common.R.drawable.fantasy_1,
            com.project.common.R.drawable.fantasy_2,
            com.project.common.R.drawable.fantasy_3,
            com.project.common.R.drawable.food_1,
            com.project.common.R.drawable.food_2,
            com.project.common.R.drawable.food_3,
        )
        _drawableImages.value = images
    }

}
