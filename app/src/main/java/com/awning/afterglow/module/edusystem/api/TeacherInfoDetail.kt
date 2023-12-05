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
 * 获取教师信息
 * @receiver [EduSystem]
 * @param id 工号
 * @return [Flow]
 */
fun EduSystem.getTeacherInfoDetail(id: String) = flow {
    val url =
        if (withWebVPN) WebVpnAPI.provideEduSystem(EduSystemAPI.teacherInfoDetail, 0)
        else EduSystemAPI.root + EduSystemAPI.teacherInfoDetail

    user.session.get(
        url,
        mapOf(
            Pair("jg0101id", id)
        )
    ).collect {
        TeacherInfoParser.parse(it).collect(this@flow)
    }
}


/**
 * 教师信息
 * @property name 姓名
 * @property gender 性别
 * @property politics 政治面貌
 * @property nation 民族
 * @property duty 职务
 * @property title 职称
 * @property category 教职工类别
 * @property department 部门（院系）
 * @property office 科室（系）
 * @property qualifications 最高学历
 * @property degree 学位
 * @property field 研究方向
 * @property phoneNumber 手机号
 * @property qQ QQ号
 * @property weChat 微信号
 * @property email 邮箱
 * @property introduction 个人简介
 * @property historyTeaching 近四个学期主讲课程
 * @property futureTeaching 下学期计划开设课程
 * @property philosophy 教学理念
 * @property mostWantToSay 最想对学生说的话
 */
data class TeacherInfoDetail(
    val name: String,
    val gender: String,
    val politics: String,
    val nation: String,
    val duty: String,
    val title: String,
    val category: String,
    val department: String,
    val office: String,
    val qualifications: String,
    val degree: String,
    val field: String,
    val phoneNumber: String,
    val qQ: String,
    val weChat: String,
    val email: String,
    val introduction: String,
    val historyTeaching: ArrayList<TeachingCourse>,
    val futureTeaching: ArrayList<TeachingCourse>,
    val philosophy: String,
    val mostWantToSay: String
)


/**
 * 授课信息
 * @property name 课程名
 * @property sort 课程分类
 * @property time 授课时间
 */
data class TeachingCourse(val name: String, val sort: String, val time: String)


/**
 * 教师信息解析器
 */
private object TeacherInfoParser : HtmlParser<TeacherInfoDetail> {
    override fun parse(httpResponse: HttpResponse) = flow {
        val document = Jsoup.parse(httpResponse.text)

        TitleMatcher.match(document, TitleMatcher.Title.TEACHER_INFO).collect {
            val table = document.getElementsByTag("tbody")[1]
            val items = table.children()

            val hasContact = items.size == 19

            // 姓名、性别
            val nameGender = items[1].getElementsByTag("td")
            // 政治面貌、民族
            val politicsNative = items[2].getElementsByTag("td")
            // 职务、职称
            val dutyTitle = items[3].getElementsByTag("td")
            // 教职工类别、部门（院系）
            val categoryDepartment = items[4].getElementsByTag("td")
            // 科室（系）、最高学历
            val officeQualifications = items[5].getElementsByTag("td")
            // 学位、研究方向
            val degreeField = items[6].getElementsByTag("td")

            // 近四个学期主讲课程
            val historyTeachingItems =
                items[if (hasContact) 12 else 11].getElementsByTag("tbody")[0].getElementsByTag("tr")

            val historyTeaching = arrayListOf<TeachingCourse>()
            for (i in 1 until historyTeachingItems.size) {
                val teachingCourseInfo = historyTeachingItems[i].getElementsByTag("td")
                if (teachingCourseInfo.size == 4) {
                    // 有课程
                    historyTeaching.add(
                        TeachingCourse(
                            teachingCourseInfo[1].text(),
                            teachingCourseInfo[2].text(),
                            teachingCourseInfo[3].text()
                        )
                    )
                } else {
                    break
                }
            }

            // 下学期计划开设课程
            val futureTeachingItems =
                items[if (hasContact) 14 else 13].getElementsByTag("tbody")[0].getElementsByTag("tr")

            val futureTeaching = arrayListOf<TeachingCourse>()
            for (i in 1 until futureTeachingItems.size) {
                val teachingCourseInfo = futureTeachingItems[i].getElementsByTag("td")
                if (teachingCourseInfo.size == 4) {
                    // 有课程
                    futureTeaching.add(
                        TeachingCourse(
                            teachingCourseInfo[1].text(),
                            teachingCourseInfo[2].text(),
                            teachingCourseInfo[3].text()
                        )
                    )
                } else {
                    break
                }
            }

            emit(
                TeacherInfoDetail(
                    nameGender[2].text(),
                    nameGender[4].text(),
                    politicsNative[1].text(),
                    politicsNative[3].text(),
                    dutyTitle[1].text(),
                    dutyTitle[3].text(),
                    categoryDepartment[1].text(),
                    categoryDepartment[3].text(),
                    officeQualifications[1].text(),
                    officeQualifications[3].text(),
                    degreeField[2].text(),
                    degreeField[4].text(),
                    if (hasContact) items[7].getElementsByTag("td")[2].text() else "",
                    if (hasContact) items[7].getElementsByTag("td")[4].text() else "",
                    if (hasContact) items[8].getElementsByTag("td")[2].text() else "",
                    if (hasContact) items[8].getElementsByTag("td")[4].text() else "",
                    items[if (hasContact) 10 else 9].text(),
                    historyTeaching,
                    futureTeaching,
                    items[if (hasContact) 16 else 15].text(),
                    items[if (hasContact) 18 else 17].text()
                )
            )
        }
    }
}