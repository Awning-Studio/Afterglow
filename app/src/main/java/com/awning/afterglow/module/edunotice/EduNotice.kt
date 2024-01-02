package com.awning.afterglow.module.edunotice

import com.awning.afterglow.request.HtmlParser
import com.awning.afterglow.request.HttpRequest
import com.awning.afterglow.request.HttpResponse
import com.awning.afterglow.request.fixURL
import com.awning.afterglow.request.waterfall.Waterfall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * 教务通知
 */
object EduNotice {
    private val httpRequest: HttpRequest = Waterfall

    /**
     * 获取通知列表
     * @param pageIndex 页面下标，从 1 开始
     * @return [Flow]
     */
    fun getList(pageIndex: Int = 0) = flow {
        httpRequest.get(EduNoticeAPI.root + EduNoticeAPI.notice(pageIndex)).collect {
            EduNoticeListParser.parse(it).collect(this@flow)
        }
    }


    /**
     * 获取通知内容
     * @param noticeInfo 通知
     * @return [Flow]
     */
    fun getDetail(noticeInfo: NoticeInfo) = flow {
        httpRequest.get(noticeInfo.url).collect {
            EduNoticeParser(noticeInfo.url).parse(it).collect(this@flow)
        }
    }


    /**
     * 通知列表解析器
     */
    private object EduNoticeListParser : HtmlParser<List<NoticeInfo>> {
        override fun parse(httpResponse: HttpResponse) = flow {
            val document = Jsoup.parse(httpResponse.text)

            val items = document.getElementsByClass("news_list list2")[0].getElementsByTag("li")
            val list = arrayListOf<NoticeInfo>()
            items.forEach {
                val aTag = it.getElementsByTag("a")[0]
                val title = aTag.attr("title")
                val time = it.child(1).text()

                list.add(NoticeInfo(title, time, fixURL(EduNoticeAPI.root, aTag.attr("href"))))
            }
            emit(list.toList())
        }
    }


    /**
     * 通知内容解析器
     * @property url 通知 url
     */
    private class EduNoticeParser(val url: String) : HtmlParser<NoticeContent> {
        override fun parse(httpResponse: HttpResponse) = flow {
            val document = Jsoup.parse(httpResponse.text)
            val title = document.title()

            // 文章[发布者]、[发布时间]信息
            val articleInfo = document.getElementsByClass("arti_metas")[0].children()
            val publisher = articleInfo[0].text()
            val time = articleInfo[1].text()

            // 文章内容
            val article = document.getElementsByClass("wp_articlecontent")[0]
            val articleParagraphs = article.children()

            // PDF类文章不为空
            val pdfContainer = article.getElementsByClass("wp_pdf_player")

            if (pdfContainer.isEmpty()) {
                var publishedId = ""
                val greetRegex = Regex("^.*[：:].*")
                val extraRegex = Regex("^附件[：:].*")

                // 文章起始位置
                var contentStartIndex = 0

                // 匹配问候语以确定文章开始位置
                for (i in articleParagraphs.indices) {
                    val text = articleParagraphs[i].text()

                    if (Regex("^粤财大教.*").matches(text)) {
                        // 若有，必定在问候语之前获取
                        publishedId = text
                    } else if (greetRegex.matches(text) && !extraRegex.matches(text)) {
                        contentStartIndex = i
                        break
                    }
                }

                // 获取文章内容
                val list = arrayListOf<Paragraph>()
                for (index in contentStartIndex until articleParagraphs.size) {
                    list.addAll(parseParagraph(articleParagraphs[index]))
                }
                emit(
                    NoticeContent(
                        title,
                        url,
                        publishedId,
                        publisher,
                        time,
                        list
                    )
                )
            } else {
                val pdfURL = fixURL(EduNoticeAPI.root, pdfContainer[0].attr("pdfsrc"))

                // 获取附件
                val list = arrayListOf<Paragraph>()
                articleParagraphs.forEach { element ->
                    list.addAll(parseParagraph(element))
                }

                emit(
                    NoticeContent(
                        title,
                        url,
                        "",
                        publisher,
                        time,
                        list,
                        pdfURL
                    )
                )
            }
        }


