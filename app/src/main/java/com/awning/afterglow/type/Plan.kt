package com.awning.afterglow.type

import java.util.Date


/**
 * 计划
 * @property name 名称
 * @property desc 藐视
 * @property deadline 截至日期
 * @property isDone 是否已经完成
 * @property id 标识，时间戳
 * @property cycle 周期
 */
data class Plan(
    val name: String,
    val desc: String,
    val deadline: String,
    val isDone: Boolean = false,
    val id: String = Date().time.toString(),
    val cycle: Int = 0
)