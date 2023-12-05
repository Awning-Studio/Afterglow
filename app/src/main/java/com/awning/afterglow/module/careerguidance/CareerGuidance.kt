package com.awning.afterglow.module.careerguidance

import com.awning.afterglow.module.AreaMapper
import com.awning.afterglow.request.HtmlParser
import com.awning.afterglow.request.HttpResponse
import com.awning.afterglow.request.fixURL
import com.awning.afterglow.request.waterfall.Waterfall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import java.time.LocalDate
import java.util.Date

/**
 * 就业指导
 */
object CareerGuidance {
    private val httpRequest = Waterfall

    /**
     * 获取招聘会信息
     * @param isOuter 是否为校外
     * @return [Flow]
     */
    fun getPresentationBasic(isOuter: Boolean = false) = flow {
        val params = mapOf(
            Pair("start_page", "1"),
            Pair("k", ""),
            Pair("panel_name", ""),
            Pair("type", if (isOuter) "outer" else "inner"),
            Pair("day", ""),
            Pair("panel_id", ""),
            Pair("professionals", ""),
            Pair("work_city", ""),
            Pair("is_yun_career", ""),
            Pair("count", "15"),
            Pair("start", "1"),
            Pair("_", Date().time.toString())
        )

        httpRequest.get(
            CareerGuidanceAPI.root + CareerGuidanceAPI.presentation,
            params
        ).collect {
            PresentationBasicParser.parse(it).collect(this@flow)
        }
    }
}


/**
 * 招聘会信息解析器
 */
private object PresentationBasicParser : HtmlParser<List<PresentationBasic>> {
    override fun parse(httpResponse: HttpResponse): Flow<List<PresentationBasic>> = flow {
        val json = JSONObject(httpResponse.text)
        val data = json.getJSONArray("data")

        val list = arrayListOf<PresentationBasic>()
        for (index in 0 until data.length()) {
            val item = data.getJSONObject(index)

            if (item.getInt("overdue") == 1) {
                val date = item.getString("meet_day")
                list.add(
                    PresentationBasic(
                        item.getString("company_name"),
                        fixURL("http:", item.getString("logo")),
                        item.getString("company_property"),
                        item.getString("industry_category"),
                        item.getString("professionals"),
                        item.getString("job_city"),
                        item.getString("meet_name"),
                        AreaMapper.mapArea(item.getString("school_name") + item.getString("address")),
                        date + " " + item.getString("meet_time"),
                        LocalDate.parse(date).dayOfWeek.value
                    )
                )
            }
        }

        emit(list)
    }
}