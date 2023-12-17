package com.awning.afterglow.request

import kotlinx.coroutines.flow.onEach
import java.net.HttpCookie

abstract class HttpSession(cookies: List<HttpCookie>? = null) : HttpRequest {
    abstract val httpRequest: HttpRequest
    private var _cookies: List<HttpCookie> = cookies ?: emptyList()

    /**
     * Cookie
     */
    val cookies: List<HttpCookie>
        get() = _cookies

    final override fun getBitmap(
        url: String,
        headers: Map<String, String>,
        cookies: List<HttpCookie>?,
        timeout: Int
    ) = httpRequest.getBitmap(
        url,
        headers,
        cookies?.let { mergeCookies(_cookies, it) } ?: _cookies,
        timeout
    )


    final override fun request(
        method: HttpRequest.Method,
        url: String,
        params: Map<String, String>?,
        form: List<Pair<String, String>>?,
        headers: Map<String, String>,
        cookies: List<HttpCookie>?,
        timeout: Int
    ) = httpRequest.request(method, url, params, form, headers, cookies?.let { mergeCookies(_cookies, it) } ?: _cookies, timeout).onEach {
        it.cookies?.let { cookies ->
            _cookies = mergeCookies(_cookies, cookies)
        }
    }
}