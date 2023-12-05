package com.awning.afterglow.viewmodel.controller

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import com.awning.afterglow.ApplicationContext
import com.awning.afterglow.Gson
import com.awning.afterglow.module.edusystem.api.TimetableAll
import com.awning.afterglow.module.edusystem.api.getTimetableAll
import com.awning.afterglow.store.timetableAllDataStore
import com.awning.afterglow.viewmodel.RuntimeVM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDate


/**
 * 全校课表控制器
 */
object TimetableAllController {
    private val key = stringPreferencesKey("TimetableAll")
    private val key_update = stringPreferencesKey("UpdateTime")

    private var _isUpdating by mutableStateOf(false)

    /**
     * 是否正在同步
     */
    val isUpdating by derivedStateOf { _isUpdating }


    /**
     * 全校课表 flow
     * @return [Flow]
     */
    fun timetableAllFlow() = flow {
        // 本地获取课表
        ApplicationContext.timetableAllDataStore.data.collect { preferences ->
            emit(preferences[key]?.let { Gson.fromJson(it, TimetableAll::class.java) })
        }
    }


    /**
     * 全校课表更新时间 flow
     * @return [Flow]
     */
    fun updateTimeFlow() = flow {
        ApplicationContext.timetableAllDataStore.data.collect {
            emit(it[key_update])
        }
    }


    /**
     * 获取全校课表
     */
    fun getTimetableAll() {
        RuntimeVM.viewModelScope.launch {
            RuntimeVM.eduSystem?.let { eduSystem ->
                _isUpdating = true
                eduSystem.getTimetableAll().catch { _isUpdating = false }.collect { timetableAll ->
                    _isUpdating = false
                    ApplicationContext.timetableAllDataStore.edit {
                        it[key] = Gson.toJson(timetableAll)
                        it[key_update] = LocalDate.now().toString()
                    }
                }
            }
        }
    }
}