package com.awning.afterglow.module.secondclass

import com.awning.afterglow.module.Snapshot

/**
 * 第二课堂
 * @property id 学号
 * @property list 第二课堂项
 */
data class SecondClass(
    override val id: String,
    val list: List<SecondClassItem>
) : Snapshot()


/**
 * 第二课堂项
 * @property name 名称
 * @property score 分数
 * @property requiredScore 要求分数
 */
data class SecondClassItem(
    val name: String,
    val score: Double,
    val requiredScore: Double
)