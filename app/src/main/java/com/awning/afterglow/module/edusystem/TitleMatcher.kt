package com.awning.afterglow.module.edusystem

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Document

/**
 * 网页 title 匹配器（仅用于教务系统）
 */
object TitleMatcher {
    /**
     * 可用的目标 title
     */
    object Title {
        const val BASE = "广东财经大学综合教务管理系统-强智科技"
        const val INDEX = "学生个人中心"
        const val TIMETABLE = "学期理论课表"
        const val EXAM_PLAN = "我的考试 - 考试安排查询"
        const val CALENDAR = "教学周历查看"
        const val SCHOOL_REPORT = "学生个人考试成绩"
        const val LEVEL_REPORT = "等级考试成绩"
        const val TEACHER_BASIC_INFO = "教师信息查询"
        const val TEACHER_INFO = "教师信息详情"
        const val COURSE_SELECTION = "学生选课"
    }


    /**
     * 匹配错误信息
     */
    object MatchMessage {
        const val TitleError = "未知标题"
        const val UnknownError = "未知错误"
        const val CaptchaError = "验证码错误"
        const val UserInfoError = "请检查学号或密码"
    }


    /**
     * 鉴别 html 的 title 是否为[target]
     * @param document html
     * @param target 目标 title
     * @return [Flow]
     */
    fun match(document: Document, target: String) = flow {
        when (val title = document.title()) {
            target -> {
                // 匹配成功
                emit(Unit)
            }

            Title.BASE -> {
                // 匹配到了登录页
                val errMsgElements = document.getElementsByTag("font")

                if (errMsgElements.isNotEmpty()) {
                    throw when (val errMsg = errMsgElements[0].text()) {
                        "验证码错误!!" -> Exception(MatchMessage.CaptchaError)
                        "用户名或密码错误" -> Exception(MatchMessage.UserInfoError)
                        else -> Exception(errMsg)
                    }
                } else {
                    throw Exception(MatchMessage.UnknownError)
                }
            }

            else -> {
                // 未知的 title
                throw Exception("${MatchMessage.TitleError}: $title")
            }
        }
    }
}