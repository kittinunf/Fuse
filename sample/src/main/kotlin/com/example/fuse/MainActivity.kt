package com.example.fuse

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.fetch.get
import kotlinx.android.synthetic.main.activity_main.*
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

        btFetch.setOnClickListener {
//            Fuse.stringCache.get(filesDir.resolve("json.txt")) { result ->
//                result.success {
//                    Log.i(TAG, it)
//                    tvResult.text = it
//                }
//            }

            Fuse.jsonCache.get(URL("http://jsonplaceholder.typicode.com/users/1")) { result ->
                result.fold({
                    Log.i(TAG, it.toString(4))
                    tvResult.text = it.toString(4)
                }, {
                    Log.e(TAG, it.message)
                })
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