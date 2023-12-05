package com.awning.afterglow.request

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import java.net.HttpCookie

interface HttpRequest {
    companion object {
        private const val TIMEOUT = 5000
        private val HEADERS = mapOf(Pair("Accept-Encoding", "br"))
    }

    /**
     * 请求方法
     */
    enum class Method {
        GET,
        POST
    }


    /**
     * 请求 [Bitmap]
     * @param url 请求地址
     * @param headers 请求头
     * @param cookies Cookie
     * @param timeout 超时
     * @return [Flow]
     */
    fun getBitmap(
        url: String,
        headers: Map<String, String> = HEADERS,
        cookies: List<HttpCookie>? = null,
        timeout: Int = TIMEOUT
    ): Flow<Bitmap>


    /**
     * 网络请求
     * @param method 请求方法
     * @param url 请求地址
     * @param params 参数
     * @param form 表单
     * @param headers 请求头
     * @param cookies Cookie
     * @param timeout 超时
     * @return [Flow]
     */
    fun request(
        method: Method,
        url: String,
        params: Map<String, String>? = null,
        form: Map<String, String>? = null,
        headers: Map<String, String> = HEADERS,
        cookies: List<HttpCookie>? = null,
        timeout: Int = TIMEOUT
    ): Flow<HttpResponse>


    /**
     * Get 请求
     * @param url 请求地址
     * @param params 参数
     * @param form 表单
     * @param headers 请求头
     * @param cookies Cookie
     * @param timeout 超时
     * @return [Flow]
     */
    fun get(
        url: String,
        params: Map<String, String>? = null,
        form: Map<String, String>? = null,
        headers: Map<String, String> = HEADERS,
        cookies: List<HttpCookie>? = null,
        timeout: Int = TIMEOUT
    ) = request(Method.GET, url, params, form, headers, cookies, timeout)


    /**
     * Post 请求
     * @param url 请求地址
     * @param params 参数
     * @param form 表单
     * @param headers 请求头
     * @param cookies Cookie
     * @param timeout 超时
     * @return [Flow]
     */
    fun post(
        url: String,
        params: Map<String, String>? = null,
        form: Map<String, String>? = null,
        headers: Map<String, String> = HEADERS,
        cookies: List<HttpCookie>? = null,
        timeout: Int = TIMEOUT
    ) = request(Method.POST, url, params, form, headers, cookies, timeout)
}