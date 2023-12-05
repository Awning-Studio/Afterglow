package com.awning.afterglow.toolkit

/**
 * 定时触发器，触发后必须在 [millis] 之后才能再次触发
 * @property millis 毫秒，默认为 500 ms
 */
class Trigger(private val millis: Long = 500) {
    private var _isTriggered: Boolean = false

    // 线程锁（对于点触操作其实也可以不用）
    private val mLock = Any()

    /**
     * 执行 [block]，在 [Trigger] 自动复原前不会重复触发
     * @param block
     */
    fun touch(block: () -> Unit) {
        if (!_isTriggered) {
            synchronized(mLock) {
                if (!_isTriggered) {
                    _isTriggered = true
                    block()

                    Thread {
                        Thread.sleep(millis)
                        _isTriggered = false
                    }.start()
                }
            }
        }
    }
}