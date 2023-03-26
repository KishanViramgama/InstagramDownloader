package com.app.instancedownload.ui.welcome.adapter

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.textview.MaterialTextView
import com.app.instancedownload.R

class MyViewPagerAdapter(private val activity: Activity, private val layouts: IntArray) :
    PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val layoutInflater =
            (activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)

        val view = layoutInflater.inflate(layouts[position], container, false)

        val textView = view.findViewById<MaterialTextView>(R.id.textView_welcome)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (position == 0) {
                textView.text = activity.resources.getString(R.string.intro_one_ten)
            } else {
                textView.text = activity.resources.getString(R.string.intro_two_ten)
            }
        } else {
            if (position == 0) {
                textView.text = activity.resources.getString(R.string.intro_one)
            } else {
                textView.text = activity.resources.getString(R.string.intro_two)
            }
        }

        container.addView(view)

        return view
    }

    override fun getCount(): Int {
        return layouts.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        container.removeView(view)
    }

}