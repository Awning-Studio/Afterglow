package com.awning.afterglow.ui.screen

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.awning.afterglow.module.edunotice.EduNotice
import com.awning.afterglow.module.edunotice.NoticeContent
import com.awning.afterglow.module.edunotice.NoticeInfo
import com.awning.afterglow.module.edunotice.Paragraph
import com.awning.afterglow.module.edunotice.ParagraphStyle
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.request.waterfall.Waterfall
import com.awning.afterglow.toolkit.Trigger
import com.awning.afterglow.ui.component.MTopAppBar
import com.awning.afterglow.ui.component.loadPDF
import com.awning.afterglow.ui.component.pdfView
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.padding
import com.awning.afterglow.ui.twiceOfPadding
import com.awning.afterglow.viewmodel.controller.NoticeController
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


private const val ROUTE_LIST = "NoticeList"
private const val ROUTE_DETAIL = "NoticeDetail"

@Composable
fun NoticeScreen(navController: NavHostController) {
    val innerNavController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var noticeContent by remember { mutableStateOf<NoticeContent?>(null) }

    LaunchedEffect(key1 = Unit) {
        NoticeController.updateLastNoticeId()
    }

    NavHost(
        navController = innerNavController,
        startDestination = ROUTE_LIST,
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it } },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally { it } }
    ) {
        composable(ROUTE_LIST) {
            NoticeListScreen(navController = navController) { noticeBasicInfo ->
                noticeContent = null

                coroutineScope.launch {
                    EduNotice.getDetail(noticeBasicInfo).catch {
                        clipboardManager.setText(buildAnnotatedString {
                            append(noticeBasicInfo.url)
                        })
                        Toast.makeText(context, "获取失败: $it，原链接已复制", Toast.LENGTH_SHORT)
                            .show()
                    }.collect {
                        noticeContent = it
                    }
                }

                innerNavController.navigate(ROUTE_DETAIL)
            }
        }
        composable(ROUTE_DETAIL) {
            NoticeDetailScreen(innerNavController, noticeContent)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoticeListScreen(
    navController: NavHostController,
    onItemClick: (notice: NoticeInfo) -> Unit
) {
    val trigger = remember { Trigger() }

    MTopAppBar(title = ModuleRoute.ModuleNotice.title, navController = navController) {
        if (NoticeController.notices.isEmpty()) {
            EmptyScreen()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(halfOfPadding)
            ) {
                NoticeController.notices.forEach {
                    item {
                        ElevatedCard(
                            onClick = {
                                trigger.touch {
                                    it.isNew = false
                                    onItemClick(it)
                                }
                            },
                            modifier = Modifier.padding(halfOfPadding)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(twiceOfPadding)
                            ) {
                                Text(
                                    text = it.title,
                                    color = if (it.isNew) MaterialTheme.colorScheme.primary else Color.Gray,
                                    fontWeight = FontWeight(600)
                                )
                                Text(text = it.time, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun NoticeDetailScreen(navController: NavHostController, noticeContent: NoticeContent?) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val pdf = remember { mutableStateListOf<ImageBitmap>() }

    LaunchedEffect(noticeContent) {
        noticeContent?.pdf?.let { url ->
            loadPDF(context, url, noticeContent.title).catch {
                Toast.makeText(context, "PDF文件加载失败", Toast.LENGTH_SHORT).show()
            }.collect {
                pdf.add(it)
            }
        }
    }

    MTopAppBar(title = "通知详情", navController = navController) {
        noticeContent?.let { notice ->
            LazyColumn(
                contentPadding = PaddingValues(twiceOfPadding)
            ) {
                item {
                    Text(
                        text = notice.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                item {
                    ClickableText(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    textDecoration = TextDecoration.Underline,
                                    color = Color(
                                        0xFF2196F3
                                    )
                                )
                            ) {
                                append("原文链接")
                            }
                        },
                        onClick = { _ ->
                            clipboardManager.setText(buildAnnotatedString { append(notice.url) })
                            Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.padding(vertical = padding)
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(halfOfPadding))
                    SelectionContainer {
                        Column {
                            Text(
                                text = notice.id,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = notice.time,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = notice.from,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(padding + halfOfPadding))
                }
                notice.pdf?.let { pdfURL ->
                    item {
                        ClickableText(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        textDecoration = TextDecoration.Underline,
                                        color = Color(
                                            0xFF2196F3
                                        )
                                    )
                                ) {
                                    append("原 PDF 链接")
                                }
                            },
                            onClick = {
                                clipboardManager.setText(buildAnnotatedString { append(pdfURL) })
                                Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    pdfView(list = pdf)
                }
                item {
                    SelectionContainer {
                        Column {
                            notice.content.forEach { paragraph ->
                                when (paragraph.style) {
                                    ParagraphStyle.Normal, ParagraphStyle.H1, ParagraphStyle.H2 -> {
                                        NoticeTextContent(paragraph = paragraph)
                                    }

                                    ParagraphStyle.Image -> {
                                        NoticeImageContent(url = paragraph.text[0].text.toString())
                                    }

                                    ParagraphStyle.Table -> {
                                        val table = paragraph.text[0].text as ArrayList<*>
                                        val firstRow = table[0] as ArrayList<*>
                                        val column = firstRow.size

                                        Row(
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            firstRow.forEach {
                                                it as String
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                ) {
                                                    Text(
                                                        text = it,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.padding(3.dp)
                                                    )
                                                }
                                            }
                                        }

                                        val itemHeight = remember { 50.dp }
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(column),
                                            modifier = Modifier.heightIn(max = 260.dp)
                                        ) {
                                            for (i in 1 until table.size) {
                                                val tableItem = table[i] as ArrayList<*>
                                                tableItem.forEach { text ->
                                                    text as String
                                                    item {
                                                        LazyColumn(
                                                            modifier = Modifier.height(
                                                                itemHeight
                                                            )
                                                        ) {
                                                            item {
                                                                Box(
                                                                    contentAlignment = Alignment.Center,
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                ) {
                                                                    Spacer(
                                                                        modifier = Modifier
                                                                            .width(0.5.dp)
                                                                            .fillParentMaxHeight()
                                                                            .background(Color.LightGray)
                                                                            .align(Alignment.TopStart)
                                                                    )
                                                                    Spacer(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .height(0.5.dp)
                                                                            .background(Color.LightGray)
                                                                            .align(Alignment.TopStart)
                                                                    )
                                                                    Text(
                                                                        text = text,
                                                                        fontSize = 9.sp,
                                                                        lineHeight = 12.sp,
                                                                        textAlign = TextAlign.Center
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun NoticeTextContent(paragraph: Paragraph) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val space = when (paragraph.style) {
        ParagraphStyle.H1 -> 16.dp
        ParagraphStyle.H2 -> 12.dp
        else -> 10.dp
    }
    Spacer(modifier = Modifier.height(space - 2.dp))
    val urlRanges = arrayListOf<IntRange>()
    val text = buildAnnotatedString {
        var count = 0
        paragraph.text.forEach {
            if (it.url == null) {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                    append(it.text.toString())
                }
            } else {
                urlRanges.add(count until count + it.text.toString().length - 1)
                withStyle(
                    SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color = Color(0xFF2196F3)
                    )
                ) {
                    append(it.text.toString())
                }
            }
            count += it.text.toString().length
        }
    }

    ClickableText(
        text = text,
        onClick = { index ->
            for (i in urlRanges.indices) {
                if (index in urlRanges[i]) {
                    clipboardManager.setText(buildAnnotatedString { text.substring(urlRanges[i]) })
                    Toast.makeText(context, "链接已复制", Toast.LENGTH_SHORT).show()
                }
            }
        },
        style = paragraph.style.textStyle
    )
    Spacer(modifier = Modifier.height(space))
}

@Composable
private fun NoticeImageContent(url: String) {
    val context = LocalContext.current
    var image by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(key1 = Unit) {
        Waterfall.getBitmap(url).catch {
            Toast.makeText(context, "图片加载失败", Toast.LENGTH_SHORT).show()
        }.collect {
            image = it
        }
    }


    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        image?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Image")
        }
    }
}