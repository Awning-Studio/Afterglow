package com.awning.afterglow.module.courseselection

import com.awning.afterglow.module.edusystem.EduSystem
import com.awning.afterglow.module.edusystem.TitleMatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup

/**
 * 选课系统(TODO)
 */
class CourseSelectionSystem {
    companion object {

        fun enter(eduSystem: EduSystem): Flow<String> = flow {
            eduSystem.user.session.get(
                CourseSelectionAPI.root + CourseSelectionAPI.entry,
                mapOf(Pair("Ves632DSdyV", "NEW_XSD_PYGL"))
            ).collect { httpResponse ->
                val document = Jsoup.parse(httpResponse.text)
            }

            eduSystem.user.session.get(
                CourseSelectionAPI.root + CourseSelectionAPI.entry, mapOf(
                    Pair("Ves632DSdyV", "NEW_XSD_PYGL")
                )
            ).collect { response ->
                val document = Jsoup.parse(response.text)

                TitleMatcher.match(document, TitleMatcher.Title.COURSE_SELECTION).catch {
                    if (it.message != TitleMatcher.MatchMessage.TitleError) {
                        // 遇到登录页面，重登录
                        eduSystem.reLogin().collect {
                            enter(eduSystem).collect(this@flow)
                        }
                    } else throw it
                }.collect {
                    val table = document.getElementById("tbKxkc")!!

                    val tags = table.getElementsByTag("a")

                    for (item in tags) {
                        if (item.text() == "进入选课") {
                            eduSystem.user.session.get(CourseSelectionAPI.root + tags.attr("href"))
                                .collect {
                                    emit("成功进入选课")
                                    TODO("查看请求到的数据是什么")
                                }
                        }
                    }
                    throw Exception("未开放选课")
                }
            }
        }


        private val form = HashMap<String, String>().also {
            it["sEcho"] = "1"
            it["iColumns"] = "14"
            it["sColumns"] = ""
            it["iDisplayStart"] = "0"
            it["iDisplayLength"] = "15"
            it["mDataProp_0"] = "kch"
            it["mDataProp_1"] = "kcmc"
            it["mDataProp_2"] = "xf"
            it["mDataProp_3"] = "skls"
            it["mDataProp_4"] = "xqid"
            it["mDataProp_5"] = "sksj"
            it["mDataProp_6"] = "skdd"
            it["mDataProp_7"] = "xxrs"
            it["mDataProp_8"] = "xkrs"
            it["mDataProp_9"] = "czrs"
            it["mDataProp_10"] = "syrs"
            it["mDataProp_11"] = "bj"
            it["mDataProp_12"] = "ctsm"
            it["mDataProp_13"] = "czOper"
        }
    }
}