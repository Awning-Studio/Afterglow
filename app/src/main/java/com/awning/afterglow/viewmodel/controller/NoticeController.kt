package com.awning.afterglow.viewmodel.controller

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.awning.afterglow.module.edunotice.EduNotice
import com.awning.afterglow.module.edunotice.NoticeInfo
import com.awning.afterglow.viewmodel.RuntimeVM
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


/**
 * 教务通知控制器（兼 ViewModel）
 */
object NoticeController {
    // 默认请求两页数据
    private const val pageCount = 2
    private var _newNoticeCount by mutableIntStateOf(0)

    /**
     * 新通知数量
     */
    val newNoticeCount by derivedStateOf { _newNoticeCount }

    /**
     * 通知列表
     */
    val notices = mutableStateListOf<NoticeInfo>()

    init {
        getNotice()
    }


    /**
     * 更新最新通知 id
     */
    fun updateLastNoticeId() {
        SettingController.setLastNoticeId(notices[0].id)
        _newNoticeCount = 0
    }


    /**
     * 获取通知列表
     */
    private fun getNotice() {
        notices.clear()
        _newNoticeCount = 0
        RuntimeVM.viewModelScope.launch {
            SettingController.lastNoticeIdFlow().catch { }.collect { id ->
                var isNew = true

                // 递归获取
                suspend fun recursion(page: Int = 0) {
                    if (page < pageCount) {
                        EduNotice.getList(page).collect { list ->
                            // 确认新通知，以及计数
                            for (index in list.indices) {
                                if (list[index].id == id) {
                                    isNew = false
                                }
                                notices.add(list[index].also { it.isNew = isNew })
                                if (isNew) _newNoticeCount++
                            }
                            recursion(page + 1)
                        }
                    }
                }

                recursion()
            }
        }
    }
}