        /**
         * 整理段落
         * @param element
         * @return [List]
         */
        private fun parseParagraph(element: Element): List<Paragraph> {
            val paragraphs = arrayListOf<Paragraph>()
            var paragraphParts = arrayListOf<ParagraphPart<*>>()

            when (element.tagName()) {
                "p" -> {
                    val text = element.text()
                    val aTag = element.getElementsByTag("a")
                    val extraRegex = Regex("^附件.*")

                    // 附件格式: [^附件.*]，具备一个 a 标签（不一定有图片）
                    if (extraRegex.matches(text) && aTag.isNotEmpty()) {
                        // 附件
                        aTag.forEach {
                            paragraphParts.add(
                                ParagraphPart(
                                    it.text(),
                                    fixURL(EduNoticeAPI.root, it.attr("href"))
                                )
                            )
                            paragraphs.add(
                                Paragraph(
                                    ParagraphStyle.Normal,
                                    paragraphParts
                                )
                            )
                            paragraphParts = arrayListOf()
                        }
                    } else {
                        // 正文
                        val children = element.children()
                        val h1Regex = Regex("^[一二三四五六七八九十]+、.*")
                        val h2Regex = Regex("^（[一二三四五六七八九十]+）.*")

                        val style = if (h1Regex.matches(text)) {
                            ParagraphStyle.H1
                        } else if (h2Regex.matches(text)) {
                            ParagraphStyle.H2
                        } else {
                            ParagraphStyle.Normal
                        }

                        // 逐个片段解析
                        children.forEach { child ->
                            when (child.tagName()) {
                                "span" -> {
                                    // 文本
                                    paragraphParts.add(
                                        ParagraphPart(
                                            child.text()
                                        )
                                    )
                                }

                                "a" -> {
                                    // 带链接的正文
                                    paragraphParts.add(
                                        ParagraphPart(
                                            child.text(),
                                            fixURL(EduNoticeAPI.root, child.attr("href"))
                                        )
                                    )
                                }

                                "img" -> {
                                    // 切换段落
                                    paragraphs.add(
                                        Paragraph(
                                            style,
                                            paragraphParts
                                        )
                                    )
                                    paragraphParts = arrayListOf()

                                    // 添加图片
                                    paragraphs.add(
                                        Paragraph(
                                            ParagraphStyle.Image,
                                            paragraphParts.also {
                                                it.add(
                                                    ParagraphPart(
                                                        "",
                                                        fixURL(EduNoticeAPI.root, child.attr("src"))
                                                    )
                                                )
                                            }
                                        )
                                    )
                                }

                                else -> {
                                    paragraphParts.add(
                                        ParagraphPart(
                                            child.text()
                                        )
                                    )
                                }
                            }
                        }
                        paragraphs.add(
                            Paragraph(
                                style,
                                paragraphParts
                            )
                        )
                    }

                    return paragraphs
                }

                "table" -> {
                    // 整理表格
                    val list = arrayListOf<ArrayList<String>>()
                    val rows = element.getElementsByTag("tr")
                    var rowCount = 0

                    // 表格逐行解析
                    rows.forEachIndexed { index, row ->
                        if (index == 0) rowCount = row.childrenSize()
                        val columns = arrayListOf<String>()

                        row.children().forEach {
                            columns.add(it.text())
                        }

                        // 补位（可能会出现问题）
                        for (i in columns.size until rowCount) {
                            columns.add(0, "")
                        }
                        list.add(columns)
                    }

                    paragraphParts.add(ParagraphPart(list))
                    paragraphs.add(
                        Paragraph(
                            ParagraphStyle.Table,
                            paragraphParts
                        )
                    )

                    return paragraphs
                }

                "div" -> {
                    element.children().forEach {
                        paragraphs.addAll(parseParagraph(it))
                    }
                    return paragraphs
                }

                else -> {
                    return paragraphs
                }
            }
        }
    }
}