package com.awning.afterglow.module.edusystem.api

import com.awning.afterglow.module.Snapshot
import com.awning.afterglow.module.edusystem.EduSystem
import com.awning.afterglow.module.edusystem.EduSystemAPI
import com.awning.afterglow.module.edusystem.EduSystemUtil
import com.awning.afterglow.module.webvpn.WebVpnAPI
import com.awning.afterglow.request.HtmlParser
import com.awning.afterglow.request.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.jsoup.Jsoup


/**
 * 全校课表
 * @receiver EduSystem
 * @param semester String
 * @return [Flow]
 */
fun EduSystem.getTimetableAll(semester: String = EduSystem.Semester) = flow {
    val form = mapOf(
        Pair("xnxqh", semester),
        Pair("skyx", ""),                // 上课院系
        Pair("kkyx", ""),                // 开课院系
        Pair("zzdKcSX", ""),
        Pair("kc", ""),                  // 课程名
        Pair("zc1", ""),                // 周次开始
        Pair("zc2", ""),                // 周次结束
        Pair("jc1", ""),                 // 节次开始
        Pair("jc2", ""),                 // 节次结束z
    )

    val url =
        if (withWebVPN) WebVpnAPI.provideEduSystem(EduSystemAPI.timetableAll, 0)
        else EduSystemAPI.root + EduSystemAPI.timetableAll

    user.session.post(url, form = form, timeout = 10000)
        .collect { httpResponse ->
            TimetableAllParser.parse(httpResponse).collect(this@flow)
        }
}


/**
 * 全校课表
 * @property list 课程项
 */
data class TimetableAll(
    val list: List<TimetableAllItem>
) : Snapshot() {
    override val id = "TimetableAll"
}


/**
 * 课表项
 * @property name 课程名称
 * @property list 课程上课信息，一个课程有多个
 */
data class TimetableAllItem(
    val name: String,
    val list: List<CourseInfo>
)


/**
 * 课程信息
 * @property clazz 上课班级
 * @property teacher 教师
 * @property rawWeeks 原始周次
 * @property weeks 解析后周次
 * @property area 地点
 * @property sectionFirst 节次开始，取值为 1, 3, 5, 7, 9, 11
 * @property dayOfWeek 周几，1 ~ 7
 */
data class CourseInfo(
    val clazz: String,
    val teacher: String,
    val rawWeeks: String,
    val weeks: List<Int>,
    val area: String,
    val sectionFirst: Int,
    val dayOfWeek: Int
)


/**
 * 全校课表解析器
 */
private object TimetableAllParser : HtmlParser<TimetableAll> {
    override fun parse(httpResponse: HttpResponse): Flow<TimetableAll> =
        flow {
            val document = Jsoup.parse(httpResponse.text)

            val items = document.getElementById("kbtable")!!.getElementsByTag("tr")

            val timetableAllList = arrayListOf<TimetableAllItem>()
            for (index in 2 until items.size) {
                // 会获取到一项课程，第 0 项包含课程名
                val children = items[index].children()

                val list = arrayListOf<CourseInfo>()
                // 遍历获取课程信息
                for (childIndex in 1 until children.size) {
                    // 一项
                    val infoList = children[childIndex].child(0).children()

                    for (info in infoList) {
                        // 0 班级  1 教师 + 周次  2 地点
                        // 可能有多项
                        val infoTextNodes = info.textNodes()
                        val realIndex = childIndex - 1

                        var teacher = ""
                        var rawWeeks = ""
                        var isRawWeeks = false

                        // 解析教师和周次

                        for (char in infoTextNodes[1].text()) {
                            if (char == '(' || char == '（') {
                                isRawWeeks = true
                                continue
                            } else if (char == ')') {
                                break
                            }
                            if (isRawWeeks) {
                                rawWeeks += char
                            } else {
                                teacher += char
                            }
                        }

                        list.add(
                            CourseInfo(
                                infoTextNodes[0].text(),
                                teacher,
                                rawWeeks,
                                EduSystemUtil.parseWeek(rawWeeks),
                                infoTextNodes[2].text().trim(),
                                realIndex % 6 * 2 + 1,
                                realIndex / 6 + 1
                            )
                        )
                    }
                }

                timetableAllList.add(
                    TimetableAllItem(children[0].text(), list)
                )
            }

            emit(TimetableAll(timetableAllList))
        }.flowOn(Dispatchers.IO)
}