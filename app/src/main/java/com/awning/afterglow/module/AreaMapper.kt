package com.awning.afterglow.module

/**
 * 校园地点匹配
 */
object AreaMapper {
    // 用于生成个性化空教室排列（由设置的校区选择先广州或先佛山）
    val guangzhou = listOf("第一教学楼", "综合楼", "实验楼", "北五", "北四", "第二综合楼", "艺术坊")
    val foshan = listOf("励学楼", "笃行楼", "拓新楼", "同心楼")

    /**
     * 通过字符快速匹配地点
     */
    private val topMap = HashMap<Char, String>().also {
        it['博'] = "博学楼"
        it['勤'] = "勤学楼"
        it['实'] = "实验楼"
        it['致'] = "致学楼"
        it['善'] = "善学楼"
        it['乐'] = "乐学楼"
        it['敏'] = "敏学楼"
        it['励'] = "励学楼"
        it['笃'] = "笃行楼"
        it['拓'] = "拓新楼"
        it['同'] = "同心楼"
        it['艺'] = "艺术坊"
    }


    /**
     * 地点二次匹配
     */
    private val areaMap = HashMap<String, String>().also {
        it["博学楼"] = "第一教学楼"
        it["勤学楼"] = "综合楼"
        it["致学楼"] = "第二综合楼"
        it["善学楼"] = "第二综合楼"
        it["实验楼"] = "实验楼"
        it["乐学楼"] = "北五"
        it["敏学楼"] = "北四"

        // 佛山校区
        it["励学楼"] = "励学楼"
        it["笃行楼"] = "笃行楼"
        it["拓新楼"] = "拓新楼"
        it["同心楼"] = "同心楼"
    }


    /**
     * 匹配地点，包含教室号
     * @param area 原始地点名，含教室号
     * @return [String]
     */
    fun mapArea(area: String): String {
        val formattedArea = formatArea(area)

        return if (formattedArea.second == null) {
            area
        } else {
            "${areaMap[formattedArea.first] ?: formattedArea.first}${formattedArea.second}"
        }
    }


    /**
     * 通过地点名获取地点，不包含教室号，不含教室号
     * @param areaName 原始地点名
     * @return [String]
     */
    fun getMappedAreaName(areaName: String) = areaMap[areaName] ?: areaName


    /**
     * 将地点格式化，返回地点名 + 教室号（如果有）
     * @param area String
     * @return Pair<String, Int?>
     */
    fun formatArea(area: String): Pair<String, Int?> {
        var matchedArea = ""
        var room = ""

        // 遍历取出地点和教室号
        var gotArea = false
        for (char in area) {
            if (char.isDigit()) {
                // 匹配教室号
                room += char
            } else {
                val mappedArea = topMap[char]
                mappedArea?.let {
                    matchedArea = it
                    gotArea = true
                }
                if (room.length == 3) {
                    // 匹配到有教室号的
                    break
                } else {
                    if (!gotArea) {
                        matchedArea += room + char
                    }
                    if (room.isNotBlank()) {
                        room = ""
                    }
                }
            }
        }

        return if (room.length == 3) Pair(matchedArea, room.toInt())
        else Pair(matchedArea, null)
    }
}