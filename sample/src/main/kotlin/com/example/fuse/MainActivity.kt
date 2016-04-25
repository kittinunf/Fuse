package com.example.fuse

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.fetch.get
import com.github.kittinunf.result.success
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Fuse.init(cacheDir.path)

        btFetch.setOnClickListener {
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