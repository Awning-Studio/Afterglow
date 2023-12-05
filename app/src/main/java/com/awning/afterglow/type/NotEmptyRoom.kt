package com.awning.afterglow.type

/**
 * 非空教室
 * @property name 地点名
 * @property list 非空教室号
 */
data class NotEmptyRoom(
    val name: String,
    val list: List<ArrayList<Int>>
)