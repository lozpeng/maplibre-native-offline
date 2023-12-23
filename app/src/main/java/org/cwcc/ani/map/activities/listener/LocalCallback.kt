package org.cwcc.ani.map.activities.listener

import android.location.GnssStatus
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class LocalCallback:GnssStatus.Callback() {
    override fun onStarted() {
        super.onStarted()
    }
    override fun onStopped() {
        super.onStopped()
    }
    override fun onSatelliteStatusChanged(status: GnssStatus) {
        super.onSatelliteStatusChanged(status)
        var satelliteCount = status.satelliteCount;
        makeGnssStatus(status,satelliteCount)
    }
    fun makeGnssStatus(status: GnssStatus, satelliteCount: Int) {
        if (satelliteCount > 0) {
            for (i in 0 until satelliteCount) {
                var type = status.getConstellationType(i);
                if (GnssStatus.CONSTELLATION_BEIDOU == type) {
                    Log.e("是不是北斗","1289" + "provider")
                }
            }
        }
    }
}