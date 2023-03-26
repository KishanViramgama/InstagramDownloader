package com.app.instancedownload.util

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class Util {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }
    
    @Provides
    @Singleton
    fun provideMethod(context: Context): Method {
        return Method(context)
    }

    @Provides
    @Singleton
    fun provideMyDataStore(context: Context): MyDataStore {
        return MyDataStore(context)
    }

    @Provides
    @Singleton
    fun provideMutableLiveData(): MutableLiveData<LiveDataType<String>> =
        MutableLiveData<LiveDataType<String>>()

    @Provides
    @Singleton
    fun provideLiveData(mutableData: MutableLiveData<LiveDataType<String>>): LiveData<LiveDataType<String>> =
        mutableData


}