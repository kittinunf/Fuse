package com.github.kittinunf.fuse.util

import android.os.Handler
import android.os.Looper
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class __Async<T>(val weakRef: WeakReference<T>)

fun <T, X> __Async<T>.mainThread(f: (T) -> X) {
    val r = weakRef.get() ?: return

    if (Thread.currentThread() == Looper.getMainLooper().thread) {
        f(r)
    } else {
        Handler(Looper.getMainLooper()).post { f(r) }
    }
}

fun <T> T.dispatch(executorService: ExecutorService, block: __Async<T>.() -> Unit): Future<Unit> {
    val a = __Async(WeakReference(this))
    return executorService.submit<Unit> { a.block() }
}



 
