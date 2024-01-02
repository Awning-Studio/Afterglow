package com.awning.afterglow.module.courseselection

import androidx.compose.runtime.MutableState

data class Course(
    var isSelected: MutableState<Boolean>,
    val id: String,
val courseSort: CourseSort,
    val courseId: String,
    val name: String,
    val teacher: String,
    val point: String,   // 学分 xf
    val campus: String,  // 校区
    val time: String,
    val area: String,
    val conflict: String,  // 冲突信息
    val type: String,     // 课程属性
    val sort: String,     // 课程性质
    val examMode: String,
    val areaInfoList: List<AreaInfo>,
    val department: String,   // 学院 dwmc
    val providedRoom: String,     // 课堂人数 xxrs
    val selectedCount: String,    // 选课人数 xkrs
)


data class AreaInfo(
    val name: String,
    val rawWeek: String,
    val section: String,
    val dayOfWeek: Int
)