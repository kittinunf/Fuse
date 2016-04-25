# Fuse

Fuse is a simple generic LRU cache for Android, backed by both memory cache ([LruCache](http://developer.android.com/reference/android/util/LruCache.html)) and disk-based cache ([DiskLruCache](https://github.com/JakeWharton/DiskLruCache)) by Jake Wharton 

Life is too short and need no more elaboration? Here is how you might wanna use Fuse to cache your network call.

```Kotlin
Fuse.init(cacheDir.path)

Fuse.jsonCache.get(URL("http://jsonplaceholder.typicode.com/users/1")) { result ->
    result.success { json ->
        //do something with json
    }
}
```

Really, that is it.

## How it works?

1. Fuse searches at 1st layer at LruCache (Memory), if found, delivers. If not found go to 2.
2. Fuse searches at 2nd layer at DiskLruCache (Disk), if found delivers, If not found go to 3.
3. Fuse performs fetch (by conformance with [Fetcher](https://github.com/kittinunf/Fuse/blob/master/fuse/src/main/kotlin/com/github/kittinunf/fuse/core/fetch/Fetcher.kt) interface), then store into LruCache and DiskCache, respectively. Subsequent uses will be much faster by going through 1 & 2. 
