package com.github.kittinunf.fuse.sample.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuse.sample.CacheRepository
import com.github.kittinunf.fuse.sample.LocalTime
import com.github.kittinunf.fuse.sample.LocalTimeServiceImpl
import com.github.kittinunf.fuse.sample.NetworkRepository
import com.github.kittinunf.fuse.sample.R
import com.github.kittinunf.fuse.sample.databinding.ActivityMainBinding
import com.github.kittinunf.result.Result

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var binding: ActivityMainBinding

    private val service by lazy {
        LocalTimeServiceImpl(
            network = NetworkRepository(),
            cache = CacheRepository(this)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val place = "Asia/Bangkok"

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
                service.getFromBoth(place) {
                    count++
                    updateTitle(count.toString())
                    updateResult(it)
                }
            }

            cacheIfNotExpired.setOnClickListener {
                setLoading(true)
                service.getFromCacheIfNotExpired(place) { result, source ->
                    updateTitle(source.name)
                    updateResult(result)
                }
            }
        }
    }

    private fun updateResult(result: Result<LocalTime, Exception>) {
        setLoading(false)
        with(binding) {
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
