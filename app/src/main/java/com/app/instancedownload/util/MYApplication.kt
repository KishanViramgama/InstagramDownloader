package com.app.instancedownload.util

import android.app.Application
import com.app.instancedownload.R
import dagger.hilt.android.HiltAndroidApp
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump

@HiltAndroidApp
class MYApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ViewPump.init(
            ViewPump.builder().addInterceptor(
                CalligraphyInterceptor(
                    CalligraphyConfig.Builder().setDefaultFontPath("font/latoregular.ttf")
                        .setFontAttrId(R.attr.fontPath).build()
                )
            ).build()
        )
    }

}