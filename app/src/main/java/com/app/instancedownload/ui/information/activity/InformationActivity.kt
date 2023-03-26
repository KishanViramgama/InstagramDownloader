package com.app.instancedownload.ui.information.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BuildCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.app.instancedownload.BuildConfig
import com.app.instancedownload.R
import com.app.instancedownload.databinding.ActivityInformationBinding
import com.app.instancedownload.ui.youtube.YoutubePlayActivity
import com.app.instancedownload.util.Method
import dagger.hilt.android.AndroidEntryPoint
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import javax.inject.Inject

@AndroidEntryPoint
class InformationActivity : AppCompatActivity() {

    @Inject
    lateinit var method: Method

    private lateinit var binding: ActivityInformationBinding

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    @androidx.annotation.OptIn(BuildCompat.PrereleaseSdkCheck::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_information)

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

        binding.toolbarInfo.title = resources.getString(R.string.guidance)
        setSupportActionBar(binding.toolbarInfo)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        method.bannerAd(binding.adViewInfo)

        Glide.with(this@InformationActivity).load(
            "https://img.youtube.com/vi/" + BuildConfig.youtubeId + "/0.jpg"
        ).placeholder(R.drawable.place_holder).into(binding.imageViewYoutubeInfo)

        binding.imageViewYoutubeInfo.setOnClickListener {
            startActivity(
                Intent(this@InformationActivity, YoutubePlayActivity::class.java).putExtra(
                        "id",
                        BuildConfig.youtubeId
                    )
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

}