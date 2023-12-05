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


/**
 * 获取教师信息列表
 * @receiver [EduSystem]
 * @param name 教师名
 * @param pageIndex 页面下标，从 1 开始
 * @param teacherInfoList 初始列表，用于与新列表拼接
 * @return [Flow]
 */
fun EduSystem.getTeacherInfoList(
    name: String,
    pageIndex: Int = 1,
    teacherInfoList: TeacherInfoList? = null
) = flow {
    val form = HashMap<String, String>().also {
        it["jsxm"] = name
        it["ssxy"] = ""
        it["pageIndex"] = if (pageIndex == 1) "" else pageIndex.toString()
    }

    val url =
        if (withWebVPN) WebVpnAPI.provideEduSystem(EduSystemAPI.teacherInfoList, 0)
        else EduSystemAPI.root + EduSystemAPI.teacherInfoList

    user.session.post(url, null, form).collect {
        TeacherInfoListParser(teacherInfoList).parse(it).collect(this@flow)
    }
}


/**
 * 教师信息列表
 * @property currentPage 当前页，从 1 开始
 * @property totalPages 总页数
 * @property list 教师信息列表
 */
data class TeacherInfoList(val currentPage: Int, val totalPages: Int, val list: List<TeacherInfo>)


/**
 * 教师信息
 * @property id 工号
 * @property name 名字
 * @property department 部门
 */
data class TeacherInfo(val id: String, val name: String, val department: String)


/**
 * 教师信息列表解析器
 * @property teacherInfoList 初始列表，用于与新列表拼接
 */
private class TeacherInfoListParser(
    val teacherInfoList: TeacherInfoList?
) : HtmlParser<TeacherInfoList> {
    override fun parse(httpResponse: HttpResponse) = flow {
        val document = Jsoup.parse(httpResponse.text)

        TitleMatcher.match(document, TitleMatcher.Title.TEACHER_BASIC_INFO).collect {
            val table = document.getElementById("Form1")!!
            val items = table.getElementsByTag("tr")

            // 获取页面信息
            val pageInfo = table.getElementById("PagingControl1_divOuterClass")!!
            val currentPage = pageInfo.getElementsByTag("input")[0].attr("value").toInt()
            val totalPages = Regex("\\d+").find(pageInfo.text())!!.value.toInt()

            val list = arrayListOf<TeacherInfo>()

            // 拼接基础列表
            teacherInfoList?.let { list.addAll(it.list) }

            for (index in 1 until items.size) {
                val info = items[index].children()

                list.add(
                    TeacherInfo(
                        info[1].text(),
                        info[2].text(),
                        info[3].text()
                    )
                )
            }

            emit(TeacherInfoList(currentPage, totalPages, list))
        }
    }
}