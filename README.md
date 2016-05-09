# Fuse

Fuse is a simple generic LRU cache for Android, backed by both memory cache ([LruCache](http://developer.android.com/reference/android/util/LruCache.html)) and disk-based cache ([DiskLruCache](https://github.com/JakeWharton/DiskLruCache)) by Jake Wharton 

## TLDR 

Here is how you might wanna use Fuse to cache your network call.

```Kotlin
Fuse.init(cacheDir.path)

Fuse.jsonCache.get(URL("http://jsonplaceholder.typicode.com/users/1")) { result ->
    result.success { json ->
        //do something with json object (result is Result<JsonObject, Exception>)
    }
}

//if you wanna know source of cache, can be either MEM(LruCache), DISK(DiskLruCache), NOT_FOUND(newly fetched)
Fuse.stringCache.get(filesDir.resolve("json.txt")) { result, type ->
    //do something with string object (result is Result<String, Exception>)
    when (type) {
        Cache.Type.NOT_FOUND -> 
        Cache.Type.MEM -> 
        Cache.Type.DISK -> 
    }
}
```

Yeah, that's about it.

## Dependency

* [DiskLruCache](https://github.com/JakeWharton/DiskLruCache)

## Installation

Use [jitpack.io](https://jitpack.io/)

```Groovy
repositories {
    maven { url "https://jipack.io" }
}
```

```Groovy
dependencies {
    compile 'com.github.kittinunf:fuse:master-SNAPSHOT'
}
```

## How it works?

1. Fuse searches at 1st layer at LruCache (Memory), if found, delivers. If not found go to 2.
2. Fuse searches at 2nd layer at DiskLruCache (Disk), if found delivers, If not found go to 3.
3. Fuse performs fetch (by conformance with [Fetcher](https://github.com/kittinunf/Fuse/blob/master/fuse/src/main/kotlin/com/github/kittinunf/fuse/core/fetch/Fetcher.kt) interface), then store into LruCache and DiskCache, respectively. Subsequent uses will be much faster by going through 1 & 2. 

## Advanced Usage

## License

Fuse itself, is released under [MIT](https://opensource.org/licenses/MIT) of course, but as Fuse depends on LruCache and DiskLruCache. Licenses on dependencies still applies.

```
Copyright 2011 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

