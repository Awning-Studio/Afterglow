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

fun EduSystem.evaluateTeaching(teachingEvaluationItem: TeachingEvaluationItem, negative: Boolean = false) = flow {
    val url = if (withWebVPN) WebVpnAPI.provideEduSystem(teachingEvaluationItem.url, 0)
    else EduSystemAPI.root + teachingEvaluationItem.url

    user.session.get(url).collect {
        EvaluateTeachingParser(withWebVPN, user.session, negative).parse(it).collect(this@flow)
    }
}

private class EvaluateTeachingParser(private val withWebVPN: Boolean, private val session: Waterfall.Session, private val negative: Boolean): HtmlParser<Unit> {
    override fun parse(httpResponse: HttpResponse): Flow<Unit> = flow {
        val table = Jsoup.parse(httpResponse.text).getElementById("Form1")!!

        val form = arrayListOf<Pair<String, String>>()
        val children = table.children()
        var index = 0
        while (index < children.size - 2) {
            val key = children[index].attr("name")
            val value = if (key == "issubmit") "1" else children[index].attr("value")
            form.add(Pair(key, value))
            index ++
        }

        val items = table.getElementById("table1")!!.getElementsByTag("tr")
        val choice = if (negative) 3 else 1

        for (i in 1 until items.size) {
            val itemInfo = items[i].children()

            if (itemInfo.size == 2) {
                val first = itemInfo[0].child(0)
                form.add(Pair(first.attr("name"), first.attr("value")))

                val ops = itemInfo[1].children()
                for (j in 1 until ops.size step 2) {
                    if (j == choice) {
                        form.add(Pair(ops[j - 1].attr("name"), ops[j - 1].attr("value")))
                    }

                    form.add(Pair(ops[j].attr("name"), ops[j].attr("value")))
                }
            }
        }

        val url = if (withWebVPN) WebVpnAPI.provideEduSystem(EduSystemAPI.evaluateTeaching, 0)
        else EduSystemAPI.root + EduSystemAPI.evaluateTeaching

        session.post(url, form = form).collect {
            emit(Unit)
        }
    }
}