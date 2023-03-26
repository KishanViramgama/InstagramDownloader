package com.app.instancedownload.ui.privacypolicy.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BuildCompat
import androidx.databinding.DataBindingUtil
import com.app.instancedownload.R
import com.app.instancedownload.databinding.ActivityPrivacyPolicyBinding
import com.app.instancedownload.util.Method
import dagger.hilt.android.AndroidEntryPoint
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
@BuildCompat.PrereleaseSdkCheck
class PrivacyPolicyActivity : AppCompatActivity() {

    lateinit var binding: ActivityPrivacyPolicyBinding

    @Inject
    lateinit var method: Method

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_privacy_policy)

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

        binding.toolbarPrivacyPolicy.title = resources.getString(R.string.privacy_policy)
        setSupportActionBar(binding.toolbarPrivacyPolicy)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        method.bannerAd(binding.conAdViewPrivacyPolicy)

        val str: String = try {
            val string = assets.open("privarcypolicy.txt")
            val size = string.available()
            val buffer = ByteArray(size) // Read the entire asset into a local byte buffer.
            string.read(buffer)
            string.close()
            String(buffer) // Convert the buffer into a string.
        } catch (e: IOException) {
            throw RuntimeException(e) // Should never happen!
        }

        binding.webViewPrivacyPolicy.setBackgroundColor(Color.TRANSPARENT)
        binding.webViewPrivacyPolicy.isFocusableInTouchMode = false
        binding.webViewPrivacyPolicy.isFocusable = false
        binding.webViewPrivacyPolicy.settings.defaultTextEncodingName = "UTF-8"
        binding.webViewPrivacyPolicy.settings.javaScriptEnabled = true

        val mimeType = "text/html"
        val encoding = "utf-8"
        val text = ("<html><head>"
                + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/font/latoregular.ttf\")}body{font-family: MyFont;color: " + method.webViewText() + "}"
                + "</style></head>"
                + "<body>"
                + str
                + "</body></html>")
        binding.webViewPrivacyPolicy.loadDataWithBaseURL(null, text, mimeType, encoding, null)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

}