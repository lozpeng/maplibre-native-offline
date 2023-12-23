package org.cwcc.ani.map.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import org.cwcc.ani.map.R
//import pub.devrel.easypermissions.EasyPermissions

object PermissionsUtils {
    private const val REQUEST_CODE:Int = 0

    private val PERMISSIONS=arrayOf<String>(
        Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    fun permissionChecker(context: Context)
    {
        // 缺少权限时, 进入权限配置页面
//        if (!EasyPermissions.hasPermissions(context,*PERMISSIONS)) {
//            EasyPermissions.requestPermissions(
//                context as Activity,
//                context.getString(R.string.request_permissions),
//                REQUEST_CODE,
//                *PERMISSIONS
//            )
//        }
    }
}