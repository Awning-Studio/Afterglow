package com.awning.afterglow.viewmodel.controller

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import com.awning.afterglow.ApplicationContext
import com.awning.afterglow.Gson
import com.awning.afterglow.store.planDateStore
import com.awning.afterglow.type.Plan
import com.awning.afterglow.viewmodel.RuntimeVM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch


/**
 * 计划控制器
 */
object PlanController {
    /**
     * 计划 flow
     * @return [Flow]
     */
    fun planFlow() = flow<List<Plan>> {
        ApplicationContext.planDateStore.data.collect {
            val plans = arrayListOf<Plan>()
            it.asMap().forEach { (_, value) ->
                plans.add(Gson.fromJson(value.toString(), Plan::class.java))
            }
            emit(plans)
        }
    }


    /**
     * 保存计划
     * @param plan 计划
     */
    fun set(plan: Plan) {
        RuntimeVM.viewModelScope.launch {
            ApplicationContext.planDateStore.edit {
                it[stringPreferencesKey(plan.id)] = Gson.toJson(plan)
            }
        }
    }


    /**
     * 移除计划
     * @param plan 计划
     */
    fun remove(plan: Plan) {
        RuntimeVM.viewModelScope.launch {
            ApplicationContext.planDateStore.edit {
                it.remove(stringPreferencesKey(plan.id))
            }
        }
    }
}