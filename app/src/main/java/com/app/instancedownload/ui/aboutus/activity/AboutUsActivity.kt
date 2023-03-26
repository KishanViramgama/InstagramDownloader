package com.app.instancedownload.ui.aboutus.activity

import android.content.Context
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BuildCompat
import androidx.databinding.DataBindingUtil
import com.app.instancedownload.R
import com.app.instancedownload.databinding.ActivityAboutUsBinding
import com.app.instancedownload.util.Method
import dagger.hilt.android.AndroidEntryPoint
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import javax.inject.Inject

@AndroidEntryPoint
class AboutUsActivity : AppCompatActivity() {

    lateinit var binding: ActivityAboutUsBinding

    @Inject
    lateinit var method: Method

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    @androidx.annotation.OptIn(BuildCompat.PrereleaseSdkCheck::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_about_us)

        if (BuildCompat.isAtLeastT()) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                // Back is pressed... Finishing the activity
                finish()
            }
        } else {
            onBackPressedDispatcher.addCallback(this /* lifecycle owner */,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        // Back is pressed... Finishing the activity
                        finish()
                    }
                })
        }

        binding.toolbarAboutUs.title = resources.getString(R.string.about_us)
        setSupportActionBar(binding.toolbarAboutUs)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        method.bannerAd(binding.conAdViewAboutUs)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}