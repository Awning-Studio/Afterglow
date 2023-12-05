package com.awning.afterglow.request.waterfall

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.awning.afterglow.request.HttpResponse
import com.awning.afterglow.request.formatURL
import com.awning.afterglow.request.mergeHeaders
import com.awning.afterglow.request.parseCookie
import java.io.UnsupportedEncodingException
import java.net.HttpCookie
import java.nio.charset.Charset

class WaterRequest(
    method: Int,
    url: String,
    params: Map<String, String>?,
    private val form: Map<String, String>?,
    private val headers: Map<String, String>,
    private val cookies: List<HttpCookie>?,
    private var listener: Response.Listener<HttpResponse>?,
    errorListener: Response.ErrorListener
) : Request<HttpResponse>(method, formatURL(url, params), errorListener) {
    // 线程锁
    private val mLock = Any()


    /**
     * 解析结果
     * @param response
     * @return
     */
    override fun parseNetworkResponse(response: NetworkResponse): Response<HttpResponse> {
        var charset = HttpHeaderParser.parseCharset(response.headers, "UTF-8")
        val parsed = try {
            String(
                response.data,
                Charset.forName(charset)
            )
        } catch (e: UnsupportedEncodingException) {
            charset = "UTF-8"
            String(response.data)
        }

        return Response.success(
            HttpResponse(
                response.statusCode,
                url,
                parsed,
                charset,
                response.data,
                response.headers ?: mapOf(),
                parseCookie(response)
            ),
            HttpHeaderParser.parseCacheHeaders(response)
        )
    }


    /**
     * 回调
     * @param httpResponse
     */
    override fun deliverResponse(httpResponse: HttpResponse) {
        var listener: Response.Listener<HttpResponse>?
        synchronized(mLock) {
            listener = this.listener
        }
        listener?.onResponse(httpResponse)
    }


    /**
     * 取消请求
     */
    override fun cancel() {
        super.cancel()
        synchronized(mLock) {
            listener = null
        }
    }


    /**
     * 获取表单
     * @return
     */
    override fun getParams() = form


    /**
     * 获取请求头
     * @return
     */
    override fun getHeaders() = mergeHeaders(headers, cookies)
}