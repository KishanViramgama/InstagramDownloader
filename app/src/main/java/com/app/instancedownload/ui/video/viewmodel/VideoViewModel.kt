package com.app.instancedownload.ui.video.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.instancedownload.R
import com.app.instancedownload.util.Method
import com.app.instancedownload.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(val context: Context) : ViewModel() {

    @Inject
    lateinit var method: Method

    private val videoMutableLiveData: MutableLiveData<Resource<MutableList<File>>> =
        MutableLiveData()
    val videoLiveData: LiveData<Resource<MutableList<File>>> = videoMutableLiveData

    fun getVideo() {
        CoroutineScope(Dispatchers.IO).launch {
            videoMutableLiveData.postValue(Resource.loading(null))
            try {
                val files = File(method.download.toString()).listFiles()
                val fileNames: MutableList<File> = arrayListOf()
                for (i in 0 until files!!.size) {
                    if (files[i].path.endsWith(".mp4")) {
                        fileNames.add(File(files[i].path))
                    }
                }
                fileNames.sort()
                fileNames.reverse()
                videoMutableLiveData.postValue(Resource.success(fileNames))
            } catch (e: Exception) {
                videoMutableLiveData.postValue(
                    Resource.error(
                        context.resources.getString(R.string.wrong), null
                    )
                )
            }
        }
    }

}