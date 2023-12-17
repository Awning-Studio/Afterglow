package com.awning.afterglow.module.edusystem.api

import com.awning.afterglow.module.edusystem.EduSystem
import com.awning.afterglow.module.edusystem.EduSystemAPI
import com.awning.afterglow.module.webvpn.WebVpnAPI
import com.awning.afterglow.request.HtmlParser
import com.awning.afterglow.request.HttpResponse
import com.awning.afterglow.request.waterfall.Waterfall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup

fun EduSystem.getTeachingEvaluationList() = flow {
    val params = mapOf(
        Pair("Ves632DSdyV", "NEW_XSD_JXPJ")
    )

    val url =
        if (withWebVPN) WebVpnAPI.provideEduSystem(EduSystemAPI.teachingEvaluationList, 0)
        else EduSystemAPI.root + EduSystemAPI.teachingEvaluationList

    user.session.get(url, params).collect {
        TeachingEvaluationListParser(withWebVPN, user.session).parse(it).collect(this@flow)
    }
}

private class TeachingEvaluationListParser(
    private val withWebVPN: Boolean,
    private val session: Waterfall.Session
) : HtmlParser<List<TeachingEvaluationItem>> {
    override fun parse(httpResponse: HttpResponse): Flow<List<TeachingEvaluationItem>> = flow {
        val table = Jsoup.parse(httpResponse.text).getElementsByClass("Nsb_r_list Nsb_table")[0]

        val rows = table.getElementsByTag("tr")
        if (rows.size < 2) {
            emit(emptyList())
        } else {
            val items = rows[1].getElementsByTag("td")[6].getElementsByTag("a")

            suspend fun repeat(index: Int = 0) {
                if (index != items.size) {
                    val sort = items[index].text()

                    val url = if (withWebVPN) WebVpnAPI.root + items[index].attr("href")
                    else EduSystemAPI.root + items[index].attr("href")

                    getList(url, sort).collect {
                        emit(it)
                        repeat(index + 1)
                    }
                }
            }

            repeat()
        }
    }

    private fun getList(url: String, sort: String) = flow {
        session.get(url).collect {
            val infoList = Jsoup.parse(it.text).getElementById("dataList")!!.getElementsByTag("tr")

            val list = arrayListOf<TeachingEvaluationItem>()
            for (index in 1 until infoList.size) {
                val info = infoList[index].getElementsByTag("td")
                val href = info[7].getElementsByTag("a")[0].attr("onclick")

                // 过滤已评
                if (href.isBlank()) continue

                list.add(
                    TeachingEvaluationItem(
                        info[1].text(),
                        info[2].text(),
                        info[3].text(),
                        sort,
                        href.substring(7, href.length - 12)
                    )
                )
            }
            emit(list)
        }
    }
}

data class TeachingEvaluationItem(
    val id: String,
    val name: String,
    val teacher: String,
    val sort: String,
    val url: String
)