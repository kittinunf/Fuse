package com.github.kittinunf.fuse.sample.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuse.sample.CacheService
import com.github.kittinunf.fuse.sample.LocalTime
import com.github.kittinunf.fuse.sample.LocalTimeRepositoryImpl
import com.github.kittinunf.fuse.sample.NetworkService
import com.github.kittinunf.fuse.sample.R
import com.github.kittinunf.fuse.sample.databinding.ActivityMainBinding
import com.github.kittinunf.result.Result
import kotlin.time.Duration.Companion.minutes

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val service by lazy {
        LocalTimeRepositoryImpl(
            network = NetworkService(),
            cache = CacheService(this)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val place = "Asia/Tokyo"

        with(binding) {
            networkButton.setOnClickListener {
                setLoading(true)
                service.getFromNetwork(place) {
                    updateTitle("Network")
                    updateResult(it)
                }
            }

            cacheButton.setOnClickListener {
                setLoading(true)
                service.getFromCache(place) {
                    updateTitle("Cache")
                    updateResult(it)
                }
            }

            bothButton.setOnClickListener {
                setLoading(true)
                var count = 0
                service.getFromCacheThenNetwork(place) {
                    count++
                    updateTitle(count.toString())
                    updateResult(it)
                }
            }

            cacheIfNotExpiredButton.setOnClickListener {
                setLoading(true)
                service.getFromCacheIfNotExpired(place, 5.minutes) { result, source ->
                    updateTitle(source.name)
                    updateResult(result)
                }
            }

            evictCacheButton.setOnClickListener {
                service.evictCache()
                updateTitle()
                updateResult(Result.failure(RuntimeException("Cache is evicted")))
            }
        }
    }

    private fun updateResult(result: Result<LocalTime, Exception>) {
        setLoading(false)
        with(binding) {
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
        binding.titleText.text = text
    }

    private fun setLoading(isLoading: Boolean) {
        with(binding) {
            if (isLoading) {
                progress.visibility = View.VISIBLE
                resultText.visibility = View.GONE
            } else {
                progress.visibility = View.GONE
                resultText.visibility = View.VISIBLE
            }
        }
    }
}
