package com.awning.afterglow.module.quicknetwork

import com.awning.afterglow.request.waterfall.Waterfall
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Date

object QuickNetwork {
    private val httpRequest = Waterfall
    private const val MAC = "00:00:00:00:00:00"


    /**
     * 登录
     * @param networkUser 校园网用户
     * @return [Flow]
     */
    fun login(networkUser: NetworkUser) = flow {
        val params = mapOf(
            Pair("callback", "A"),
            Pair("login_method", "1"),
            Pair("user_account", ",0,${networkUser.username}"),
            Pair("user_password", networkUser.password),
            Pair("wlan_user_ip", networkUser.ip),
            Pair("wlan_user_ipv6", ""),
            Pair("wlan_user_mac", MAC.replace(":", "")),
            Pair("wlan_ac_ip", ""),
            Pair("wlan_ac_name", ""),
            Pair("jsVersion", "4.1.3"),
            Pair("terminal_type", "2"),
            Pair("v", Date().time.toString().substring(9)),
            Pair("lang", "zh"),
        )

        httpRequest.get(QuickNetworkAPI.root + QuickNetworkAPI.login, params).collect { response ->
            val json = JSONObject(response.text.substring(2, response.text.length - 1))

            if (json.getInt("result") == 1) {
                emit("登录成功")
            } else {
                val code = try {
                    json.getString("ret_code").let { if (it.isNotBlank()) it.toInt() else 0 }
                } catch (e: Exception) {
                    0
                }

                if (code == 2) {
                    emit("设备已经在线")
                } else {
                    val msg = json.getString("msg")
                    throw Exception(msg)
                }
            }
        }
    }


    /**
     * 本机 IP
     */
    val ownIp: String?
        get() {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()

                val inetAddresses = networkInterface.inetAddresses
                while (inetAddresses.hasMoreElements()) {
                    val inetAddress = inetAddresses.nextElement()

                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.hostAddress
                    }
                }
            }
            return null
        }


    /**
     * 本机 Mac
     */
    val ownMac: String?
        get() {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()

                if (networkInterface.name == "wlan0") {
                    val mac = networkInterface.hardwareAddress

                    if (mac != null) {
                        val stringBuilder = StringBuilder()
                        for (i in mac.indices) {
                            stringBuilder.append(String.format("%02X:", mac[i]))
                        }
                        if (stringBuilder.isNotEmpty()) {
                            stringBuilder.deleteCharAt(stringBuilder.length - 1)
                        }
                        return stringBuilder.toString()
                    }
                }
            }
            return null
        }
}