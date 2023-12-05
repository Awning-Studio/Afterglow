package com.awning.afterglow.viewmodel.controller

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import com.awning.afterglow.ApplicationContext
import com.awning.afterglow.Today
import com.awning.afterglow.store.settingDataStore
import com.awning.afterglow.viewmodel.RuntimeVM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.floor


/**
 * 设置控制器（兼 ViewModel）
 */
object SettingController {
    private val KEY_LAST_USERNAME = stringPreferencesKey("LastUsername")
    private val KEY_LAST_LOGIN_TIME = stringPreferencesKey("LastLoginTime")
    private val KEY_LAST_NOTICE_ID = stringPreferencesKey("LastNoticeId")
    private val KEY_MATERIAL_YOU = booleanPreferencesKey("Material You")
    private val KEY_AUTO_LOGIN = booleanPreferencesKey("AutoLogin")
    private val KEY_SCHOOL_START = stringPreferencesKey("SchoolStart")
    private val KEY_SCHEDULE = intPreferencesKey("Schedule")
    private val KEY_REMEMBER_ME = booleanPreferencesKey("RememberMe")
    private val KEY_With_WebVPN = booleanPreferencesKey("WithWebVPN")
    private val KEY_SKIP_CAPTCHA = booleanPreferencesKey("SkipCaptcha")


    /**
     * 获取 [String] 值，可能为空
     * @param key 键
     * @param default 默认值
     * @return [Flow]
     */
    private fun getString(key: Preferences.Key<String>, default: String? = null) =
        ApplicationContext.settingDataStore.data.map {
            it[key] ?: default
        }


    /**
     * 获取 [String] 值，不为空
     * @param key 键
     * @param default 默认值
     * @return [Flow]
     */
    private fun getNotNullString(key: Preferences.Key<String>, default: String) =
        ApplicationContext.settingDataStore.data.map {
            it[key] ?: default
        }


    /**
     * 获取 [Boolean] 值
     * @param key 键
     * @param default 默认值
     * @return [Flow]
     */
    private fun getBoolean(key: Preferences.Key<Boolean>, default: Boolean = false) =
        ApplicationContext.settingDataStore.data.map {
            it[key] ?: default
        }


    /**
     * 获取 [Int] 值
     * @param key 键
     * @param default 默认值
     * @return [Flow]
     */
    private fun getInt(key: Preferences.Key<Int>, default: Int = 0) =
        ApplicationContext.settingDataStore.data.map {
            it[key] ?: default
        }


    /**
     * 设置值
     * @param key 键
     * @param value 值
     */
    private fun <T> set(key: Preferences.Key<T>, value: T) {
        RuntimeVM.viewModelScope.launch {
            ApplicationContext.settingDataStore.edit {
                it[key] = value
            }
        }
    }


    /**
     * 移除键值
     * @param key 键
     */
    private fun remove(key: Preferences.Key<*>) {
        RuntimeVM.viewModelScope.launch {
            ApplicationContext.settingDataStore.edit {
                it.remove(key)
            }
        }
    }


    /**
     * 最后登录用户 flow
     * @return [Flow]
     */
    fun lastUsernameFlow() = getString(KEY_LAST_USERNAME)


    /**
     * 存储最后登录用户
     * @param username 学号
     */
    fun setLastUsername(username: String) = set(KEY_LAST_USERNAME, username)


    /**
     * 移除最后登录用户名
     */
    fun removeLastUsername() = remove(KEY_LAST_USERNAME)


    /**
     * 最后登录时间 flow
     * @return [Flow]
     */
    fun lastLoginTimeFlow() = getString(KEY_LAST_LOGIN_TIME)


    /**
     * 存储最后登录时间
     * @param date 日期，如 2023-11-27
     */
    fun setLastLoginTime(date: String) = set(KEY_LAST_LOGIN_TIME, date)


    /**
     * 最新教务通知 id flow
     * @return [Flow]
     */
    fun lastNoticeIdFlow() = getString(KEY_LAST_NOTICE_ID)


    /**
     * 存储最新教务通知 id
     * @param id 通知 id
     */
    fun setLastNoticeId(id: String) = set(KEY_LAST_NOTICE_ID, id)


    /**
     * Material You flow
     * @return [Flow]
     */
    fun martialYouFlow() = getBoolean(KEY_MATERIAL_YOU, true)


    /**
     * 存储 Material You
     * @param on 开关状态
     */
    fun setMaterialYou(on: Boolean) = set(KEY_MATERIAL_YOU, on)


    /**
     * 自动登录 flow
     * @return [Flow]
     */
    fun autoLoginFlow() = getBoolean(KEY_AUTO_LOGIN)


    /**
     * 存储自动登录
     * @param on 开关状态
     */
    fun setAutoLogin(on: Boolean) = set(KEY_AUTO_LOGIN, on)


    /**
     * 时间表 flow，0 广州校区  1 佛山校区
     * @return [Flow]
     */
    fun scheduleFlow() = getInt(KEY_SCHEDULE)


    /**
     * 存储时间表
     * @param option 时间表下标，0 广州校区  1 佛山校区
     */
    fun setSchedule(option: Int) = set(KEY_SCHEDULE, option)


    /**
     * 开学日期 flow
     * @return [Flow]
     */
    fun schoolStartFlow() = getNotNullString(KEY_SCHOOL_START, LocalDate.now().toString())



    /**
     * 存储开学日期
     * @param date 日期，如 2023-11-27
     */
    fun setSchoolStart(date: String) = set(KEY_SCHOOL_START, date)


    /**
     * 记住我 Flow
     * @return [Flow]
     */
    fun rememberMeFlow() = getBoolean(KEY_REMEMBER_ME, true)


    /**
     * 存储记住我选项
     * @param on 开关状态
     */
    fun setRememberMe(on: Boolean) = set(KEY_REMEMBER_ME, on)


    /**
     * 登录是否使用 WebVPN Flow
     * @return [Flow]
     */
    fun withWebVPNFlow() = getBoolean(KEY_With_WebVPN)


    /**
     * 存储 WebVPN 开启状态
     * @param on 开关状态
     */
    fun setWithWebVPN(on: Boolean) = set(KEY_With_WebVPN, on)


    /**
     * 跳过验证码 Flow
     * @return [Flow]
     */
    fun skipCaptchaFlow() = getBoolean(KEY_SKIP_CAPTCHA, true)


    /**
     * 存储跳过验证码状态
     * @param on 开关状态
     */
    fun setSkipCaptcha(on: Boolean) = set(KEY_SKIP_CAPTCHA, on)


    /**
     * 当前周次 flow
     * @return [Flow]
     */
    fun currentWeekFlow() = flow {
        schoolStartFlow().collect {
            emit(
                floor(ChronoUnit.DAYS.between(LocalDate.parse(it), Today).toFloat()).toInt() / 7 + 1
            )
        }
    }
}