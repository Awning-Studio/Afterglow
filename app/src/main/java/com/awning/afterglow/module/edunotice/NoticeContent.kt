package com.awning.afterglow.module.edunotice

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 教务通知内容
 * @property title 标题
 * @property url 原文 url
 * @property id 版号，可能为空
 * @property from 发布者
 * @property time 发布时间
 * @property content 文章内容
 * @property pdf 文章 PDF 链接
 */
data class NoticeContent(
    val title: String,
    val url: String,
    val id: String,
    val from: String,
    val time: String,
    val content: List<Paragraph>,
    val pdf: String? = null
)


/**
 * 教务通知预览
 * @property title 标题
 * @property time 发布时间
 * @property url 原文 url
 * @property isNew 是否为新通知
 */
data class NoticeInfo(
    val title: String,
    val time: String,
    val url: String,
    var isNew: Boolean = false
) {
    val id: String = title + time
}


/**
 * 教务通知段落
 * @property style 段落样式
 * @property text 段落内容
 */
data class Paragraph(val style: ParagraphStyle, val text: List<ParagraphPart<*>>)


/**
 * 教务通知段落中的一小部分
 * @param T 类型为 [String]（文字） 或 [ArrayList]<String>（表格）
 * @property text 文本/表格
 * @property url 可能附带的链接
 */
data class ParagraphPart<T>(val text: T, val url: String? = null)


/**
 * 教务通知段落样式
 * @property textStyle 文本样式
 */
enum class ParagraphStyle(val textStyle: TextStyle) {
    /**
     * 正文
     */
    Normal(TextStyle(fontSize = 15.sp, lineHeight = 26.sp)),

    /**
     * 标题一，格式为“一、”
     */
    H1(
        TextStyle(
            fontSize = 20.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold
        )
    ),

    /**
     * 标题二，格式为“（一）”
     */
    H2(
        TextStyle(
            fontSize = 16.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight(600)
        )
    ),

    /**
     * 图片
     */
    Image(TextStyle()),

    /**
     * 表格
     */
    Table(TextStyle(fontSize = 14.sp)),
}