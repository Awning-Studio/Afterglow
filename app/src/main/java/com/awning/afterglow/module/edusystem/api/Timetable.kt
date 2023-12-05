package com.awning.afterglow.module.edusystem.api

import com.awning.afterglow.module.Snapshot
import com.awning.afterglow.module.edusystem.EduSystem
import com.awning.afterglow.module.edusystem.EduSystemAPI
import com.awning.afterglow.module.edusystem.EduSystemUtil
import com.awning.afterglow.module.edusystem.TitleMatcher
import com.awning.afterglow.module.webvpn.WebVpnAPI
import com.awning.afterglow.request.HtmlParser
import com.awning.afterglow.request.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup


/**
 * 获取课表
 * @receiver [EduSystem]
 * @param semester 学期
 * @return [Flow]
 */
fun EduSystem.getTimetable(semester: String = EduSystem.Semester) = flow {
    val url =
        if (withWebVPN) WebVpnAPI.provideEduSystem(EduSystemAPI.timetable, 0)
        else EduSystemAPI.root + EduSystemAPI.timetable

    user.session.get(
        url,
        mapOf(Pair("xnxq01id", semester))
    ).collect {
        TimetableParser(user.username, semester).parse(it).collect(this@flow)
    }
}


/**
 * 课表
 * @property username 学号
 * @property semester 学期
 * @property list 课表项，外层列表数量为 42 个
 * @property notes 课表备注，一般为慕课
 */
data class Timetable(
    val username: String,
    val semester: String,
    val list: List<List<TimetableItem>?>,
    val notes: String
) : Snapshot() {
    override val id = username + semester

    companion object {
        fun getNowId(username: String?): String? {
            return username?.let { username + EduSystem.Semester }
        }
    }
}


/**
 * 课表项
 * @property name 课程名
 * @property teacher 教师
 * @property rawWeeks 原始周次
 * @property weeks 解析后周次
 * @property area 地点
 * @property section 节次
 */
data class TimetableItem(
    val name: String,
    val teacher: String,
    val rawWeeks: String,
    val weeks: List<Int>,
    val area: String,
    val section: String
)


/**
 * 课表解析器
 * @property username 学号
 * @property semester 学期
 */
class TimetableParser(
    private val username: String,
    private val semester: String
) : HtmlParser<Timetable> {
    override fun parse(httpResponse: HttpResponse) = flow {
        val document = Jsoup.parse(httpResponse.text)

        TitleMatcher.match(document, TitleMatcher.Title.TIMETABLE).collect {
            val table = document.getElementById("kbtable")!!
            val items = table.getElementsByTag("td")

            // 替换节次头部 0，如 ”01-02节“
            val regex = Regex("^0")
            val list = arrayListOf<ArrayList<TimetableItem>?>()

            for (index in 0 until items.size - 1) {
                val info = items[index].getElementsByClass("kbcontent")[0]

                // 课程名、节次
                val nameSection = info.textNodes()

                var timetableItems: ArrayList<TimetableItem>? = null

                if (nameSection.size != 1) {
                    // 有课程
                    timetableItems = arrayListOf()
                    val courseDetail = info.getElementsByTag("font")

                    for (i in 0..(nameSection.size - 2) / 3) {
                        val detailIndex = i * 3

                        val rawWeeks = courseDetail[detailIndex + 1].text()
                        val sectionParts =
                            nameSection[detailIndex + 1].text().substring(1, 6).split("-")
                        val session = StringBuilder()
                            .append(sectionParts[0].replace(regex, ""))
                            .append(" - ")
                            .append(sectionParts[1].replace(regex, ""))
                            .append("节")
                            .toString()

                        timetableItems.add(
                            TimetableItem(
                                nameSection[detailIndex].text(),
                                courseDetail[detailIndex].text(),
                                rawWeeks,
                                EduSystemUtil.parseWeek(rawWeeks),
                                courseDetail[detailIndex + 2].text(),
                                session
                            )
                        )
                    }
                }

                list.add(timetableItems)
            }

            emit(Timetable(username, semester, list, items.last()!!.text()))
        }
    }
}