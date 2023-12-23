package org.cwcc.ani.map.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    /**
     *
     */
    fun long2String(time:Long):String{
        //毫秒转秒
        //毫秒转秒
        var sec = time.toInt() / 1000
        val min = sec / 60 //分钟

        sec = sec % 60 //秒

        return if (min < 10) {    //分钟补0
            if (sec < 10) {    //秒补0
                "0$min:0$sec"
            } else {
                "0$min:$sec"
            }
        } else {
            if (sec < 10) {    //秒补0
                "$min:0$sec"
            } else {
                "$min:$sec"
            }
        }
    }

    /**
     *
     */
    @SuppressLint("SimpleDateFormat")
    fun getCurrentTime():String{
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        return sdf.format(System.currentTimeMillis())
    }
}

@SuppressLint("SimpleDateFormat")
fun Date.getCurrentTime():String{
    val sdf = SimpleDateFormat("yyyyMMddHHmmss")
    return sdf.format(System.currentTimeMillis())
}
//方法名称前面的Date.表示该方法扩展自Date类
//返回的日期时间格式形如2017-10-01 10:00:00
@SuppressLint("SimpleDateFormat")
fun Date.getNowDateTime(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return sdf.format(this)
}


//只返回日期字符串
@SuppressLint("SimpleDateFormat")
fun Date.getNowDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    return sdf.format(this)
}


//只返回时间字符串
@SuppressLint("SimpleDateFormat")
fun Date.getNowTime(): String {
    val sdf = SimpleDateFormat("HH:mm:ss")
    return sdf.format(this)
}


//返回详细的时间字符串，精确到毫秒
@SuppressLint("SimpleDateFormat")
fun Date.getNowTimeDetail(): String {
    val sdf = SimpleDateFormat("HH:mm:ss.SSS")
    return sdf.format(this)
}