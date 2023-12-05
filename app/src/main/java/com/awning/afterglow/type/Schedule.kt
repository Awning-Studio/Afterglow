package com.awning.afterglow.type

/**
 * 时间表
 * @property area 地区
 * @property list 时间项
 */
data class Schedule(
    val area: String,
    val list: List<List<String>>
)


/**
 * 两校区时间表
 */
val schoolSchedules = arrayListOf(
    Schedule(
        "广州校区",
        listOf(
            listOf("08:00", "08:45", "08:55", "09:40"),
            listOf("10:00", "10:45", "10:55", "11:40"),
            listOf("14:10", "14:55", "15:05", "15:50"),
            listOf("16:10", "16:55", "17:05", "17:50"),
            listOf("18:40", "19:25", "19:35", "20:20"),
            listOf("20:30", "21:15", "21:25", "22:10")
        )
    ),
    Schedule(
        "佛山校区",
        listOf(
            listOf("08:30", "09:15", "09:15", "10:00"),
            listOf("10:20", "11:05", "11:05", "11:50"),
            listOf("14:00", "14:45", "14:45", "15:30"),
            listOf("15:50", "16:35", "16:35", "17:20"),
            listOf("18:30", "19:15", "19:15", "20:00"),
            listOf("20:20", "21:05", "21:05", "21:50")
        )
    ),
)