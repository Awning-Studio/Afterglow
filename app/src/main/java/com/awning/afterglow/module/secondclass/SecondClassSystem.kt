package com.awning.afterglow.module.secondclass

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
class SecondClassSystem(val username: String, private val id: Int, private val token: String) {
    companion object {
        private val httpRequest = Waterfall

        /**
         * 登录
         * @param username 学号
         * @param password 密码
         * @return [Flow]
         */
        fun login(username: String, password: String) = flow {
            val form = mapOf(
                Pair(
                    "para",
                    "{'school':10018,'account':'${username}','password':'${password}'}"
                )
            )

            httpRequest.post(SecondClassSystemAPI.root + SecondClassSystemAPI.login, form = form)
                .collect {
                    val token = it.headers["X-token"]
                    if (token == null) {
                        throw Exception("无法获取 token")
                    } else {
                        emit(
                            SecondClassSystem(
                                username,
                                JSONObject(it.text).getJSONObject("data").getInt("id"),
                                token
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

        httpRequest.get(
            SecondClassSystemAPI.root + SecondClassSystemAPI.report,
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