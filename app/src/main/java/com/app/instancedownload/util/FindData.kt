package com.app.instancedownload.util

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.app.instancedownload.R
import com.app.instancedownload.util.Constant.isDownload
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern
import javax.inject.Inject

class FindData @Inject constructor(private val context: Context) {

    fun data(
        stringData: String,
        backResult: (linkList: ArrayList<String>, message: String, isData: Boolean) -> Unit
    ) {
        val arrayList = ArrayList<String>()
        if (stringData.matches(Regex("https://www.instagram.com/(.*)"))) {
            val data = stringData.split(Pattern.quote("?").toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val string = data[0]
            if (isNetworkAvailable) {
                if (isDownload) {
                    val client = AsyncHttpClient()
                    client.addHeader("Accept", "application/json")
                    client.addHeader("x-requested-with", "XMLHttpRequest")
                    client.addHeader("Content-Type", "application/json;charset=UTF-8")
                    client.addHeader(
                        "user-agent",
                        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36"
                    )
                    client["$string?__a=1&__d=dis", null, object : AsyncHttpResponseHandler() {
                        override fun onSuccess(
                            statusCode: Int, headers: Array<Header>, responseBody: ByteArray
                        ) {
                            val res = String(responseBody)
                            try {
                                val jsonObject = JSONObject(res)
                                var link: String? = null
                                val objectGraphql = jsonObject.getJSONObject("graphql")
                                val objectMedia = objectGraphql.getJSONObject("shortcode_media")
                                val isVideo = objectMedia.getBoolean("is_video")
                                link = if (isVideo) {
                                    objectMedia.getString("video_url")
                                } else {
                                    objectMedia.getString("display_url")
                                }
                                arrayList.add(link)
                                try {
                                    val objectSidecar =
                                        objectMedia.getJSONObject("edge_sidecar_to_children")
                                    val jsonArray = objectSidecar.getJSONArray("edges")
                                    arrayList.clear()
                                    var edgeSidecar: String? = null
                                    for (i in 0 until jsonArray.length()) {
                                        val `object` = jsonArray.getJSONObject(i)
                                        val node = `object`.getJSONObject("node")
                                        val is_video_group = node.getBoolean("is_video")
                                        edgeSidecar = if (is_video_group) {
                                            node.getString("video_url")
                                        } else {
                                            node.getString("display_url")
                                        }
                                        arrayList.add(edgeSidecar)
                                    }
                                } catch (e: Exception) {
                                    Log.e("error_show", e.toString())
                                }
                                backResult.invoke(arrayList, "", true)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                                Log.e("error_show", e.toString())
                                backResult.invoke(
                                    arrayList,
                                    context.resources.getString(R.string.not_support),
                                    false
                                )
                            }
                        }

                        override fun onFailure(
                            statusCode: Int,
                            headers: Array<Header>,
                            responseBody: ByteArray,
                            error: Throwable
                        ) {
                            backResult.invoke(
                                arrayList, context.resources.getString(R.string.wrong), false
                            )
                        }
                    }]
                } else {
                    backResult.invoke(
                        arrayList, context.resources.getString(R.string.download_msg), false
                    )
                }
            } else {
                backResult.invoke(
                    arrayList, context.resources.getString(R.string.internet_connection), false
                )
            }
        } else {
            backResult.invoke(arrayList, context.resources.getString(R.string.not_support), false)
        }
    }

    //network check
    private val isNetworkAvailable: Boolean
        get() {
            val connectivityManager =
                (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
}