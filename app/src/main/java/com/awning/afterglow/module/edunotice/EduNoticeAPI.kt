package com.awning.afterglow.module.edunotice

import com.awning.afterglow.module.APILike

object EduNoticeAPI: APILike {
    override val root = "https://jwc.gdufe.edu.cn"

    // 通知
    fun notice(pageIndex: Int) = "/4133/list${if (pageIndex != 0) pageIndex + 1 else ""}.htm"
}