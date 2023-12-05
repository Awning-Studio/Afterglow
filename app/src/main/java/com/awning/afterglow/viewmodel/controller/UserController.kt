package com.awning.afterglow.viewmodel.controller

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import com.awning.afterglow.ApplicationContext
import com.awning.afterglow.Gson
import com.awning.afterglow.store.userDataStore
import com.awning.afterglow.type.User
import com.awning.afterglow.viewmodel.RuntimeVM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch


/**
 * 用户控制器
 */
object UserController {
    /**
     * 用户 flow
     * @return [Flow]
     */
    fun userFlow() = flow<List<User>> {
        ApplicationContext.userDataStore.data.collect {
            val users = arrayListOf<User>()
            it.asMap().forEach { (_, value) ->
                users.add(Gson.fromJson(value.toString(), User::class.java))
            }
            emit(users)
        }
    }


    /**
     * 存储用户
     * @param user 用户
     */
    fun set(user: User) {
        RuntimeVM.viewModelScope.launch {
            ApplicationContext.userDataStore.edit {
                it[stringPreferencesKey(user.username)] = Gson.toJson(user)
            }
        }
    }


    /**
     * 移除用户
     * @param user 用户
     */
    fun remove(user: User) {
        RuntimeVM.viewModelScope.launch {
            ApplicationContext.userDataStore.edit {
                it.remove(stringPreferencesKey(user.username))
            }
        }
    }
}