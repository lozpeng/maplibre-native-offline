package org.cwcc.ani.map.utils

import android.content.Context
import android.os.Environment
import timber.log.Timber
import java.io.*
import java.nio.charset.Charset

object FileUtils {
    private const val TAG = "FileUtils"
    private const val urlNull = "原文件路径不存在"
    private const val isFile = "原文件不是文件"
    private const val canRead = "原文件不能读"
    private  var message = "确定"
    private const val cFromFile = "创建原文件出错:"
    private const val ctoFile = "创建备份文件出错:"

    /**
     * 获取SD卡路径
     */
    fun getSdCardPath():String
    {
        val Dir = Environment.getExternalStorageDirectory()
        //得到一个路径，内容是内部sdcard的文件夹路径和名字
        return Dir.path
    }

    /**
     *
     */
    fun createChildFilesDir(path:String,name:String):Boolean{
        var pathMain: File = File("$path/$name")
        if(!pathMain.exists())
            pathMain.mkdirs()
        return true
    }

    fun createChildFilesDir(pathName:String):Boolean{
        var pathMain:File = File(pathName)
        if(!pathMain.exists())
            return pathMain.mkdirs()
        return false
    }

    fun deleteFiles(path:String):Boolean{
        if(path=="/"||path.length==1)return false //如果是根目录则直接返回不进行删除操作

        var file:File = File(path)
        try{
            if(file.exists()){
                if(file.isFile)file.delete()
                else if(file.isDirectory)
                {
                    file.deleteRecursively()
                }
                return true
            }
            else return false
        }
        catch(e:java.lang.Exception)
        {
            return false
        }
    }

    /**
     * 文件拷贝
     * @param fromFileUrl 旧文件地址和名称
     * @param toFileUrl 新文件地址和名称
     * @return 返回备份文件的信息，ok是成功，其它就是错误
     */
    fun copyFile(fromFileUrl: String?, toFileUrl: String?): String? {
        var fromFile: File? = null
        var toFile: File? = null
        fromFile = try {
            File(fromFileUrl)
        } catch (e: Exception) {
            return  cFromFile + e.message
        }
        toFile = try {
            File(toFileUrl)
        } catch (e: Exception) {
            return  ctoFile + e.message
        }
        if (fromFile?.exists()!=true) {
            return  urlNull
        }
        if (fromFile.isFile !=true) {
            return isFile
        }
        if (!fromFile.canRead()) {
            return canRead
        }

        // 复制到的路径如果不存在就创建
        if (toFile?.parentFile?.exists() != true) {
            toFile?.parentFile?.mkdirs()
        }
        if (toFile?.exists()==true) {
            toFile.delete()
        }
        if (toFile?.canWrite()!=true) {
            //return notWrite;
        }
        try {
            val fosfrom = FileInputStream(
                fromFile
            )
            val fosto = FileOutputStream(toFile)
            val bt = ByteArray(1024)
            var c: Int
            while (fosfrom.read(bt).also { c = it } > 0) {
                fosto.write(bt, 0, c) // 将内容写到新文件当中
            }
            //关闭数据流
            fosfrom.close()
            fosto.close()
        } catch (e: Exception) {
            e.printStackTrace()
            message = "备份失败!"
        }
        return message
    }

    /**
     * 判断文件或文件夹是否存在
     * @param filePath 文件路径
     * @return 是否存在
     */
    fun isExist(filePath: String?): Boolean {
        val file = File(filePath)
        return file.exists()
    }

