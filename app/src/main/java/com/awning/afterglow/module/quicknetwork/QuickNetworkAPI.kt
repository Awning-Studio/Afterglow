package com.awning.afterglow.module.quicknetwork

import com.awning.afterglow.module.APILike

object QuickNetworkAPI : APILike {
    override val root = "http://100.64.13.17"

    // 登录
    const val login = ":801/eportal/portal/login"
}