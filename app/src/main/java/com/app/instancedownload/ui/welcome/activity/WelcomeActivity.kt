package com.app.instancedownload.ui.welcome.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.BuildCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.asLiveData
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.app.instancedownload.R
import com.app.instancedownload.databinding.ActivityWelcomeBinding
import com.app.instancedownload.ui.MainActivity
import com.app.instancedownload.ui.welcome.adapter.MyViewPagerAdapter
import com.app.instancedownload.util.*
import dagger.hilt.android.AndroidEntryPoint
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@BuildCompat.PrereleaseSdkCheck
class WelcomeActivity : AppCompatActivity() {

    @Inject
    lateinit var method: Method

    @Inject
    lateinit var myDataStore: MyDataStore

    lateinit var binding: ActivityWelcomeBinding

    private lateinit var layouts: IntArray

    private var isObserver: Boolean = true

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Making notification bar transparent
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        changeStatusBarColor(this)

        myDataStore.themSetting.asLiveData().observe(this) { itThem ->
            when (itThem) {
                "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> {}
            }
            myDataStore.isFirstTimeLaunch.asLiveData().observe(this) {
                if (isObserver) {
                    if (it == true) {
                        startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                        finish()
                    } else {
                        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome)

                        layouts = intArrayOf(R.layout.welcome_slide_one, R.layout.welcome_slide_two)
                        val myViewPagerAdapter = MyViewPagerAdapter(this@WelcomeActivity, layouts)

                        binding.viewPagerMain.adapter = myViewPagerAdapter
                        binding.viewPagerMain.addOnPageChangeListener(viewPagerPageChangeListener)

                        binding.btnSkip.setOnClickListener { launchHomeScreen() }

                        binding.btnNext.setOnClickListener {
                            val current = binding.viewPagerMain.currentItem + 1
                            if (current < layouts.size) {
                                binding.viewPagerMain.currentItem = current
                            } else {
                                launchHomeScreen()
                            }
                        }
                    }
                }
            }
        }

    }

    private fun launchHomeScreen() {
        CoroutineScope(Dispatchers.IO).launch {
            isObserver = false
            myDataStore.isFirstTimeLaunch(true)
            startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
            finishAffinity()
        }
    }

    //viewpager change listener
    private var viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.size - 1) {
                // last page. make button text to GOT IT
                binding.btnNext.text = getString(R.string.start)
                binding.btnSkip.gone()
            } else {
                // still pages are left
                binding.btnNext.text = getString(R.string.next)
                binding.btnSkip.visible()
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }
}