package com.app.instancedownload.ui.image.viewmodel

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
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(val context: Context) : ViewModel() {

    @Inject
    lateinit var method: Method

    private val imageMutableLiveData: MutableLiveData<Resource<MutableList<File>>> =
        MutableLiveData()
    val imageLiveData: LiveData<Resource<MutableList<File>>> = imageMutableLiveData

    fun getImage() {
        imageMutableLiveData.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val files = File(method.download.toString()).listFiles()
                val fileNames: MutableList<File> = arrayListOf()
                for (i in 0 until files!!.size) {
                    if (files[i].path.endsWith(".jpg") || files[i].path.endsWith(".gif")) {
                        fileNames.add(File(files[i].path))
                    }
                }
                fileNames.sort()
                fileNames.reverse()
                imageMutableLiveData.postValue(Resource.success(fileNames))
            } catch (e: Exception) {
                imageMutableLiveData.postValue(
                    Resource.error(
                        context.resources.getString(R.string.wrong),
                        null
                    )
                )
            }
        }
    }

}