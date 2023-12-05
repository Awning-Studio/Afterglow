package com.awning.afterglow.module.courseselection

import com.awning.afterglow.module.APILike
import com.awning.afterglow.module.edusystem.EduSystemAPI

object CourseSelectionAPI: APILike {
    override val root = "http://jwxt.gdufe.edu.cn"

    private const val base = "/jsxsd"

    const val entry = "${EduSystemAPI.base}/xsxk/xklc_list"

    // 选课学分表
    const val credit = "${EduSystemAPI.base}/xsxk/xsxk_tzsm"

    // 选课课表
    const val timetable = "${EduSystemAPI.base}/xsxkjg/xsxkkb"

    // 学科基础课、专业必修课
    const val sortMajor = "${EduSystemAPI.base}/xsxkkc/xsxkBxxk"

    // 专业选修课
    const val sortElective = "${EduSystemAPI.base}/xsxkkc/xsxkXxxk"

    // 通识课
    const val searchGeneral = "${EduSystemAPI.base}/xsxkkc/xsxk/Ggxxkxk"

    // 跨专业课
    const val searchCrossMajor = "${EduSystemAPI.base}/xsxkkc/xsxk/Fawxk"

    // 跨年级
    const val searchCrossYear = "${EduSystemAPI.base}/xsxkkc/xsxk/Knjxk"

    // 专业内计划课
    const val searchPlanInMajor = "${EduSystemAPI.base}/xsxkkc/xsxk/Bxqjhxk"
}