    /**
     * 文件拷贝
     * @param myContext
     * @param ASSETS_NAME 要复制的文件路径及文件名
     * @param savePathName 要保存的文件路径及文件名
     */
    fun copyFileFromAssets(
        myContext: Context,
        ASSETS_NAME: String?,
        savePathName: String
    ): String? {
        return try {
            if (!File(savePathName).exists()) {
                val `is` = myContext.resources.assets
                    .open(ASSETS_NAME!!)
                val fos = FileOutputStream(savePathName)
                val buffer = ByteArray(7168)
                var count = 0
                while (`is`.read(buffer).also { count = it } > 0) {
                    fos.write(buffer, 0, count)
                }
                fos.close()
                `is`.close()
                "拷贝成功"
            } else {
                "文件不存在"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "拷贝异常"
        }
    }

    /**
     * 获取TXT文件内容
     * @param filePath 文件路径+名称
     * @param encode  字符编码类型 GB2312，UTF-8
     * @return TXT文件中的内容 String
     */
    fun openTxt(filePath: String?, encode: String): String? {
        val file = File(filePath)
        var result = ""
        if (!file.exists()) {
            //判断文件是否存在，如果不存在，则创建文件
            try {
                file.createNewFile()
            } catch (e: IOException) {
                // TODO 自动生成的 catch 块
                e.printStackTrace()
            }
        }
        try {
            //#从文件attribute.txt中读出数据
            //在内存中开辟一段缓冲区
            val buffer = ByteArray(1024 * 300) //300kb
            //得到文件输入流
            val `in` = FileInputStream(file)
            //读出来的数据首先放入缓冲区，满了之后再写到字符输出流中
            val len = `in`.read(buffer)
            //创建一个字节数组输出流
            val outputStream = ByteArrayOutputStream()
            outputStream.write(buffer, 0, len)
            //把字节输出流转String
            result = String(outputStream.toByteArray(), Charset.forName(encode))
        } catch (e: Exception) {
            // TODO: handle exception
            Timber.e("文件读取失败$e")
        }
        return result
    }

    /**
     * 在路径filePath下创建文件
     * @param filePath 文件地址+名称；
     * @param Content 内容；
     * @return 返回是否创建成功
     */
    fun saveTxt(filePath: String?, Content: String): Boolean {
        val file = File(filePath)
        if (!file.exists()) {
            //判断文件是否存在，如果不存在，则创建文件
            try {
                file.createNewFile()
            } catch (e: IOException) {
                // TODO 自动生成的 catch 块
                e.printStackTrace()
            }
        }
        try {
            //#写数据到文件XXX.txt
            //创建一个文件输出流
            val out = FileOutputStream(file, false) //true表示在文件末尾添加
            out.write(Content.toByteArray(charset("GB2312")))
            out.close()
        } catch (e: Exception) {
            // TODO: handle exception
            return false
        }
        return true
    }


    /**
     * 获取文件夹下文件列表
     * @param path 文件夹路径
     * @param type 1-all（加载全部） 2-folder（只加载文件夹） 3-.apk (加载指定后缀文件)
     * @return
     */
    fun getFileListInfo(path: String?, type: String): List<FileInfo?>? {
        var result: MutableList<FileInfo?>? = null
        try {
            val f = File(path)
            val files = f.listFiles() // 列出所有文件
            // 将所有文件存入list中
            if (files != null) {
                val count = files.size // 文件个数
                result = ArrayList()
                for (i in 0 until count) {
                    val file = files[i]
                    val file_t = FileInfo()
                    file_t.FileName = file.name
                    file_t.FilePath = file.path
                    if (type === "all") {
                        result.add(file_t)
                    } else if (type === "folder") {
                        val str = file_t.FileName
                        if (str!!.indexOf(".") == -1) { //只加载文件夹
                            result.add(file_t)
                        } else {
                            continue
                        }
                    } else {
                        var str = file_t.FileName

                        val strArray =str?.split("\\.".toRegex())?.dropLastWhile { it.isEmpty() }
                                ?.toTypedArray()
                        var size:Int? = strArray?.size
                        if(size ==null) size =0
                        val suffixIndex = size - 1
                        val indx = strArray!![suffixIndex].indexOf(type)
                        if (indx != -1) { //只加载指定类型数据
                            result.add(file_t)
                        } else {
                            continue
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return result
    }

    /**
     * 文件信息类
     */
    class FileInfo {
        var FileName // 文件或文件夹名称
                : String? = null
        var FilePath //文件或文件夹路径
                : String? = null
    }
}