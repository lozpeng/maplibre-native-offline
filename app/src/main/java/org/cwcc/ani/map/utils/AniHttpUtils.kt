package org.cwcc.ani.map.utils

import android.util.Log
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject


object AniHttpUtils {
    /**
     * 请求制定的地址返回一个JSONObject对象
     */
    fun getDataByUrl(url:String,mapParams:Map<String,String>):JSONObject
    {
        if(url.isNullOrEmpty())return JSONObject()
        var  result = JSONObject();

        val urlBuilder = url.toHttpUrl().newBuilder();
        if(!mapParams.isNullOrEmpty()) //如果制定参数列表不为空
        {
            for (entry in mapParams.entries) {
                urlBuilder.addQueryParameter(entry.key,entry.value)
            }
        }
        val httUrl = urlBuilder.build();
        try
        {
            val request: Request = Request.Builder().url(httUrl).build()
            val response = OkHttpClient().newCall(request).execute()
            if(response.isSuccessful)
            {
                var str = response.body?.string()
                result = JSONObject(str)
//                    result?.keys()?.forEach { key ->
//
//                    }
            }
        }
        catch (e:Exception)
        {
            Log.e("http",e.message,e)
        }
        return result;
    }

    /**
     * 获取数据返回一个JSONArray对象
     */
    fun getDatasByUrl(surl:String,mapParams:Map<String,String>):JSONArray{
        var result = JSONArray()
        if(surl.isNullOrEmpty())return result;

        val urlBuilder = surl.toHttpUrl().newBuilder();
        if(!mapParams.isNullOrEmpty()) //如果制定参数列表不为空
        {
            for (entry in mapParams.entries) {
                urlBuilder.addQueryParameter(entry.key,entry.value)
            }

        }
        val url = urlBuilder.build();

        val httUrl = urlBuilder.build();
        try
        {
            val request: Request = Request.Builder().url(httUrl).build()
            val response = OkHttpClient().newCall(request).execute()
            if(response.isSuccessful)
            {
                var str = response.body?.string()
                result = JSONArray(str)
            }
        }
        catch (e:Exception)
        {
            Log.e("http",e.message,e)
        }
        return result;
    }
}