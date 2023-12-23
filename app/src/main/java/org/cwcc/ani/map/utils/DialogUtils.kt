package org.cwcc.ani.map.utils

import android.app.AlertDialog
import android.content.Context

/**
 * 对话框帮助类
 */
object DialogUtils {
    /**
     *显示对话框
     */
    fun showDialog(context: Context, msg:String)
    {
        AlertDialog.Builder(context)
            .setMessage(msg)
            .setTitle("系统提示")
            .setNegativeButton("确认"){
                dialog,_ -> dialog.dismiss()
            }
            .create().show()
    }
    /**
     * 显示对话框
     */
    fun showDialog(context:Context,title:String,msg:String)
    {
        AlertDialog.Builder(context)
            .setMessage(msg)
            .setTitle(title)
            .setNegativeButton("确认"){
                    dialog,_ -> dialog.dismiss()
            }
            .create().show()
    }
}