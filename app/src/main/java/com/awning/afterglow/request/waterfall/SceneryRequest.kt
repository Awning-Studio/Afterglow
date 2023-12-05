package com.awning.afterglow.request.waterfall

import android.graphics.Bitmap
import android.widget.ImageView
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.awning.afterglow.request.mergeHeaders
import java.net.HttpCookie

class SceneryRequest(
    url: String,
    private val headers: Map<String, String>,
    private val cookies: List<HttpCookie>?,
    listener: Response.Listener<Bitmap>,
    errorListener: Response.ErrorListener,
) : ImageRequest(
    url,
    listener,
    0,
    0,
    ImageView.ScaleType.MATRIX,
    Bitmap.Config.ARGB_8888,
    errorListener
) {
    override fun getHeaders() = mergeHeaders(headers, cookies)

    override fun parseNetworkResponse(response: NetworkResponse?): Response<Bitmap> {
        return super.parseNetworkResponse(response)
    }
}