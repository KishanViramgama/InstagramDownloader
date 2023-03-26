package com.app.instancedownload.ui.show.activity

import android.app.WallpaperManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.app.instancedownload.R
import com.app.instancedownload.databinding.ActivityImageShowBinding
import com.app.instancedownload.ui.show.adapter.ShowAdapter
import com.app.instancedownload.ui.videoplayer.activity.VideoPlayerActivity
import com.app.instancedownload.util.*
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.io.File
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class ShowActivity : AppCompatActivity() {

    private lateinit var type: String
    private lateinit var myAnim: Animation
    private lateinit var showAdapter: ShowAdapter
    private lateinit var showArray: MutableList<File>
    private lateinit var binding: ActivityImageShowBinding

    @Inject
    lateinit var method: Method

    @Inject
    lateinit var myDataStore: MyDataStore

    @Inject
    lateinit var mutableLiveData: MutableLiveData<LiveDataType<String>>

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_show)

        changeStatusBarColor(this)

        val selectedPosition = intent.getIntExtra("position", 0)
        type = intent.getStringExtra("type")!!

        showArray = ArrayList()
        myAnim = AnimationUtils.loadAnimation(this@ShowActivity, R.anim.bounce)
        method.bannerAd(binding.conAdViewImageShow)

        binding.toolbarImageShow.title = ""
        setSupportActionBar(binding.toolbarImageShow)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        when (type) {
            "image" -> showArray = Constant.imageArray
            "video" -> showArray = Constant.videoArray
        }

        val zoomOutTransformation = ZoomOutTransformation()
        binding.viewpagerImageShow.setPageTransformer(true, zoomOutTransformation)
        showAdapter = ShowAdapter(
            this@ShowActivity,
            showArray,
            type,
            method, backResult = { position, type, data ->
                startActivity(Intent(this, VideoPlayerActivity::class.java).putExtra("link", data))
            }
        )
        binding.viewpagerImageShow.adapter = showAdapter
        binding.viewpagerImageShow.addOnPageChangeListener(viewPagerPageChangeListener)
        setCurrentItem(selectedPosition)

        checkImage()

        binding.conSetAsWallImageShow.setOnClickListener {
            binding.imageViewSetAsWallImageShow.startAnimation(myAnim)
            CropImage.activity(Uri.fromFile(File(showArray[binding.viewpagerImageShow.currentItem].toString())))
                .start(this@ShowActivity)
        }

        binding.conDeleteImageShow.setOnClickListener {
            binding.imageViewDeleteImageShow.startAnimation(myAnim)

            myDataStore.isDelete.asLiveData().observe(this) {
                if (it) {
                    val builder =
                        MaterialAlertDialogBuilder(this@ShowActivity, R.style.DialogTitleTextStyle)
                    builder.setMessage(resources.getString(R.string.delete_title))
                    builder.setIcon(R.mipmap.ic_launcher)
                    builder.setCancelable(false)
                    builder.setNegativeButton(resources.getString(R.string.no)) { _: DialogInterface?, _: Int -> }
                    builder.setPositiveButton(resources.getString(R.string.yes)) { _: DialogInterface?, _: Int -> deleteFile() }
                    val alertDialog = builder.create()
                    alertDialog.show()
                } else {
                    deleteFile()
                }
            }

        }

        binding.conShareImageShow.setOnClickListener {
            binding.imageViewShareImageShow.startAnimation(myAnim)
            when (type) {
                "image" -> method.share(
                    this,
                    showArray[binding.viewpagerImageShow.currentItem].toString(), "image"
                )
                "video" -> method.share(
                    this,
                    showArray[binding.viewpagerImageShow.currentItem].toString(), "video"
                )
            }
            Toast.makeText(
                this@ShowActivity, resources.getString(R.string.share), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setCurrentItem(position: Int) {
        binding.viewpagerImageShow.setCurrentItem(position, false)
    }

    //page change listener
    var viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            checkImage()
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    private fun deleteFile() {
        if (showArray.size != 0) {
            try {
                val files = File(showArray[binding.viewpagerImageShow.currentItem].toString())
                files.delete()
                showArray.removeAt(binding.viewpagerImageShow.currentItem)
                showAdapter.notifyDataSetChanged()
                if (showArray.size == 0) {
                    onBackPressed()
                }
                mutableLiveData.value = LiveDataType.callObserver(Type.ADAPTER, 0, "")
                Toast.makeText(
                    this@ShowActivity, resources.getString(R.string.delete), Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                method.alertBox(this, resources.getString(R.string.wrong))
            }
        }
    }

    fun checkImage() {
        if (type == "video") {
            binding.imageViewLineTwoImageShow.gone()
            binding.conSetAsWallImageShow.gone()
        } else {
            binding.imageViewLineTwoImageShow.visible()
            binding.conSetAsWallImageShow.visible()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                try {
                    val myBitmap =
                        BitmapFactory.decodeStream(contentResolver.openInputStream(resultUri))
                    val myWallpaperManager = WallpaperManager.getInstance(applicationContext)
                    myWallpaperManager.setBitmap(myBitmap)
                } catch (e: IOException) {
                    Log.d("data_app", e.toString())
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Log.d("data_app", error.toString())
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

}