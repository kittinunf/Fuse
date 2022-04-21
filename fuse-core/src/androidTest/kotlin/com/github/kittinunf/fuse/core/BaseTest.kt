package com.github.kittinunf.fuse.core

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
actual abstract class BaseTest {

    @Before
    actual fun before() {
        setUp(ApplicationProvider.getApplicationContext())
    }

    actual abstract fun setUp(any: Any)
}
