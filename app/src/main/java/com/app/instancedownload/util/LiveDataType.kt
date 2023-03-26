package com.app.instancedownload.util

data class LiveDataType<out T>(val type: Type, val position: Int, val data: T?) {

    companion object {

        fun <T> callObserver(type: Type, position: Int, data: T?): LiveDataType<T> {
            return LiveDataType(type, position, data)
        }

    }
}