package com.awning.afterglow

import android.content.Context
import android.content.res.AssetManager
import com.google.gson.Gson
import java.time.LocalDate

/**
 * 全局 [Gson]
 */
val Gson = Gson()

/**
 * 全局 [Context]（之间通过函数传参有点小麻烦）
 */
lateinit var ApplicationContext: Context

/**
 * 全局 [AssetManager]
 */
lateinit var AssetManager: AssetManager

/**
 * File 路径
 */
lateinit var FileDir: String
val Today: LocalDate by lazy { LocalDate.now() }


/**
 * 初始化 AfterglowKT
 * @param context
 */
fun initialize(context: Context) {
    ApplicationContext = context.applicationContext
    AssetManager = ApplicationContext.assets
    FileDir = context.filesDir.absolutePath
}