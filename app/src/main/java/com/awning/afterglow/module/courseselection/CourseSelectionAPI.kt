package com.awning.afterglow.module.courseselection

import com.awning.afterglow.module.APILike
import java.net.URLEncoder

object CourseSelectionAPI : APILike {
    override val root = "http://jwxt.gdufe.edu.cn"

    private const val base = "/jsxsd"

    const val entry = "$base/xsxk/xklc_list"

    // 选课学分表
    const val credit = "$base/xsxk/xsxk_tzsm"

    // 选课课表
    const val timetable = "$base/xsxkjg/xsxkkb"

    // 检查校区
    const val checkCampus = "$base/xsxkkc/checkXq"

    // 检查是否已通过
    const val checkHavePass = "$base/xsxkkc/checkXscj"

    /**
     * 必修课、选修课列表
     * @param courseSort 课程分类
     * @return [String]
     */
    fun courseList(courseSort: CourseSort) = "$base/xsxkkc/xsxk${courseSort.search}xk"


    /**
     * 双重编码
     * @param text 待编码文字
     * @return [String]
     */
    private fun encode(text: String) = URLEncoder.encode(URLEncoder.encode(text, "UTF-8"), "UTF-8")


    /**
     * 可搜索课程
     * @param courseSort 课程分类
     * @param name 课程名
     * @param teacher 教师
     * @param dayOfWeek 周几 1..7
     * @param section 节次，如“1-2-”
     * @return [String]
     */
    fun courseSearch(
        courseSort: CourseSort,
        name: String = "",
        teacher: String = "",
        dayOfWeek: String = "",
        section: String = ""
    ) =
        "${base}/xsxkkc/xsxk${courseSort.search}xk?kcxx=${encode(name)}&skls=${encode(teacher)}&skxq=$dayOfWeek&skjc=$section&sfym=false&sfct=false${if (courseSort == CourseSort.General) "&szjylb=&xq=&szkclb=" else ""}"


    /**
     * 选课
     * @param id 课程 Id
     * @param courseSort 课程分类
     * @param priority 一轮抽签时需要填 1..3
     * @return [String]
     */
    fun select(id: String, courseSort: CourseSort, priority: String = "") = "$base/xsxkkc/${courseSort.select}xkOper?jx0404id=$id&xkzy=${priority}&trjf=&cxxdlx=1"


    /**
     * 退课
     * @param id 课程 Id
     * @return [String]
     */
    fun courseDelete(id: String) = "$base/xsxkjg/xstkOper?jx0404id=$id"
}