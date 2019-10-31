package com.github.kittinunf.fuse.sample.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuse.sample.CacheRepository
import com.github.kittinunf.fuse.sample.LocalTime
import com.github.kittinunf.fuse.sample.LocalTimeServiceImpl
import com.github.kittinunf.fuse.sample.NetworkRepository
import com.github.kittinunf.fuse.sample.R
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val service by lazy {
        LocalTimeServiceImpl(
            network = NetworkRepository(),
            cache = CacheRepository(cacheDir.path)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val place = "Asia/Bangkok"

        networkButton.setOnClickListener {
            service.getFromNetwork(place) {
                updateTitle("Network")
                updateResult(it)
            }
        }

        cacheButton.setOnClickListener {
            service.getFromCache(place) {
                updateTitle("Cache")
                updateResult(it)
            }
        }

        bothButton.setOnClickListener {
            var count = 0
            service.getFromBoth(place) {
                count++

                updateTitle(count.toString())
                updateResult(it)
            }
        }

        cacheIfNotExpired.setOnClickListener {
            service.getFromCacheIfNotExpired(place) { result, source ->
                updateTitle(source.name)
                updateResult(result)
            }
        }
    }

    private fun updateResult(result: Result<LocalTime, Exception>) {
        when (result) {
            is Result.Success -> {
                resultText.text =
                    "Location: ${result.value.timezone}\nTime: ${result.value.dateTime}\nTZ: ${result.value.abbrev}"
            }

            is Result.Failure -> {
                resultText.text = result.error.message
            }
        }
    }

    private fun updateTitle(text: String? = null) {
        titleText.text = text
    }
}
