package com.app.instancedownload.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.instancedownload.ui.image.fragment.ImageFragment
import com.app.instancedownload.ui.video.fragment.VideoFragment

class ViewPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle, private val numOfTabs: Int) :

    FragmentStateAdapter(fm, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ImageFragment()
            1 -> VideoFragment()
            else -> {
                ImageFragment()
            }
        }
    }

    override fun getItemCount(): Int {
        return numOfTabs
    }

}