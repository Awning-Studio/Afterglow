package com.awning.afterglow.module.quicknetwork

import java.util.Date

/**
 * 用户名
 * @property name 非空的自定义标识，不唯一
 * @property username 学号
 * @property password 密码
 * @property ip IP
 */
data class NetworkUser(
    var name: String,
    var username: String,
    var password: String,
    var ip: String
) {
    /**
     * 唯一标识，取自时间戳
     */
    val id = Date().time.toString()
}