package com.example.fuse

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.fetch.get
import com.github.kittinunf.result.success
import kotlinx.android.synthetic.main.activity_main.btClear
import kotlinx.android.synthetic.main.activity_main.btDiskFetch
import kotlinx.android.synthetic.main.activity_main.btNetworkFetch
import kotlinx.android.synthetic.main.activity_main.tvResult
import java.io.FileOutputStream
import java.net.URL

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Fuse.init(cacheDir.path)

        Fuse.stringCache.get("hello") { result ->
        }

        btNetworkFetch.setOnClickListener {
            Fuse.jsonCache.get(URL("http://httpbin.org/get")) { result, type ->
                result.fold({
                    Log.i(TAG, it.toString(4))
                    tvResult.text = "From: ${type.name}, \n ${it.toString(4)}"
                }, {
                    Log.e(TAG, it.message)
                })
            }
        }

        btDiskFetch.setOnClickListener {
            Fuse.stringCache.get(filesDir.resolve("json.txt")) { result ->
                result.success {
                    Log.i(TAG, it)
                    tvResult.text = it
                }
            }
        }

        btClear.setOnClickListener {
            tvResult.text = ""
        }

        writeFromAssetToFileIfNeeded("json.txt")
    }

    fun writeFromAssetToFileIfNeeded(fileName: String) {
        if (filesDir.resolve(fileName).exists().not()) {
            val stream = assets.open(fileName)
            val bytes = stream.readBytes()
            FileOutputStream(filesDir.resolve(fileName)).use {
                it.write(bytes)
            }
        }
    }
}