package org.cwcc.ani.map.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.AppUtils
import org.cwcc.ani.map.R
import timber.log.Timber

class ChooserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooser)

        findViewById<Button>(R.id.vectorMapButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<Button>(R.id.rasterMapButton).setOnClickListener {
            startActivity(Intent(this, RasterActivity::class.java))
        }

        findViewById<Button>(R.id.localServerButton).setOnClickListener {
            startActivity(Intent(this, LocalServerActivity::class.java))
        }
        //!在线地图浏览
        findViewById<Button>(R.id.onLineMapButton).setOnClickListener{
            startActivity(Intent(this, OnLineMapActivity::class.java))

        }
        var textView:TextView = findViewById<TextView>(R.id.txtViewSha)
        var shaCode:String ="SHA代码：\n"

        for (i in 0 until AppUtils.getAppSignaturesSHA1().size) {
            Timber.tag("SHA1").v(AppUtils.getAppSignaturesSHA1().get(i))
            var str:String  = AppUtils.getAppSignaturesSHA1().get(i)
            Log.v("SHA1",str)
            shaCode+=str
            shaCode+="\n"
        }
        val version =  shaCode + "\n 版本号："+  org.maplibre.android.BuildConfig.MAPLIBRE_VERSION_STRING
        textView.text = version
    }
}