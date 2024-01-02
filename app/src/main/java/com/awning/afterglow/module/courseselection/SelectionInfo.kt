package com.awning.afterglow.module.courseselection

/**
 * 选课信息
 * @property name 选课名称（会记录轮次）
 * @property startTime 开始时间
 * @property endTime 结束时间
 * @constructor
 */
data class SelectionInfo(val name: String, val startTime: String, val endTime: String)
