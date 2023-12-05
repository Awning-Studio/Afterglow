package com.awning.afterglow.request

import kotlinx.coroutines.flow.Flow

/**
 * [HttpResponse] 解析器
 * @param T
 */
interface HtmlParser<T> {
    /**
     * 解析 [httpResponse]
     * @param httpResponse 网络请求响应
     * @return [Flow]
     */
    fun parse(httpResponse: HttpResponse): Flow<T>
}