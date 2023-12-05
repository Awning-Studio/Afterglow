package com.awning.afterglow.request

import com.android.volley.NetworkResponse
import java.net.HttpCookie
import java.net.URLEncoder

/**
 * 解析 Set-Cookie 值
 * @param response
 * @return [List]
 */
fun parseCookie(response: NetworkResponse): List<HttpCookie>? {
    response.headers?.get("Set-Cookie")?.let {
        return HttpCookie.parse(it)
    }
    return null
}


/**
 * 将 URL 与参数拼接
 * @param url
 * @param params
 * @return [String]
 */
fun formatURL(url: String, params: Map<String, String>?): String {
    return if (params.isNullOrEmpty()) url else url + (if (url.contains('?')) "&" else "?") + encodeParams(
        params
    )
}


/**
 * 编码参数
 * @param params 参数
 * @return [String]
 */
fun encodeParams(params: Map<String, String>): String {
    var paramsString = ""
    var isFirst = true
    params.forEach { (key, value) ->
        if (isFirst) {
            isFirst = false
            paramsString += "$key${
                if (value.isBlank()) "" else "=" + URLEncoder.encode(
                    value,
                    "UTF-8"
                )
            }"
        } else {
            paramsString += "&$key${
                if (value.isBlank()) "" else "=" + URLEncoder.encode(
                    value,
                    "UTF-8"
                )
            }"
        }
    }
    return paramsString
}


/**
 * 将 Cookie 与请求头合并（会覆盖掉请求头中的 Cookie 值）
 * @param headers
 * @param cookies
 * @return [Map]
 */
fun mergeHeaders(
    headers: Map<String, String>,
    cookies: List<HttpCookie>?
): Map<String, String> {
    return if (cookies == null) {
        headers
    } else {
        mutableMapOf<String, String>().also { mutableMap ->
            headers.forEach { (key, value) ->
                mutableMap[key] = value
            }
            mutableMap["Cookie"] = cookies.joinToString(";") { "${it.name}=${it.value}" }
        }
    }
}


/**
 * 合并 Cookies
 * @param cookies 旧 Cookie
 * @param newCookies 新 Cookie
 * @return [List]
 */
fun mergeCookies(
    cookies: List<HttpCookie>,
    newCookies: List<HttpCookie>
): List<HttpCookie> {
    return arrayListOf<HttpCookie>().also { mergedCookies ->
        mergedCookies.addAll(cookies)
        newCookies.forEach {
            if (!mergedCookies.contains(it)) {
                mergedCookies.add(it)
            }
        }
    }
}


/**
 * 为 [url] 加上 [root]（如果需要）
 * @param root 根 url
 * @param url url
 * @return [String]
 */
fun fixURL(root: String, url: String) = if (Regex("^http.*").matches(url)) url else root + url