package com.awning.afterglow.module.careerguidance


/**
 * 宣讲会信息
 * @property company 公司名称
 * @property logo 公司 Logo
 * @property type 公司类型
 * @property sort 行业
 * @property specificRequire 专业要求
 * @property city 工作城市
 * @property meetingName 会议名称
 * @property area 举行地点
 * @property time 举行时间
 * @property dayOfWeek 周几
 */
data class PresentationBasic(
    val company: String,
    val logo: String,
    val type: String,
    val sort: String,
    val specificRequire: String,
    val city: String,
    val meetingName: String,
    val area: String,
    val time: String,
    val dayOfWeek: Int,
)