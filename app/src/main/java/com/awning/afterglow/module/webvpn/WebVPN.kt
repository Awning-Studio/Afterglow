package com.awning.afterglow.module.webvpn

import com.awning.afterglow.module.edusystem.TitleMatcher
import com.awning.afterglow.type.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import org.jsoup.Jsoup
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class WebVPN(val user: User) {
    companion object {

        /**
         * 登录
         * @param user 用户
         * @return [Flow]
         */
        fun login(user: User) = flow {
            user.session.get(WebVpnAPI.root + WebVpnAPI.cookie).collect { _ ->
                user.session.get(
                    WebVpnAPI.root + WebVpnAPI.login,
                    mapOf(Pair("service", WebVpnAPI.root + WebVpnAPI.service))
                ).collect { response ->
                    val document = Jsoup.parse(response.text)

                    if (document.title() == "资源导航登录") {
                        // 已经登录上了
                        emit(WebVPN(user))
                        return@collect
                    }

                    val execution = document.getElementById("execution")!!.attr("value")
                    val salt = document.getElementById("pwdEncryptSalt")!!.attr("value")

                    val form = mapOf(
                        Pair("username", user.username),
                        Pair("password", passwordEncode(user.password, salt)),
                        Pair("captcha", ""),
                        Pair("lt", ""),
                        Pair("_eventId", "submit"),
                        Pair("cllt", "userNameLogin"),
                        Pair("dllt", "generalLogin"),
                        Pair("execution", execution),
                    )

                    user.session.post(
                        WebVpnAPI.root + WebVpnAPI.login,
                        mapOf(
                            Pair("vpn-0", ""),
                            Pair("amp;service", WebVpnAPI.root + WebVpnAPI.service)
                        ),
                        form
                    ).catch {
                        throw Exception(TitleMatcher.MatchMessage.UserInfoError)
                    }.collect { _ ->
                        user.session.get(WebVpnAPI.root + WebVpnAPI.state)
                            .collect {
                                val json = JSONObject(it.text)

                                if (json.getInt("code") == 0) {
                                    emit(WebVPN(user))
                                } else {
                                    throw Exception("登录失败")
                                }
                            }
                    }
                }
            }
        }


        /**
         * AES 加密
         * @param password 密码
         * @param salt 盐值
         * @return [String]
         */
        private fun passwordEncode(password: String, salt: String): String {
            fun randomString(length: Int): String {
                val sequence = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678"
                val random = SecureRandom()
                val stringBuilder = StringBuilder(length)
                for (i in 0 until length) {
                    stringBuilder.append(sequence[random.nextInt(sequence.length)])
                }
                return stringBuilder.toString()
            }

            val plaintext = randomString(64) + password
            val iv = randomString(16)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKey = SecretKeySpec(salt.toByteArray(), "AES")
            val ivSpec = IvParameterSpec(iv.toByteArray())
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val encryptedBytes = cipher.doFinal(plaintext.toByteArray())

            return Base64.getEncoder().encodeToString(encryptedBytes)
        }
    }
}