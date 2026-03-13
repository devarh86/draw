package com.fahad.newtruelovebyfahad.ui.fragments.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.fahad.newtruelovebyfahad.databinding.StaggeredScreenRowItemHomeBinding
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener

class HomeForYouAdapter(
    private val onClick: (item: DrawableItem, position: Int) -> Unit
) : RecyclerView.Adapter<HomeForYouAdapter.DrawableViewHolder>() {

    private val dataList: MutableList<DrawableItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawableViewHolder {
        val binding = StaggeredScreenRowItemHomeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DrawableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DrawableViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = dataList[position]
        with(holder.binding) {
            contentIv.setImageResource(item.drawableResId)
            purchaseTagIv.visibility = android.view.View.INVISIBLE
            root.setSingleClickListener {
                onClick.invoke(item, position)
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(items: List<DrawableItem>) {
        dataList.clear()
        dataList.addAll(items)
        notifyDataSetChanged()
    }

    inner class DrawableViewHolder(val binding: StaggeredScreenRowItemHomeBinding) :
        RecyclerView.ViewHolder(binding.root)

    @Keep
    data class DrawableItem(
        @DrawableRes val drawableResId: Int,
        val path: String   // android.resource URI passed to HowToDrawFragment
    )
}
