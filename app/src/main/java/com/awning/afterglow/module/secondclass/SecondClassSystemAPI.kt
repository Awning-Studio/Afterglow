package com.awning.afterglow.module.secondclass

import com.awning.afterglow.module.APILike

object SecondClassSystemAPI : APILike {
    override val root = "http://2ketang.gdufe.edu.cn"

    // 登录
    const val login = "/apps/common/login"

    // 成绩
    const val report = "/apps/user/achievement/by-classify-list"
}