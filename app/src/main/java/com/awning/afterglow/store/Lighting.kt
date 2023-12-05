package com.awning.afterglow.store

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * 导航图标颜色
 */
object Lighting {
    var Sapphire by mutableStateOf(false)
    var Ruby by mutableStateOf(false)
    var Amethyst by mutableStateOf(false)
    var OrangeGemstone by mutableStateOf(false)

    object MColor {
        val Sapphire = Color(0xFFF44336)
        val Ruby = Color(0xFF078DF3)
        val Amethyst = Color(0xFF673AB7)
        val OrangeGemstone = Color(0xFFFF8222)
    }
}