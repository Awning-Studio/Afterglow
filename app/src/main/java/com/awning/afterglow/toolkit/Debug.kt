package com.awning.afterglow.toolkit

import android.util.Log

private const val TAG = "Console"

/**
 * 生成格式化日志信息
 * @param source 来源
 * @param messages
 * @return [String]
 */
private fun toLog(source: String, vararg messages: Any?): String {

    var log = ""
    messages.forEach {
        log += it.toString() + " "
    }
    log += "\n来自于: $source"
    return log
}

/**
 * 调用 [Log.d]，[TAG] 为 Console
 * @param messages
 */
fun log(vararg messages: Any?) {
    val source = Thread.currentThread().stackTrace[3]
    Log.d(TAG, toLog("${source.className}.${source.methodName}", *messages))
}


/**
 * 调用 [Log.i]，[TAG] 为 Console
 * @param messages
 */
fun info(vararg messages: Any?) {
    val source = Thread.currentThread().stackTrace[3]
    Log.i(TAG, toLog("${source.className}.${source.methodName}", *messages))
}


/**
 * 调用 [Log.w]，[TAG] 为 Console
 * @param messages
 */
fun warn(vararg messages: Any?) {
    val source = Thread.currentThread().stackTrace[3]
    Log.w(TAG, toLog("${source.className}.${source.methodName}", *messages))
}


/**
 * 调用 [Log.e]，[TAG] 为 Console
 * @param messages
 */
fun err(vararg messages: Any?) {
    val source = Thread.currentThread().stackTrace[3]
    Log.e(TAG, toLog("${source.className}.${source.methodName}", *messages))
}