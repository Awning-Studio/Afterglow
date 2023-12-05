package com.awning.afterglow.module.edusystem.api

import com.awning.afterglow.module.edusystem.EduSystem
import com.awning.afterglow.module.edusystem.EduSystemAPI
import com.awning.afterglow.module.edusystem.TitleMatcher
import com.awning.afterglow.module.webvpn.WebVpnAPI
import com.awning.afterglow.request.HtmlParser
import com.awning.afterglow.request.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 校历开始日期
 * @receiver [EduSystem]
 * @return [Flow]
 */
fun EduSystem.getCalendar() = flow {
    val params = mapOf(Pair("Ves632DSdyV", "NEW_XSD_WDZM"))

    val url =
        if (withWebVPN) WebVpnAPI.provideEduSystem(EduSystemAPI.calendar, 0)
        else EduSystemAPI.root + EduSystemAPI.calendar

    user.session.get(url, params = params).collect {
        CalendarParser.parse(it).collect(this@flow)
    }
}


/**
 * 校历开始日期解析器
 */
private object CalendarParser : HtmlParser<LocalDate> {
    override fun parse(httpResponse: HttpResponse): Flow<LocalDate> = flow {
        val document = Jsoup.parse(httpResponse.text)

        TitleMatcher.match(document, TitleMatcher.Title.CALENDAR).collect {
            val table = document.getElementById("kbtable")!!

            val items = table.getElementsByTag("tr")
            val dateString = items[1].getElementsByTag("td")[2].attr("title")
            val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd")
            emit(LocalDate.parse(dateString, formatter))
        }
    }
}