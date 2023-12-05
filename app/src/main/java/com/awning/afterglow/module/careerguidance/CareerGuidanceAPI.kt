package com.awning.afterglow.module.careerguidance

import com.awning.afterglow.module.APILike

object CareerGuidanceAPI: APILike {
    override val root = "http://gdcj.bibibi.net"

    // 招聘会
    const val presentation = "/module/getcareers"
}