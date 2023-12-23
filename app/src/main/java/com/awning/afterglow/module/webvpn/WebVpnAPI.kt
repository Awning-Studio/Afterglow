package com.awning.afterglow.module.webvpn

import com.awning.afterglow.module.APILike

object WebVpnAPI : APILike {
    override val root = "https://sec.gdufe.edu.cn"

    // Cookie
    const val cookie = "/rump_frontend/login/"

    // 登录状态
    const val state = "/rump_frontend/getHomeParam/"

    // 服务器
    const val service = "/rump_frontend/loginFromCas/"

    // 路由提供
    private fun nav(route: String) = "/webvpn/LjIwMy4yMTUuMTY1LjE2MQ==$route"

    // 登录
    val login =
        nav("/LjE5Ni4yMTYuMTY1LjE1My4xNjMuMTUxLjIxMy4xNjYuMTk4LjE2NC45NC4xNjAuMTUxLjIxOS4xNTUuMTUxLjE0Ny4xNTAuMTQ4LjIxNy4xMDAuMTU2LjE2MQ==/authserver/login")

    // 外接教务系统
    fun provideEduSystem(route: String, vpnIndex: Int) =
        root + nav("/LjIwNS4yMTguMTY5LjE2NS45NC4xNTMuMTk5LjE2NS4xOTkuMTUxLjk0LjE1OC4xNTEuMjE5Ljk5LjE0OS4yMTE=$route?vpn-${vpnIndex}")

    // 外接第二课堂
    fun provideSecondClass(route: String) =
        root + nav("/LjE0OS4yMDYuMTUwLjE2NS4xNDUuMTYwLjIwMi45NC4yMDAuMTUwLjE2NS4xNTkuMTUyLjE0OC4xNTQuMTUwLjIxOC45NS4xNDcuMjEw$route?vpn-12-2ketang.gdufe.edu.cn")
}