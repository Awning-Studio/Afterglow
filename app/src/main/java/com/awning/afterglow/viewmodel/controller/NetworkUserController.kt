package com.awning.afterglow.viewmodel.controller

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import com.awning.afterglow.ApplicationContext
import com.awning.afterglow.Gson
import com.awning.afterglow.module.quicknetwork.NetworkUser
import com.awning.afterglow.store.networkUserDataStore
import com.awning.afterglow.viewmodel.RuntimeVM
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch


/**
 * 校园网用户控制器
 */
object NetworkUserController {
    /**
     * 用户 flow
     * @return [Flow]
     */
    fun userFlow() = flow<List<NetworkUser>> {
        ApplicationContext.networkUserDataStore.data.collect {
            val networkUsers = arrayListOf<NetworkUser>()
            it.asMap().forEach { (_, value) ->
                networkUsers.add(Gson.fromJson(value.toString(), NetworkUser::class.java))
            }
            emit(networkUsers)
        }
    }


    /**
     * 存储用户
     * @param networkUser 校园网用户
     */
    fun set(networkUser: NetworkUser) {
        RuntimeVM.viewModelScope.launch {
            ApplicationContext.networkUserDataStore.edit {
                it[stringPreferencesKey(networkUser.id)] = Gson.toJson(networkUser)
            }
        }
    }


    /**
     * 移除用户
     * @param networkUser 校园网用户
     */
    fun remove(networkUser: NetworkUser) {
        RuntimeVM.viewModelScope.launch {
            ApplicationContext.networkUserDataStore.edit {
                it.remove(stringPreferencesKey(networkUser.id))
            }
        }
    }
}