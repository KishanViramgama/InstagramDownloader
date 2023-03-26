package com.app.instancedownload.ui.setting.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BuildCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.asLiveData
import androidx.viewpager.widget.ViewPager
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textview.MaterialTextView
import com.app.instancedownload.R
import com.app.instancedownload.databinding.ActivitySettingBinding
import com.app.instancedownload.ui.aboutus.activity.AboutUsActivity
import com.app.instancedownload.ui.information.activity.InformationActivity
import com.app.instancedownload.ui.privacypolicy.activity.PrivacyPolicyActivity
import com.app.instancedownload.ui.welcome.activity.WelcomeActivity
import com.app.instancedownload.util.Method
import com.app.instancedownload.util.MyDataStore
import dagger.hilt.android.AndroidEntryPoint
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
@BuildCompat.PrereleaseSdkCheck
class SettingActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingBinding

    @Inject
    lateinit var method: Method

    @Inject
    lateinit var myDataStore: MyDataStore

    private lateinit var myAnim: Animation
    private var themMode: String = ""

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    @SuppressLint("SetTextI18n", "NonConstantResourceId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)

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

        myAnim = AnimationUtils.loadAnimation(this@SettingActivity, R.anim.bounce)

        binding.toolbarSetting.title = resources.getString(R.string.setting)
        setSupportActionBar(binding.toolbarSetting)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        method.bannerAd(binding.conAdViewSetting)

        myDataStore.themSetting.asLiveData().observe(this) {
            when (it) {
                "system" -> {
                    themMode = "system"
                    binding.textViewThemNameSetting.text =
                        resources.getString(R.string.system_default)
                }
                "light" -> {
                    themMode = "light"
                    binding.textViewThemNameSetting.text = resources.getString(R.string.light)
                }
                "dark" -> {
                    themMode = "dark"
                    binding.textViewThemNameSetting.text = resources.getString(R.string.dark)
                }
                else -> {}
            }
        }

        myDataStore.isDelete.asLiveData().observe(this) {
            binding.switchDeleteSetting.isChecked = it
        }

        binding.textViewCashSetting.text = (resources.getString(R.string.cash_file)
                + " " + DecimalFormat("##.##")
            .format(
                (FileUtils.sizeOfDirectory(cacheDir) + FileUtils.sizeOfDirectory(
                    File(
                        externalCacheDir!!.absolutePath
                    )
                )) / (1024 * 1024)
            )
                + " " + resources.getString(R.string.mb))

        binding.imageViewClearSetting.setOnClickListener {
            binding.imageViewClearSetting.startAnimation(myAnim)
            FileUtils.deleteQuietly(cacheDir)
            try {
                FileUtils.deleteDirectory(File(externalCacheDir!!.absolutePath))
            } catch (e: IOException) {
                e.printStackTrace()
            }
            binding.textViewCashSetting.text =
                resources.getString(R.string.cash_file) + " " + FileUtils.sizeOfDirectory(
                    cacheDir
                ) + " " + resources.getString(R.string.mb)

        }

        binding.conThemSetting.setOnClickListener {

            var sendThemMode: String = ""

            val dialog = Dialog(this@SettingActivity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_them)
            dialog.window!!.setLayout(
                ViewPager.LayoutParams.MATCH_PARENT,
                ViewPager.LayoutParams.WRAP_CONTENT
            )
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroup_them)
            val textViewOk = dialog.findViewById<MaterialTextView>(R.id.textView_ok_them)
            val textViewCancel = dialog.findViewById<MaterialTextView>(R.id.textView_cancel_them)

            when (themMode) {
                "system" -> radioGroup.check(radioGroup.getChildAt(0).id)
                "light" -> radioGroup.check(radioGroup.getChildAt(1).id)
                "dark" -> radioGroup.check(radioGroup.getChildAt(2).id)
                else -> {}
            }

            radioGroup.setOnCheckedChangeListener { group: RadioGroup, checkedId: Int ->
                val rb = group.findViewById<MaterialRadioButton>(checkedId)
                if (null != rb && checkedId > -1) {
                    when (checkedId) {
                        R.id.radioButton_system_them -> sendThemMode = "system"
                        R.id.radioButton_light_them -> sendThemMode = "light"
                        R.id.radioButton_dark_them -> sendThemMode = "dark"
                        else -> {}
                    }
                }
            }

            textViewOk.setOnClickListener {
                dialog.dismiss()
                if (sendThemMode != "") {
                    if (themMode != sendThemMode) {
                        CoroutineScope(Dispatchers.IO).launch {
                            myDataStore.themSetting(sendThemMode)
                            withContext(Dispatchers.Main) {
                                startActivity(
                                    Intent(
                                        this@SettingActivity,
                                        WelcomeActivity::class.java
                                    )
                                )
                                finishAffinity()
                            }
                        }
                    }
                }
            }

            textViewCancel.setOnClickListener { dialog.dismiss() }
            dialog.show()

        }

        binding.switchDeleteSetting.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            CoroutineScope(Dispatchers.IO).launch {
                myDataStore.isDelete(isChecked)
            }
        }

        binding.conAboutUsSetting.setOnClickListener {
            startActivity(
                Intent(
                    this@SettingActivity,
                    AboutUsActivity::class.java
                )
            )
        }

        binding.conPrivacyPolicySetting.setOnClickListener {
            startActivity(
                Intent(
                    this@SettingActivity,
                    PrivacyPolicyActivity::class.java
                )
            )
        }

        binding.conGuidanceSetting.setOnClickListener {
            startActivity(
                Intent(
                    this@SettingActivity,
                    InformationActivity::class.java
                )
            )
        }

        binding.conShareAppSetting.setOnClickListener {
            try {
                val string =
                    "${resources.getString(R.string.Let_me_recommend_you_this_application)} https://play.google.com/store/apps/details?id=${application.packageName}"
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, string)
                intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name))
                startActivity(
                    Intent.createChooser(
                        intent,
                        resources.getString(R.string.choose_one)
                    )
                )
            } catch (e: Exception) {
                e.toString();
            }
        }

        binding.conRateAppSetting.setOnClickListener {
            val uri = Uri.parse("market://details?id=" + application.packageName)
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + application.packageName)
                    )
                )
            }
        }

        binding.conMoreAppSetting.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(
                        resources.getString(R.string.play_more_app)
                    )
                )
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}