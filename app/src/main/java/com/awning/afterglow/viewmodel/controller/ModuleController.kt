package com.awning.afterglow.viewmodel.controller

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import com.awning.afterglow.ApplicationContext
import com.awning.afterglow.Gson
import com.awning.afterglow.module.Snapshot
import com.awning.afterglow.module.edusystem.api.ExamPlan
import com.awning.afterglow.module.edusystem.api.LevelReport
import com.awning.afterglow.module.edusystem.api.SchoolReport
import com.awning.afterglow.module.edusystem.api.Timetable
import com.awning.afterglow.module.secondclass.SecondClass
import com.awning.afterglow.store.examPlanDataStore
import com.awning.afterglow.store.levelReportDataStore
import com.awning.afterglow.store.schoolReportDataStore
import com.awning.afterglow.store.secondClassDataStore
import com.awning.afterglow.store.timetableDataStore
import com.awning.afterglow.viewmodel.RuntimeVM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * 模组功能控制器
 */
object ModuleController {
    /**
     * 本地获取数据并还原为数据类
     * @param key 键
     * @param default 默认值
     * @param dataStore DateStore
     * @return [Flow]
     */
    private inline fun <reified T : Snapshot> get(
        key: String?,
        default: T?,
        dataStore: DataStore<Preferences>
    ) = if (key == null) {
        flow { emit(null) }
    } else {
        dataStore.data.map {
            it[stringPreferencesKey(key)]
        }.map { value ->
            value?.let { Gson.fromJson(value, T::class.java) } ?: default
        }
    }


    /**
     * 本地存储数据
     * @param value 值
     * @param dataStore DataStore
     */
    private fun <T : Snapshot> set(value: T, dataStore: DataStore<Preferences>) {
        RuntimeVM.viewModelScope.launch {
            dataStore.edit {
                it[stringPreferencesKey(value.id)] = Gson.toJson(value)
            }
        }
    }


    /**
     * 课表 flow
     * @param username 学号
     * @return [Flow]
     */
    fun timetableFlow(username: String?) =
        get<Timetable>(Timetable.getNowId(username), null, ApplicationContext.timetableDataStore)


    /**
     * 存储课表
     * @param timetable 课表
     */
    fun setTimetable(timetable: Timetable) = set(timetable, ApplicationContext.timetableDataStore)


    /**
     * 考试安排 flow
     * @param username 学号
     * @return [Flow]
     */
    fun examPlanFlow(username: String?) =
        get<ExamPlan>(username, null, ApplicationContext.examPlanDataStore)


    /**
     * 存储考试安排
     * @param examPlan 考试安排
     */
    fun setExamPlan(examPlan: ExamPlan) = set(examPlan, ApplicationContext.examPlanDataStore)


    /**
     * 课程成绩 flow
     * @param username 学号
     * @return [Flow]
     */
    fun schoolReportFlow(username: String?) =
        get<SchoolReport>(username, null, ApplicationContext.schoolReportDataStore)


    /**
     * 存储课程成绩
     * @param schoolReport 课程成绩
     */
    fun setSchoolReport(schoolReport: SchoolReport) =
        set(schoolReport, ApplicationContext.schoolReportDataStore)


    /**
     * 等级考试成绩 flow
     * @param username 学号
     * @return [Flow]
     */
    fun levelReportFlow(username: String?) =
        get<LevelReport>(username, null, ApplicationContext.levelReportDataStore)


    /**
     * 存储等级考试成绩
     * @param levelReport 等级考试成绩
     */
    fun setLevelReport(levelReport: LevelReport) =
        set(levelReport, ApplicationContext.levelReportDataStore)


    /**
     * 第二课堂成绩 flow
     * @param username 学号
     * @return [Flow]
     */
    fun secondClassFlow(username: String?) =
        get<SecondClass>(username, null, ApplicationContext.secondClassDataStore)


    /**
     * 设置第二课堂成绩
     * @param secondClass 第二课堂成绩
     */
    fun setSecondClass(secondClass: SecondClass) =
        set(secondClass, ApplicationContext.secondClassDataStore)
}