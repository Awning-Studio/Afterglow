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
 * 获取等级考试成绩
 * @receiver [EduSystem]
 * @return [Flow]
 */
fun EduSystem.getLevelReport() = flow {
    val url =
        if (withWebVPN) WebVpnAPI.provideEduSystem(EduSystemAPI.levelReport, 0)
        else EduSystemAPI.root + EduSystemAPI.levelReport

    user.session.get(url).collect {
        LevelReportParser(user.username).parse(it).collect(this@flow)
    }
}


/**
 * 等级考试成绩
 * @property id 学号
 * @property list 等级考试成绩项
 */
data class LevelReport(
    override val id: String,
    val list: List<LevelReportItem>
) : Snapshot()


/**
 * 等级考试成绩项
 * @property name 科目
 * @property time 考试时间
 * @property writtenScore 笔试成绩
 * @property machineScore 机试成绩
 * @property score 总成绩
 * @property writtenLevel 笔试等级
 * @property machineLevel 机试等级
 * @property level 总等级
 */
data class LevelReportItem(
    val name: String,
    val time: String,
    val writtenScore: String,
    val machineScore: String,
    val score: String,
    val writtenLevel: String,
    val machineLevel: String,
    val level: String
)


/**
 * 等级考试成绩解析器
 * @property username 学号
 */
private class LevelReportParser(val username: String) : HtmlParser<LevelReport> {
    override fun parse(httpResponse: HttpResponse) = flow {
        val document = Jsoup.parse(httpResponse.text)

        TitleMatcher.match(document, TitleMatcher.Title.LEVEL_REPORT).collect {
            val table = document.getElementById("dataList")!!
            val items = table.getElementsByTag("tr")

            val list = arrayListOf<LevelReportItem>()
            for (index in 2 until items.size) {
                val info = items[index].children()
                list.add(
                    LevelReportItem(
                        info[1].text(),
                        info[8].text(),
                        info[2].text(),
                        info[3].text(),
                        info[4].text(),
                        info[5].text(),
                        info[6].text(),
                        info[7].text(),
                    )
                )
            }

            emit(LevelReport(username, list))
        }
    }
}