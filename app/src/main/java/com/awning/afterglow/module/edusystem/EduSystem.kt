package com.awning.afterglow.module.edusystem

import android.graphics.Bitmap
import com.awning.afterglow.module.webvpn.WebVPN
import com.awning.afterglow.module.webvpn.WebVpnAPI
import com.awning.afterglow.request.HtmlParser
import com.awning.afterglow.request.HttpResponse
import com.awning.afterglow.request.HttpSession
import com.awning.afterglow.toolkit.OCR
import com.awning.afterglow.type.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import java.nio.charset.Charset
import java.time.LocalDate
import java.util.Date


/**
 * 教务系统
 * @property user 用户
 * @property withWebVPN 是否使用 WebVPN
 */
class EduSystem private constructor(val user: User, val withWebVPN: Boolean) {
    fun reLogin() = login(user)


    /**
     * 登出
     * @return [Flow]
     */
    fun logout() = flow {
        val params = mapOf(
            Pair("amp;method: exit", "exit"),
            Pair("amp;tktime", Date().time.toString())
        )

        val url =
            if (withWebVPN) WebVpnAPI.provideEduSystem(EduSystemAPI.login, 1)
            else EduSystemAPI.root + EduSystemAPI.login

        user.session.get(url, params)
            .collect {
                emit(Unit)
            }
    }

    companion object {
        /**
         * 当前学期，格式如 2023-2024-1
         */
        val Semester = currentSemester()


        /**
         * 获取当前学年字符串
         * @return 格式如 2023-2024-1 的 [String]
         */
        private fun currentSemester(): String {
            val now = LocalDate.now()
            val year = now.year
            val month = now.month.value
            return if (month in 2..8) {
                "${year - 1}-$year-2"
            } else {
                "$year-${year + 1}-1"
            }
        }


        /**
         * 获取验证码
         * @param session 会话
         * @return [Flow]
         */
        fun getCaptcha(session: HttpSession) = flow {
            session.get(EduSystemAPI.root + EduSystemAPI.base).collect {
                session.getBitmap(EduSystemAPI.root + EduSystemAPI.captcha).collect(this@flow)
            }
        }


        /**
         * 通过 WebVPN 获取验证码
         * @param webVPN
         * @return [Flow]
         */
        fun getCaptcha(webVPN: WebVPN) = flow {
            webVPN.user.session.get(WebVpnAPI.provideEduSystem(EduSystemAPI.base, 0)).collect {
                webVPN.user.session.getBitmap(
                    WebVpnAPI.provideEduSystem(
                        EduSystemAPI.captcha,
                        0
                    )
                ).collect(this@flow)
            }
        }


        /**
         * 验证用户信息
         * @param user 用户
         * @param captcha 验证码内容
         * @param withWebVPN 是否使用 WebVPN
         * @return [Flow]
         */
        private fun verify(user: User, captcha: String, withWebVPN: Boolean) = flow {
            val form = mapOf(
                Pair("USERNAME", user.username),
                Pair("PASSWORD", user.password),
                Pair("RANDOMCODE", captcha)
            )

            val url =
                if (withWebVPN) WebVpnAPI.provideEduSystem(EduSystemAPI.login, 1)
                else EduSystemAPI.root + EduSystemAPI.login

            user.session.post(url, null, form)
                .collect { httpResponse ->
                    LoginParser.parse(httpResponse).collect(this@flow)
                }
        }


        /**
         * 验证码（[String]）登录
         * @param user 用户
         * @param captcha 验证码内容
         * @return [Flow]
         */
        fun login(user: User, captcha: String) = flow {
            verify(user, captcha, false).collect {
                emit(EduSystem(user, false))
            }
        }


        /**
         * 验证码（[String]） WebVPN 登录
         * @param webVPN WebVPN
         * @param captcha 验证码内容
         * @return [Flow]
         */
        fun login(webVPN: WebVPN, captcha: String) = flow {
            verify(webVPN.user, captcha, true).collect {
                emit(EduSystem(webVPN.user, true))
            }
        }


        /**
         * 验证码（[Bitmap]）登录
         * @param user 用户
         * @param captcha 验证码 [Bitmap]
         * @return [Flow]
         */
        fun login(user: User, captcha: Bitmap) = flow {
            suspend fun retry(error: Throwable) {
                // 重新登录
                if (error.message == TitleMatcher.MatchMessage.CaptchaError) {
                    login(user).collect(this@flow)
                } else throw error
            }

            OCR.recognizeCaptcha(captcha).catch {
                retry(it)
            }.collect { captchaContent ->
                login(user, captchaContent).catch { retry(it) }.collect {
                    OCR.release()
                    emit(it)
                }
            }
        }


        /**
         * 验证码（[Bitmap]） WebVPN 登录
         * @param webVPN WebVPN
         * @param captcha 验证码[Bitmap]
         * @return [Flow]
         */
        fun login(webVPN: WebVPN, captcha: Bitmap) = flow {
            suspend fun retry(error: Throwable) {
                // 重新登录
                if (error.message == TitleMatcher.MatchMessage.CaptchaError) {
                    login(webVPN).collect(this@flow)
                } else throw error
            }

            OCR.recognizeCaptcha(captcha).catch {
                retry(it)
            }.collect { captchaContent ->
                login(webVPN, captchaContent).catch { retry(it) }.collect {
                    OCR.release()
                    emit(it)
                }
            }
        }


        /**
         * 无验证码登录
         * @param user 用户
         * @return [Flow]
         */
        fun login(user: User): Flow<EduSystem> = flow {
            getCaptcha(user.session).collect {
                login(user, it).collect(this@flow)
            }
        }


        /**
         * 无验证码 WebVPN 登录
         * @param webVPN WebVPN
         * @return [Flow]
         */
        fun login(webVPN: WebVPN): Flow<EduSystem> = flow {
            getCaptcha(webVPN).collect {
                login(webVPN, it).collect(this@flow)
            }
        }


        /**
         * 登录页解析器
         */
        object LoginParser : HtmlParser<String> {
            override fun parse(httpResponse: HttpResponse) = flow {
                val document = Jsoup.parse(httpResponse.text)

                TitleMatcher.match(document, TitleMatcher.Title.INDEX).catch {
                    if (it.message == TitleMatcher.MatchMessage.UnknownError) {
                        // 可能存在编码问题，改用 GBK
                        TitleMatcher.match(
                            Jsoup.parse(
                                String(httpResponse.data, Charset.forName("GBK"))
                            ), TitleMatcher.Title.INDEX
                        )
                    } else {
                        throw it
                    }
                }.collect {
                    emit("登录成功")
                }
            }
        }
    }
}