package org.cwcc.ani.map.utils

import android.os.Environment
import android.os.StatFs
import java.io.File

object SDCardUtils {
    /**
     * SDE卡是否可用
     */
    var isSDCardEnable={
        Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
    }

    /**
     * 获取SD卡的路径
     */
    fun getSDCardPath():String{
        return Environment.getExternalStorageDirectory().absolutePath+ File.separator
    }

    fun getSDCardAllSize():Long{
        if(isSDCardEnable())
        {
            var stat:StatFs= StatFs(getSDCardPath())
            // 获取空闲的数据块的数量
            var availableBlocks:Long = stat.availableBlocksLong -4
            // 获取单个数据块的大小（byte）
            var freeBlocks:Long = stat.blockSizeLong
            return freeBlocks*availableBlocks
        }
        return 0
    }

    /**
     * 获取可用空间大小
     */
    fun getFreeBytes(filePath:String):Long{
        var path:String=""
        if(filePath.startsWith(getSDCardPath()))
        {
            path = getSDCardPath()
        }
        else path = Environment.getDataDirectory().absolutePath
        var stat:StatFs = StatFs(path)
        var availableBlocks:Long = stat.availableBlocksLong - 4
        return stat.blockSizeLong * availableBlocks
    }

    /**
     * 获取系统存储路径
     */
    fun getRootDirectoryPath():String{
        return Environment.getRootDirectory().absolutePath
    }
}