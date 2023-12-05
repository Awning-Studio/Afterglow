package com.awning.afterglow.viewmodel.controller

import android.graphics.Bitmap
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.awning.afterglow.module.edusystem.EduSystem
import com.awning.afterglow.module.edusystem.EduSystemUtil
import com.awning.afterglow.module.edusystem.api.getCalendar
import com.awning.afterglow.module.edusystem.api.getExamPlan
import com.awning.afterglow.module.edusystem.api.getLevelReport
import com.awning.afterglow.module.edusystem.api.getSchoolReport
import com.awning.afterglow.module.edusystem.api.getTimetable
import com.awning.afterglow.module.secondclass.SecondClassSystem
import com.awning.afterglow.module.webvpn.WebVPN
import com.awning.afterglow.request.HttpSession
import com.awning.afterglow.request.waterfall.Waterfall
import com.awning.afterglow.type.User
import com.awning.afterglow.viewmodel.RuntimeVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit


/**
 * 用户登录控制器
 */
object LoginController {
    private var _isGettingCaptcha by mutableStateOf(false)

    /**
     * 是否正在获取验证码
     */
    val isGettingCaptcha by derivedStateOf { _isGettingCaptcha }


    /**
     * 使用 WebVPN 获取验证码
     * @param webVPN WebVPN
     * @return [Flow]
     */
    fun getCaptcha(webVPN: WebVPN?, user: User) = flow {
        if (!_isGettingCaptcha) {
            _isGettingCaptcha = true

            if (webVPN != null && webVPN.user.username == user.username) {
                EduSystem.getCaptcha(webVPN).catch {
                    _isGettingCaptcha = false
                    throw Exception("验证码获取失败: ${it.message ?: it}")
                }.collect {
                    emit(Pair(webVPN, it))
                    _isGettingCaptcha = false
                }
            } else {
                WebVPN.login(user).collect { webVPN ->
                    EduSystem.getCaptcha(webVPN).catch {
                        _isGettingCaptcha = false
                        throw Exception("验证码获取失败: ${it.message ?: it}")
                    }.collect {
                        emit(Pair(webVPN, it))
                        _isGettingCaptcha = false
                    }
                }
            }
        }
    }


    /**
     * 获取验证码
     * @param session 会话
     * @return [Flow]
     */
    fun getCaptcha(session: HttpSession) = flow {
        if (!_isGettingCaptcha) {
            _isGettingCaptcha = true

            EduSystem.getCaptcha(session).catch {
                _isGettingCaptcha = false
                throw Exception("验证码获取失败: ${it.message ?: it}")
            }.collect {
                emit(it)
                _isGettingCaptcha = false
            }
        }
    }


    /**
     * 快速登录（用于自动登录）
     * @param user 用户
     * @return [Flow]
     */
    fun login(user: User) = login(user.session, user.username, user.password, user.secondClassPwd)


    /**
     * 登录
     * @param session 会话
     * @param username 学号
     * @param password 密码
     * @param secondClassPwd 第二课堂密码
     * @param captchaContent 验证码
     * @param captcha 验证码 [Bitmap]
     * @return [Flow]
     */
    fun login(
        session: Waterfall.Session,
        username: String,
        password: String,
        secondClassPwd: String,
        captchaContent: String? = null,
        captcha: Bitmap? = null
    ) = flow {
        if (username.isBlank()) {
            throw Exception("请输入学号")
        } else if (password.isBlank()) {
            throw Exception("请输入密码")
        } else {
            // 校验学号
            EduSystemUtil.verifyUsername(username)?.let {
                throw Exception(it)
            }

            val user = User(
                username,
                password,
                secondClassPwd.ifBlank { username },
                session
            )

            // 成功回调
            suspend fun resolveLogin(eduSystem: EduSystem) {
                RuntimeVM.eduSystem = eduSystem
                SettingController.setLastUsername(username)

                emit(user)
                RuntimeVM.isLoggingIn = false

                val now = LocalDate.now()
                val coroutineScope = CoroutineScope(Dispatchers.IO)

                // 自动设置开学日期
                coroutineScope.launch {
                    SettingController.schoolStartFlow().collect { dateString ->
                        val schoolStart = LocalDate.parse(dateString)
                        eduSystem.getCalendar().catch { }.collect {
                            val sub = ChronoUnit.DAYS.between(it, now)
                            val oneTerm = 120

                            // 校验开学日期有效并且疑似为未设置状态
                            if (sub < oneTerm && schoolStart == now) {
                                // 自动设置开学日期
                                SettingController.setSchoolStart(it.toString())
                            }
                        }
                    }
                }

                // 获取课表
                coroutineScope.launch {
                    eduSystem.getTimetable().catch {}.collect {
                        ModuleController.setTimetable(it)
                    }
                }

                // 获取考试安排
                coroutineScope.launch {
                    eduSystem.getExamPlan().catch { }.collect { ModuleController.setExamPlan(it) }
                }

                // 获取课程成绩
                coroutineScope.launch {
                    eduSystem.getSchoolReport().catch { }
                        .collect { ModuleController.setSchoolReport(it) }
                }

                // 获取等级考试成绩
                coroutineScope.launch {
                    eduSystem.getLevelReport().catch { }
                        .collect { ModuleController.setLevelReport(it) }
                }

                // 获取第二课堂成绩
                coroutineScope.launch {
                    SecondClassSystem.login(user.username, user.secondClassPwd).catch { }
                        .collect { secondClassSystem ->
                            secondClassSystem.getReport().catch { }
                                .collect { ModuleController.setSecondClass(it) }
                        }
                }

                // 设置上次登录时间（主要是为了间断性同步所有，但目前未使用到）
                SettingController.setLastLoginTime(now.toString())

                // 获取全校课表
                coroutineScope.launch {
                    TimetableAllController.getTimetableAll()
                }
            }

            // 失败回调
            fun rejectLogin(error: Throwable) {
                RuntimeVM.isLoggingIn = false
                throw Exception("登录失败: ${error.message ?: error}")
            }

            RuntimeVM.isLoggingIn = true
            if (captcha == null && captchaContent == null) {
                // 无验证码
                EduSystem.login(user).catch { rejectLogin(it) }
                    .collect { resolveLogin(it) }
            } else if (captchaContent != null) {
                // 输入验证码
                EduSystem.login(user, captchaContent).catch { rejectLogin(it) }
                    .collect { resolveLogin(it) }
            } else if (captcha != null) {
                // 已加载验证码但未输入
                EduSystem.login(user, captcha).catch { rejectLogin(it) }
                    .collect { resolveLogin(it) }
            }
        }
    }


