package com.awning.afterglow.store

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

// DataStore

// 教务用户
val Context.userDataStore by preferencesDataStore("User")
// 校园网用户
val Context.networkUserDataStore by preferencesDataStore("NetworkUser")
// 计划
val Context.planDateStore by preferencesDataStore("Plan")
// 设置
val Context.settingDataStore by preferencesDataStore("Setting")
// 课表
val Context.timetableDataStore by preferencesDataStore("Timetable")
// 全校课表
val Context.timetableAllDataStore by preferencesDataStore("TimetableAll")
// 考试安排
val Context.examPlanDataStore by preferencesDataStore("ExamPlan")
// 课程成绩
val Context.schoolReportDataStore by preferencesDataStore("SchoolReport")
// 等级考试成绩
val Context.levelReportDataStore by preferencesDataStore("LevelReport")
// 第二课堂成绩
val Context.secondClassDataStore by preferencesDataStore("SecondClass")