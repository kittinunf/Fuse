# Fuse

[![jcenter](https://api.bintray.com/packages/kittinunf/maven/Fuse/images/download.svg)](https://bintray.com/kittinunf/maven/Fuse/_latestVersion) 
[![Build Status](https://travis-ci.org/kittinunf/Fuse.svg?branch=master)](https://travis-ci.org/kittinunf/Fuse) 
[![Codecov](https://codecov.io/github/kittinunf/Fuse/coverage.svg?branch=master)](https://codecov.io/gh/kittinunf/Fuse)


The simple generic LRU cache for Android, backed by both memory cache ([LruCache](http://developer.android.com/reference/android/util/LruCache.html)) and disk-based cache ([DiskLruCache](https://github.com/JakeWharton/DiskLruCache)) by Jake Wharton 

# Installation

The core package has following dependencies;

- Kotlin - [![Kotlin](https://img.shields.io/badge/Kotlin-1.3.60-blue.svg)](http://kotlinlang.org)
- [Result](https://github.com/kittinunf/Result) - 2.2.0

```groovy
  //core
  implementation 'com.github.kittinunf.fuse:fuse:<latest-version>'
  
  //android
  implementation 'com.github.kittinunf.fuse:fuse-android:<latest-version>'
```

# How to use

`Fuse` is designed to be simple and easy to use. All you need is `CacheBuilder` to setup configuration for your cache. 

```kotlin
private val tempDir = createTempDir().absolutePath // use any readable/writable directory of your choice

val convertible = // there is 1 built-in DataConvertible, StringDataConvertible
val cache = CacheBuilder.config(tempDir, convertible) { 
  // do more configuration here
}.build()
```

Then, you can build `Cache` from the `CacheBuilder` and, you can start using your cache like;

```kotlin
cache = //cache instance that was instantiated earlier

//put value "world" for key "hello", "put" will always add new value into the cache
cache.put("hello", { "world" })

//later
cache.get("hello") // this returns Result.Success["world"]
val (result, source) = cache.getWithSource("hello") // this also returns Source which is one of the following, 1. MEM, 2. DISK, 3. ORIGIN


val result = cache.get("hello", { "world" }) // this return previously cached value otherwise it will save value "world" into the cache for later use
when (result) {
  is Success -> { // value is successfully return/fetch, result.value is data 
  }
  is Failure -> { // something wrong, check result.error for more details 
  }
}
```

### Source

`Source` gives you an information where the data is coming from.

```kotlin
enum class Source {
  ORIGIN,
  DISK,
  MEM
}
```

- ORIGIN - The data is coming from the original source. This means that it is being fetched from the `Fetcher<T>` class.
- DISK - The data is coming from the Disk cache. In this cache, it is specifically retrieved from DiskLruCache
- MEM - The data is coming from the memory cache.

All of the interfaces that provides `Source` have `WithSource` suffix, i.e. `getWithSource()` etc.

### Android Usage

For Android, it is basically a thin layer on top of the memory cache by using a [LruCache](https://developer.android.com/reference/android/util/LruCache)

```kotlin
// same configuration as above
val cache = CacheBuilder.config(tempDir, convertible) {
  // do more configuration here
  memCache = defaultAndroidMemoryCache() // this will utilize the LruCache provided by Android SDK
}.build()
```

By default, the Cache is perpetual meaning that it will never expired by itself. Please check [Detail Usage] for more information about expirable cache.

# Detailed usage

The default Cache that is provided by Fuse is a perpetual cache that will never expire the entry. In some cases, this is not what you want. If you are looking for non-perpetual cache, luckily, Fuse also provides you with a `ExpirableCache` as well. 

The usage of `ExpirableCache` is almost exactly the same as a regular cache, but with a time constraint that is configurable for the entry to be expired.

```kotlin
private val cache = CacheBuilder.config(tempDir, StringDataConvertible()).build().let(::ExpirableCache)

// cache is ExpirableCache type
val (value, error) = expirableCache.get("hello", { "world" }) // this works the same as regular cache

println(value) //Result.Success["world"]

// after 5 seconds has passed
val (value, error) = expirableCache.get("hello", { "new world" }, timeLimit = 5.seconds) // if the cached value has a lifetime longer than 5 seconds, entry becomes invalid

println(value) //Result.Success["new world"], it got refreshed as the entry is expired
```

# Sample

Please see the sample Android app that utilize Fuse in the [Sample](https://github.com/kittinunf/Fuse/tree/master/sample) folder

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

