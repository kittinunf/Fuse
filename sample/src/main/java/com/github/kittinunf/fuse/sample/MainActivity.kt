package com.github.kittinunf.fuse.sample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.result.Result

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val titleText by lazy { findViewById<TextView>(R.id.titleText) }
    private val resultText by lazy { findViewById<TextView>(R.id.resultText) }
    private val networkButton by lazy { findViewById<Button>(R.id.networkButton) }
    private val cacheButton by lazy { findViewById<Button>(R.id.cacheButton) }

    private val service by lazy {
        TimeServiceImpl(
            network = NetworkRepository(),
            cache = CacheRepository(cacheDir.path)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        networkButton.setOnClickListener {
            service.getFromNetwork("Asia", "Tokyo") {
                updateTitle()
                updateResult(it)
            }
        }

        cacheButton.setOnClickListener {
            service.getFromCache("Asia", "Bangkok") {
                updateTitle("Cache")
                updateResult(it)
            }
        }
    }

    private fun updateResult(result: Result<LocalTime, Exception>) {
        runOnUiThread {
            when (result) {
                is Result.Success -> {
                    resultText.text = "Location: ${result.value.timezone}\nTime: ${result.value.dateTime}\nTZ: ${result.value.abbrev}"
                }

                is Result.Failure -> {
                    resultText.text = result.error.message
                }
            }
        }
    }

    private fun updateTitle(text: String? = null) {
        runOnUiThread {
            titleText.text = text
        }
    }
}
