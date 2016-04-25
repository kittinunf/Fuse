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

Fuse will search through at 1st layer at LruCache, if not found, then go search at 2nd layer to find at DiskLruCache. 
If Fuse cannot find suitable cache in Disk, it will fetch for data (For above example, it will make network call). 
Once the fetch finishes, data is delivered through callback, then LruCache & DiskLruCache will be filled with data for subsequent use.
