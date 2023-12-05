package com.awning.afterglow.module.edusystem

object EduSystemUtil {
    /**
     * 验证学号是否正确
     * @param username 学号
     * @return 如果学号正确，则返回 null，否则返回一个错误信息
     */
    fun verifyUsername(username: String): String? {
        return if (Regex("^\\d{11}$").matches(username)) {
            null
        } else if (username.length != 11) {
            // 学号长度出错
            "账号长度为 11 位"
        } else {
            // 学号包含非数字字符
            "账号仅包含数字"
        }
    }


    /**
     * 检查验证码内容格式是否正确
     * @param captcha 验证码内容
     * @return 如果 [captcha] 格式正确，将返回 true
     */
    fun checkCaptcha(captcha: String) =
        Regex("[A-Za-z0-9]{4}").matches(captcha)


    /**
     * 解析周次
     * @param weekString 待解析周次字符
     * @return 解析后的周次数组
     */
    fun parseWeek(weekString: String): List<Int> {
        val weeks = arrayListOf<Int>()

        var firstNumberRecord = ""
        var secondNumberRecord = ""
        var isRecordSecond = false

        for (index in weekString.indices) {
            val item = weekString[index]

            // 累积数字字符
            if (item.isDigit()) {
                if (isRecordSecond) {
                    secondNumberRecord += item
                } else {
                    firstNumberRecord += item
                }
                continue
            }

            when (item) {
                '-' -> {
                    // 如 “1-16周”
                    isRecordSecond = true
                }

                ',', '，' -> {
                    // 结算
                    if (isRecordSecond) {
                        // 结算 -
                        for (i in firstNumberRecord.toInt()..secondNumberRecord.toInt()) {
                            weeks.add(i)
                        }

                        isRecordSecond = false
                        secondNumberRecord = ""
                    } else {
                        weeks.add(firstNumberRecord.toInt())
                    }

                    firstNumberRecord = ""
                }

                else -> {
                    // 结算
                    if (isRecordSecond) {
                        // 结算 -

                        // 适配全校课表
                        val markIndex = if (weekString[index] == '(') index + 1 else index

                        when (weekString[markIndex]) {
                            '周' -> {
                                for (i in firstNumberRecord.toInt()..secondNumberRecord.toInt()) {
                                    weeks.add(i)
                                }
                                break
                            }

                            '单' -> {
                                for (i in firstNumberRecord.toInt()..secondNumberRecord.toInt()) {
                                    if (i % 2 != 0) {
                                        weeks.add(i)
                                    }
                                }
                                break
                            }

                            '双' -> {
                                for (i in firstNumberRecord.toInt()..secondNumberRecord.toInt()) {
                                    if (i % 2 == 0) {
                                        weeks.add(i)
                                    }
                                }
                                break
                            }

                            else -> {
                                for (i in firstNumberRecord.toInt()..secondNumberRecord.toInt()) {
                                    weeks.add(i)
                                }
                            }
                        }

                        isRecordSecond = false
                        firstNumberRecord = ""
                        secondNumberRecord = ""
                    } else {
                        if (firstNumberRecord.isNotBlank()) {
                            weeks.add(firstNumberRecord.toInt())

                            firstNumberRecord = ""
                            secondNumberRecord = ""
                        }
                    }
                }
            }
        }

        return weeks
    }
}