# Fuse

[ ![Kotlin](https://img.shields.io/badge/Kotlin-1.1.4.3-blue.svg)](http://kotlinlang.org) [ ![jcenter](https://api.bintray.com/packages/kittinunf/maven/Fuse/images/download.svg) ](https://bintray.com/kittinunf/maven/Fuse/_latestVersion) [![Build Status](https://travis-ci.org/kittinunf/Fuse.svg?branch=master)](https://travis-ci.org/kittinunf/Fuse)


The simple generic LRU cache for Android, backed by both memory cache ([LruCache](http://developer.android.com/reference/android/util/LruCache.html)) and disk-based cache ([DiskLruCache](https://github.com/JakeWharton/DiskLruCache)) by Jake Wharton 

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

### Gradle

```Groovy
repositories {
    jcenter()
}
```

```Groovy
dependencies {
    compile 'com.github.kittinunf.fuse:fuse:0.1'
}
```

## Detail Usage

### Built-in Cache

By default, `Fuse` has built-in Byte, String and JSONObject cache by using `Fuse.byteCache`, `Fuse.stringCache` and `Fuse.jsonCache` respectively

### Remove

You can remove specific cache by using Key

```Kotlin
Fuse.bytesCache.remove("key") //same for stringCache and jsonCache
```

## Advanced Usage

## How it works?

1. Fuse searches at 1st layer at LruCache (Memory), if found, delivers. If not found go to 2.
2. Fuse searches at 2nd layer at DiskLruCache (Disk), if found delivers, If not found go to 3.
3. Fuse performs fetch (by conformance with [Fetcher](https://github.com/kittinunf/Fuse/blob/master/fuse/src/main/kotlin/com/github/kittinunf/fuse/core/fetch/Fetcher.kt) interface), then store into LruCache and DiskCache, respectively. Therefore, subsequent uses will be much faster by going through 1 & 2. 

## License

Fuse is released under [MIT](https://opensource.org/licenses/MIT), but as Fuse depends on LruCache and DiskLruCache. Licenses agreement on both dependencies applies.

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

