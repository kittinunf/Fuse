package com.github.kittinunf.fuse.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HttpbinGet(
    val args: Args = Args(),
    val headers: Headers = Headers(),
    val origin: String = "", // 14.3.47.17
    val url: String = "" // https://www.httpbin.org/get
)

@Serializable
class Args

@Serializable
data class Headers(
    @SerialName("Accept")
    val accept: String = "", // text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
    @SerialName("Accept-Encoding")
    val acceptEncoding: String = "", // gzip, deflate, br
    @SerialName("Accept-Language")
    val acceptLanguage: String = "", // en-US,en;q=0.9,ja-JP;q=0.8,ja;q=0.7,th;q=0.6
    @SerialName("Host")
    val host: String = "", // www.httpbin.org
    @SerialName("Sec-Ch-Ua")
    val secChUa: String = "", // "Chromium";v="92", " Not A;Brand";v="99", "Google Chrome";v="92"
    @SerialName("Sec-Ch-Ua-Mobile")
    val secChUaMobile: String = "", // ?0
    @SerialName("Sec-Fetch-Dest")
    val secFetchDest: String = "", // document
    @SerialName("Sec-Fetch-Mode")
    val secFetchMode: String = "", // navigate
    @SerialName("Sec-Fetch-Site")
    val secFetchSite: String = "", // none
    @SerialName("Sec-Fetch-User")
    val secFetchUser: String = "", // ?1
    @SerialName("Sec-Gpc")
    val secGpc: String = "", // 1
    @SerialName("Upgrade-Insecure-Requests")
    val upgradeInsecureRequests: String = "", // 1
    @SerialName("User-Agent")
    val userAgent: String = "", // Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36
    @SerialName("X-Amzn-Trace-Id")
    val xAmznTraceId: String = "" // Root=1-61123427-178b17555551263e1948dc5f
)
