package com.awning.afterglow.request

import java.net.HttpCookie

/**
 * 请求响应
 * @property statusCode 状态码
 * @property url url
 * @property text 响应文本
 * @property charset 编码
 * @property data 响应二进制数据
 * @property headers 响应头
 * @property cookies Cookie
 * @constructor
 */
data class HttpResponse(
    val statusCode: Int,
    val url: String,
    val text: String,
    val charset: String,
    val data: ByteArray,
    val headers: Map<String, String>,
    val cookies: List<HttpCookie>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpResponse

        if (statusCode != other.statusCode) return false
        if (url != other.url) return false
        if (text != other.text) return false
        if (!data.contentEquals(other.data)) return false
        if (headers != other.headers) return false
        if (cookies != other.cookies) return false

        return true
    }

    override fun hashCode(): Int {
        var result = statusCode
        result = 31 * result + url.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + (cookies?.hashCode() ?: 0)
        return result
    }
}
