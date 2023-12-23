package org.cwcc.ani.map

import android.app.Application
import android.os.StrictMode
import org.cwcc.ani.map.utils.GeoAniDebugTree
import timber.log.Timber

/**
 * 程序主入口
 */
class AniGeoMapApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(GeoAniDebugTree())
        /**
         * 解决Android系统7.0以上遇到exposed beyond app through ClipData.Item.getUri
         * https://blog.csdn.net/FrancisBingo/article/details/78248118
         */
        var builder =  StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
    }
}