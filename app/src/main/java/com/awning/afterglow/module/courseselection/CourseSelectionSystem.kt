package com.awning.afterglow.module.courseselection

import androidx.compose.runtime.mutableStateOf
import com.awning.afterglow.module.edusystem.EduSystem
import com.awning.afterglow.module.edusystem.TitleMatcher
import com.awning.afterglow.type.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.Date

/**
 * 选课系统
 */
class CourseSelectionSystem(private val user: User) {
    companion object {
        /**
         * 进入选课系统
         * @param eduSystem
         * @return [Flow]
         */
        fun enter(eduSystem: EduSystem): Flow<Pair<CourseSelectionSystem, SelectionInfo>> = flow {
            eduSystem.user.session.get(
                CourseSelectionAPI.root + CourseSelectionAPI.entry, mapOf(
                    Pair("Ves632DSdyV", "NEW_XSD_PYGL")
                )
            ).collect { response ->
                val document = Jsoup.parse(response.text)

                TitleMatcher.match(document, TitleMatcher.Title.COURSE_SELECTION).catch {
                    // 标题不匹配
                    if (it.message != TitleMatcher.MatchMessage.TitleError) {
                        // 遇到登录页面，重登录
                        eduSystem.reLogin().collect {
                            enter(eduSystem).collect(this@flow)
                        }
                    } else throw it
                }.collect {
                    val table = document.getElementById("tbKxkc")!!

                    val rows = table.getElementsByTag("tr")
                    if (rows.size == 1) {
                        throw Exception("选课未开放")
                    } else {
                        val info = rows[1].getElementsByTag("td")
                        eduSystem.user.session.get(
                            CourseSelectionAPI.root + info[6].child(0).attr("href")
                        ).collect {
                            emit(
                                Pair(
                                    CourseSelectionSystem(eduSystem.user),
                                    SelectionInfo(info[1].text(), info[2].text(), info[3].text())
                                )
                            )
                        }
                    }
                }
            }
        }


        private fun generateForm(pageIndex: Int, count: Int = 15): List<Pair<String, String>> {
            return arrayListOf(
                Pair("sEcho", pageIndex.toString()),
                Pair("iColumns", "14"),
                Pair("sColumns", ""),
                Pair("iDisplayStart", (count * pageIndex).toString()),
                Pair("iDisplayLength", count.toString())
            )
        }

        private val campus = listOf("广州校区", "佛山校区")

        private fun parseCourseList(text: String, courseSort: CourseSort): Pair<Int, List<Course>> {
            val json = JSONObject(text)
            val data = json.getJSONArray("aaData")

            val count = json.getInt("iTotalRecords")
            val courseList = arrayListOf<Course>()
            for (index in 0 until data.length()) {
                val item = data.getJSONObject(index)
                val areaInfo = item.getJSONArray("kkapList")

                val areaInfoList = arrayListOf<AreaInfo>()
                for (areaIndex in 0 until areaInfo.length()) {
                    val areaInfoItem = areaInfo.getJSONObject(areaIndex)
                    areaInfoList.add(
                        AreaInfo(
                            areaInfoItem.getString("jsmc"),
                            areaInfoItem.getString("kkzc"),
                            "${areaInfoItem.getString("skjcmc")}节",
                            areaInfoItem.getInt("xq")
                        )
                    )
                }

                courseList.add(
                    Course(
                        mutableStateOf(item.getInt("sfYx") == 1),
                        item.getString("jx0404id"),
                        courseSort,
                        item.getString("kch"),
                        item.getString("kcmc"),
                        item.getString("skls"),
                        item.getString("xf"),
                        campus[item.getInt("xqid") - 1],
                        item.getString("sksj").replace("<br>", "\n"),
                        item.getString("skdd"),
                        item.getString("ctsm"),
                        try {
                            item.getString("kcsxmc")
                        } catch (_: Exception) {
                            ""
                        },
                        try {
                            item.getString("kcxzmc")
                        } catch (_: Exception) {
                            ""
                        },
                        try {
                            item.getString("khfs")
                        } catch (_: Exception) {
                            ""
                        },
                        areaInfoList,
                        item.getString("dwmc"),
                        item.getString("xxrs"),
                        item.getString("xkrs")
                    )
                )
            }

            return Pair(count, courseList)
        }
    }


    /**
     * 获取必修课、选修课列表
     * @param courseSort 课程分类
     * @param pageIndex 页下标
     * @return [Flow]
     */
    fun getCourseList(courseSort: CourseSort, pageIndex: Int = 0) = flow {
        val url = when (courseSort) {
            CourseSort.Basic, CourseSort.Optional -> CourseSelectionAPI.root + CourseSelectionAPI.courseList(
                courseSort
            )

            else -> throw Exception("出现异常分支${courseSort.name}")
        }
        user.session.post(url, form = generateForm(pageIndex)).collect {
            emit(parseCourseList(it.text, courseSort))
        }
    }


    /**
     * 搜索课程
     * @param courseSort 课程分类
     * @param pageIndex 页下标
     * @param name 课程名
     * @param teacher 教师
     * @param dayOfWeek 周几 1..7
     * @param section 节次，如“1-2-”
     * @return [Flow]
     */
    fun searchCourse(
        courseSort: CourseSort,
        pageIndex: Int = 0,
        name: String = "",
        teacher: String = "",
        dayOfWeek: String = "",
        section: String = ""
    ) = flow {
        val url = when (courseSort) {
            CourseSort.Basic, CourseSort.Optional -> throw Exception("出现异常分支${courseSort.name}")
            else -> CourseSelectionAPI.root + CourseSelectionAPI.courseSearch(
                courseSort,
                name,
                teacher,
                dayOfWeek,
                section
            )
        }
        user.session.post(url, form = generateForm(pageIndex)).collect {
            emit(parseCourseList(it.text, courseSort))
        }
    }


    fun checkCampus(id: String) = flow {
        user.session.get(
            CourseSelectionAPI.root + CourseSelectionAPI.checkCampus,
            params = mapOf(
                Pair("jx0404id", id),
                Pair("_", Date().time.toString())
            )
        ).collect {
            if (JSONObject(it.text).getInt("status") == 0) {
                emit(Unit)
            } else {
                throw Exception("不同校区")
            }
        }
    }


    fun checkHavePass(id: String) = flow {
        user.session.get(
            CourseSelectionAPI.root + CourseSelectionAPI.checkHavePass,
            params = mapOf(
                Pair("jx0404id", id),
                Pair("_", Date().time.toString())
            )
        ).collect {
            if (JSONObject(it.text).getInt("status") == 0) {
                emit(Unit)
            } else {
                throw Exception("已经修读过了")
            }
        }
    }


    fun select(id: String, courseSort: CourseSort) = flow {
        user.session.get(
            CourseSelectionAPI.root + CourseSelectionAPI.select(id, courseSort)
        ).collect {
            val data = JSONObject(it.text)
            if (data.getBoolean("success")) {
                emit(Unit)
            } else {
                throw Exception(data.getString("message"))
            }
        }
    }
}