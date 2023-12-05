package com.awning.afterglow.module.edusystem.api

import com.awning.afterglow.module.Snapshot
import com.awning.afterglow.module.edusystem.TitleMatcher
import com.awning.afterglow.module.edusystem.EduSystem
import com.awning.afterglow.module.edusystem.EduSystemAPI
import com.awning.afterglow.module.webvpn.WebVpnAPI
import com.awning.afterglow.request.HtmlParser
import com.awning.afterglow.request.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup

/**
 * 获取考试安排
 * @receiver [EduSystem]
 * @return [Flow]
 */
fun EduSystem.getExamPlan() = flow {
    val form = mapOf(
        Pair("xqlbmc", ""),
        Pair("xnxqid", EduSystem.Semester),
        Pair("xqlb", "")
    )

    val url =
        if (withWebVPN) WebVpnAPI.provideEduSystem(EduSystemAPI.examPlan, 0)
        else EduSystemAPI.root + EduSystemAPI.examPlan

    user.session.post(url, form = form).collect {
        ExamPlanParser(user.username).parse(it).collect(this@flow)
    }
}


/**
 * 考试安排
 * @property id 学号
 * @property list 安排项
 */
data class ExamPlan(
    override val id: String,
    val list: List<ExamPlanItem>
) : Snapshot()


/**
 * 考试安排项
 * @property courseId 课程编号
 * @property name 课程名
 * @property time 考试时间
 * @property campus 校区
 * @property area 地点
 * @property id 准考证号
 */
data class ExamPlanItem(
    val courseId: String,
    val name: String,
    val time: String,
    val campus: String,
    val area: String,
    val id: String?
)


/**
 * 考试安排解析器
 * @property username 学号
 */
private class ExamPlanParser(val username: String) : HtmlParser<ExamPlan> {
    override fun parse(httpResponse: HttpResponse) = flow {
        val document = Jsoup.parse(httpResponse.text)

        TitleMatcher.match(document, TitleMatcher.Title.EXAM_PLAN).collect {
            val table = document.getElementById("dataList")!!
            val items = table.getElementsByTag("tr")

            val list = arrayListOf<ExamPlanItem>()
            for (index in 1 until items.size) {
                val info = items[index].getElementsByTag("td")

                list.add(
                    ExamPlanItem(
                        info[1].text(),
                        info[2].text(),
                        info[3].text(),
                        info[4].text(),
                        info[5].text(),
                        info[6].text().ifBlank { null }
                    )
                )
            }

            emit(ExamPlan(username, list))
        }
    }
}