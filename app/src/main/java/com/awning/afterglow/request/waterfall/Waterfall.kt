package com.awning.afterglow.request.waterfall

import android.graphics.Bitmap
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkError
import com.android.volley.ParseError
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.awning.afterglow.ApplicationContext
import com.awning.afterglow.request.HttpRequest
import com.awning.afterglow.request.HttpResponse
import com.awning.afterglow.request.HttpSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.net.HttpCookie

/**
 * “瀑布”网络请求，基于 [Volley] 的 [HttpRequest] 实现
 */
object Waterfall : HttpRequest {
    private const val TAG = "Waterfall"
    private val QUEUE by lazy {
        Volley.newRequestQueue(ApplicationContext)
    }


    /**
     * 会话
     * @param cookies
     */
    class Session(cookies: List<HttpCookie>? = null) : HttpSession(cookies) {
        override val httpRequest = Waterfall
    }


    override fun getBitmap(
        url: String,
        headers: Map<String, String>,
        cookies: List<HttpCookie>?,
        timeout: Int
    ): Flow<Bitmap> = addSceneryRequest(url, headers, cookies, timeout)


    override fun request(
        method: HttpRequest.Method,
        url: String,
        params: Map<String, String>?,
        form: List<Pair<String, String>>?,
        headers: Map<String, String>,
        cookies: List<HttpCookie>?,
        timeout: Int
    ): Flow<HttpResponse> = addWaterRequest(method, url, params, form, headers, cookies, timeout)


    /**
     * 向队列添加 [SceneryRequest]
     * @param url String
     * @param headers 请求头
     * @param cookies Cookie
     * @param timeout 超时
     */
    private fun addSceneryRequest(
        url: String,
        headers: Map<String, String>,
        cookies: List<HttpCookie>?,
        timeout: Int
    ) = callbackFlow {
        QUEUE.add(
            SceneryRequest(url, headers, cookies, {
                trySend(it).isSuccess
            }) {
                close(identifyError(it))
            }.setRetryPolicy(
                DefaultRetryPolicy(
                    timeout,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            )
        ).also {
            awaitClose { it.cancel() }
        }
    }


    /**
     * 向队列添加 [WaterRequest]
     * @param method 请求方法
     * @param url 请求地址
     * @param params 参数
     * @param form 表单
     * @param headers 请求头
     * @param cookies Cookie
     * @param timeout 超时
     */
    private fun addWaterRequest(
        method: HttpRequest.Method,
        url: String,
        params: Map<String, String>?,
        form: List<Pair<String, String>>?,
        headers: Map<String, String>,
        cookies: List<HttpCookie>?,
        timeout: Int
    ) = callbackFlow {
        QUEUE.add(
            WaterRequest(method.ordinal, url, params, form, headers, cookies, {
                trySend(it)
            }) {
                close(identifyError(it))
            }.setRetryPolicy(
                DefaultRetryPolicy(
                    timeout,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            )
        ).also {
            awaitClose { it.cancel() }
        }
    }


    /**
     * 取消所有请求
     */
    fun close() {
        QUEUE.cancelAll(TAG)
    }


    private fun identifyError(error: VolleyError): Throwable {
        return when(error) {
            is TimeoutError -> Exception("请求超时")
            is AuthFailureError -> Exception("身份认证出错")
            is ServerError -> Exception("服务器错误")
            is NetworkError -> Exception("网络错误")
            is ParseError -> Exception("解析出错")
            else -> { Exception() }
        }
    }
}