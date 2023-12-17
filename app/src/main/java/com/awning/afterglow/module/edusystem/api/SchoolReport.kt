package com.awning.afterglow.module.edusystem.api

import com.awning.afterglow.module.Snapshot
import com.awning.afterglow.module.edusystem.EduSystem
import com.awning.afterglow.module.edusystem.EduSystemAPI
import com.awning.afterglow.module.edusystem.TitleMatcher
import com.awning.afterglow.module.webvpn.WebVpnAPI
import com.awning.afterglow.request.HtmlParser
import com.awning.afterglow.request.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup


/**
 * 获取课程成绩
 * @receiver [EduSystem]
 * @return [Flow]
 */
fun EduSystem.getSchoolReport() = flow {
    val form = listOf(
        Pair("kksj", ""),
        Pair("kcxz", ""),
        Pair("kcmc", ""),
        Pair("fxkc", "0"),
        Pair("xsfs", "all")
    )

    val url =
        if (withWebVPN) WebVpnAPI.provideEduSystem(EduSystemAPI.schoolReport, 0)
        else EduSystemAPI.root + EduSystemAPI.schoolReport

    user.session.post(url, form = form, timeout = 15000).collect {
        SchoolReportParser(user.username).parse(it).collect(this@flow)
    }
}


/**
 * 课程成绩
 * @property id 学号
 * @property evaluation 系统生成评价
 * @property list 成绩项
 */
data class SchoolReport(
    override val id: String,
    val evaluation: String,
    val list: List<SchoolReportItem>
) : Snapshot()


/**
 * 课程成绩项
 * @property semester 学期
 * @property courseId 课程编号
 * @property name 课程名
 * @property usualScore 平时成绩
 * @property experimentScore 实验成绩
 * @property examScore 考试成绩
 * @property calculatedScore 总成绩
 * @property credit 学分
 * @property classHours 学时
 * @property examMode 考核方式
 * @property type 课程属性
 * @property sort 课程性质
 * @property examType 考试性质
 */
data class SchoolReportItem(
    val semester: String,
    val courseId: String,
    val name: String,
    val usualScore: String,
    val experimentScore: String,
    val examScore: String,
    val calculatedScore: String,
    val credit: String,
    val classHours: String,
    val examMode: String,
    val type: String,
    val sort: String,
    val examType: String
)


/**
 * 课程成绩解析器
 * @property username 学号
 */
private class SchoolReportParser(val username: String) : HtmlParser<SchoolReport> {
    override fun parse(httpResponse: HttpResponse) = flow {
        val document = Jsoup.parse(httpResponse.text)

        TitleMatcher.match(document, TitleMatcher.Title.SCHOOL_REPORT).collect {
            val table = document.getElementById("dataList")!!
            val items = table.getElementsByTag("tr")

            val list = arrayListOf<SchoolReportItem>()

            // 反向遍历保障最新的在上方
            for (index in items.size - 1 downTo 1) {
                val info = items[index].getElementsByTag("td")
                list.add(
                    SchoolReportItem(
                        info[1].text(),
                        info[2].text(),
                        info[3].text(),
                        info[4].text(),
                        info[5].text(),
                        info[6].text(),
                        info[7].text(),
                        info[8].text(),
                        info[9].text(),
                        info[10].text(),
                        info[11].text(),
                        info[12].text(),
                        info[14].text()
                    )
                )
            }

            val evaluation = try {
                val info = httpResponse.text.split(Regex("<br\\s*/>"))
                Jsoup.parse(info[3]).text() + "\n" + Jsoup.parse(info[4].split("<table")[0]).text()
            } catch (_: Exception) {
                ""
            }

            emit(
                SchoolReport(
                    username,
                    evaluation,
                    list
                )
            )
        }
    }
}