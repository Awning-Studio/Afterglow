package com.awning.afterglow.module.secondclass

import com.awning.afterglow.module.webvpn.WebVPN
import com.awning.afterglow.module.webvpn.WebVpnAPI
import com.awning.afterglow.request.waterfall.Waterfall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject


/**
 *
 * @property username 学号
 * @property id 第二课堂用户标识
 * @property token 访问令牌
 */
class SecondClassSystem(
    val username: String,
    private val id: Int,
    private val token: String,
    private val webVPN: WebVPN?
) {
    companion object {
        private val httpRequest = Waterfall

        /**
         * 登录
         * @param username 学号
         * @param password 密码
         * @param webVPN WebVPN
         * @return [Flow]
         */
        fun login(username: String, password: String, webVPN: WebVPN? = null) = flow {
            val form = listOf(
                Pair(
                    "para",
                    "{'school':10018,'account':'${username}','password':'${password}'}"
                )
            )

            val url = webVPN?.let { WebVpnAPI.provideSecondClass(SecondClassSystemAPI.login) }
                ?: (SecondClassSystemAPI.root + SecondClassSystemAPI.login)

            (webVPN?.user?.session ?: httpRequest).post(url, form = form)
                .collect {
                    val token = it.headers["X-token"]
                    if (token == null) {
                        throw Exception("无法获取 token")
                    } else {
                        emit(
                            SecondClassSystem(
                                username,
                                JSONObject(it.text).getJSONObject("data").getInt("id"),
                                token,
                                webVPN
                            )
                        )
                    }
                }
        }
    }


    /**
     * 获取第二课堂成绩
     * @return [Flow]
     */
    fun getReport() = flow {
        val params = mapOf(Pair("para", "{'userId':$id}"))
        val headers = mapOf(Pair("X-Token", token))

        val url = webVPN?.let { WebVpnAPI.provideSecondClass(SecondClassSystemAPI.report) }
            ?: (SecondClassSystemAPI.root + SecondClassSystemAPI.report)

        (webVPN?.user?.session ?: httpRequest).get(
            url,
            params,
            headers = headers
        ).collect {
            val data = JSONObject(it.text).getJSONArray("data")

            val list = arrayListOf<SecondClassItem>()
            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)

                list.add(
                    SecondClassItem(
                        item.getString("classifyName"),
                        item.getDouble("classifyHours"),
                        item.getDouble("classifySchoolMinHours")
                    )
                )
            }
            emit(SecondClass(username, list))
        }
    }
}