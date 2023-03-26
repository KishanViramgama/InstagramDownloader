package com.app.instancedownload.ui.show.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.app.instancedownload.R
import com.app.instancedownload.databinding.ShowAdapterBinding
import com.app.instancedownload.util.Method
import com.app.instancedownload.util.gone
import java.io.File

class ShowAdapter(
    private val activity: Activity,
    private val string: List<File>,
    private val type: String,
    private val method: Method,
    private val backResult: (position: Int, type: String, data: String) -> Unit
) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val binding =
            ShowAdapterBinding.inflate(LayoutInflater.from(container.context), container, false)

        if (type == "image") {
            binding.imageViewPlay.gone()
        } else if (type == "all") {
            if (string[position].toString().contains(".jpg")) {
                binding.imageViewPlay.gone()
            }
        }

        binding.imageViewPlay.setOnClickListener {
            method.onClick(activity,position, type, string[position].toString()) { position, type, data ->
                backResult.invoke(position, type, data)
            }
        }

        Glide.with(activity).load(string[position].toString())
            .placeholder(R.drawable.place_holder)
            .into(binding.imageViewImageShowAdapter)

        container.addView(binding.root)

        return binding.root
    }

    override fun getCount(): Int {
        return string.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}