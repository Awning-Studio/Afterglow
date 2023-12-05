package com.awning.afterglow.type

import com.awning.afterglow.request.waterfall.Waterfall


/**
 * 用户
 * @property username 学号
 * @property password 密码
 * @property secondClassPwd 第二课堂密码
 * @property session 会话
 */
data class User(
    val username: String,
    val password: String,
    val secondClassPwd: String,
    val session: Waterfall.Session
)