    fun login(webVPN: WebVPN?, user: User, captchaContent: String?, captcha: Bitmap?) = flow {
        // 成功回调
        suspend fun resolveLogin(webVPN: WebVPN, eduSystem: EduSystem) {
            RuntimeVM.eduSystem = eduSystem
            SettingController.setLastUsername(webVPN.user.username)

            emit(webVPN.user)
            RuntimeVM.isLoggingIn = false

            val now = LocalDate.now()
            val coroutineScope = CoroutineScope(Dispatchers.IO)

            // 自动设置开学日期
            coroutineScope.launch {
                SettingController.schoolStartFlow().collect { dateString ->
                    val schoolStart = LocalDate.parse(dateString)
                    eduSystem.getCalendar().catch { }.collect {
                        val sub = ChronoUnit.DAYS.between(it, now)
                        val oneTerm = 120

                        // 校验开学日期有效并且疑似为未设置状态
                        if (sub < oneTerm && schoolStart == now) {
                            // 自动设置开学日期
                            SettingController.setSchoolStart(it.toString())
                        }
                    }
                }
            }

            // 获取课表
            coroutineScope.launch {
                eduSystem.getTimetable().catch {}.collect {
                    ModuleController.setTimetable(it)
                }
            }

            // 获取考试安排
            coroutineScope.launch {
                eduSystem.getExamPlan().catch { }.collect { ModuleController.setExamPlan(it) }
            }

            // 获取课程成绩
            coroutineScope.launch {
                eduSystem.getSchoolReport().catch { }
                    .collect { ModuleController.setSchoolReport(it) }
            }

            // 获取等级考试成绩
            coroutineScope.launch {
                eduSystem.getLevelReport().catch { }
                    .collect { ModuleController.setLevelReport(it) }
            }

            // 获取第二课堂成绩
            coroutineScope.launch {
                SecondClassSystem.login(webVPN.user.username, webVPN.user.secondClassPwd).catch { }
                    .collect { secondClassSystem ->
                        secondClassSystem.getReport().catch { }
                            .collect { ModuleController.setSecondClass(it) }
                    }
            }

            // 设置上次登录时间（主要是为了间断性同步所有，但目前未使用到）
            SettingController.setLastLoginTime(now.toString())

            // 获取全校课表
            coroutineScope.launch {
                TimetableAllController.getTimetableAll()
            }
        }

        // 失败回调
        fun rejectLogin(error: Throwable) {
            RuntimeVM.isLoggingIn = false
            throw Exception("登录失败: ${error.message ?: error}")
        }

        RuntimeVM.isLoggingIn = true

        if (webVPN != null && webVPN.user.username == user.username) {
            if (captcha == null && captchaContent == null) {
                // 无验证码
                EduSystem.login(webVPN).catch { rejectLogin(it) }
                    .collect { resolveLogin(webVPN, it) }
            } else if (captchaContent != null) {
                // 输入验证码
                EduSystem.login(webVPN, captchaContent).catch { rejectLogin(it) }
                    .collect { resolveLogin(webVPN, it) }
            } else if (captcha != null) {
                // 已加载验证码但未输入
                EduSystem.login(webVPN, captcha).catch { rejectLogin(it) }
                    .collect { resolveLogin(webVPN, it) }
            }
        } else {
            WebVPN.login(user).collect { webVPN ->
                if (captcha == null && captchaContent == null) {
                    // 无验证码
                    EduSystem.login(webVPN).catch { rejectLogin(it) }
                        .collect { resolveLogin(webVPN, it) }
                } else if (captchaContent != null) {
                    // 输入验证码
                    EduSystem.login(webVPN, captchaContent).catch { rejectLogin(it) }
                        .collect { resolveLogin(webVPN, it) }
                } else if (captcha != null) {
                    // 已加载验证码但未输入
                    EduSystem.login(webVPN, captcha).catch { rejectLogin(it) }
                        .collect { resolveLogin(webVPN, it) }
                }
            }
        }
    }
}