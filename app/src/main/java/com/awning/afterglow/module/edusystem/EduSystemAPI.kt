package com.awning.afterglow.module.edusystem

import com.awning.afterglow.module.APILike

object EduSystemAPI : APILike {
    override val root = "http://jwxt.gdufe.edu.cn"

    // 登录页
    const val base = "/jsxsd"

    // 验证码
    const val captcha = "$base/verifycode.servlet"

    // 登录
    const val login = "$base/xk/LoginToXkLdap"

    // 课表
    const val timetable = "$base/xskb/xskb_list.do"

    // 考试安排
    const val examPlan = "$base/xsks/xsksap_list"

    // 校历
    const val calendar = "$base/jxzl/jxzl_query"

    // 课程安排
    const val schoolReport = "$base/kscj/cjcx_list"

    // 等级考试成绩
    const val levelReport = "$base/kscj/djkscj_list"

    // 教师信息列表
    const val teacherInfoList = "$base/jsxx/jsxx_list"

    // 教师信息详情
    const val teacherInfoDetail = "$base/jsxx/jsxx_query_detail"

    // 全校课表
    const val timetableAll = "$base/kbcx/kbxx_kc_ifr"

    // 评教
    const val teachingEvaluationList = "$base/xspj/xspj_find.do"

    // 评教提交
    const val evaluateTeaching = "$base/xspj/xspj_save.do"
}