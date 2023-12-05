package com.awning.afterglow.module

import java.time.LocalDate

/**
 * 存储于本地的一些快照数据
 * @property id 唯一标识
 */
abstract class Snapshot {
    abstract val id: String
    val updateTime: String = LocalDate.now().toString()
}