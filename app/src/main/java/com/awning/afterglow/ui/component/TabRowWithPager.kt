package com.awning.afterglow.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


/**
 * [TabRow] 与 [HorizontalPager] 的有机结合
 * @param pagerState PagerState
 * @param tabs [TabRow] 项
 * @param modifier Modifier
 * @param pageContent [HorizontalPager] 内容
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabRowWithPager(
    pagerState: PagerState,
    tabs: List<String>,
    modifier: Modifier = Modifier,
    pageContent: @Composable PagerScope.(page: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
    ) {
        // TabRow
        TabRow(
            selectedTabIndex = 0,
            divider = {
                // 调节分割线
                Divider(color = Color.LightGray.copy(0.2f))
            },
            indicator = { tabPositions ->
                // 调节当前项标记
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    contentAlignment = Alignment.Center
                ) {
                    Divider(
                        modifier = Modifier
                            .width(60.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 10.dp,
                                    topEnd = 10.dp,
                                    bottomStart = 5.dp,
                                    bottomEnd = 5.dp
                                )
                            ),
                        thickness = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = index == pagerState.currentPage,
                    text = { Text(text = tab) },
                    onClick = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(index)
                        }
                    }
                )
            }
        }

        // HorizontalPager
        HorizontalPager(state = pagerState, beyondBoundsPageCount = 1, pageContent = pageContent)
    }
}


/**
 * [ScrollableTabRow] 与 [HorizontalPager] 的有机结合
 * @param pagerState PagerState
 * @param tabs [TabRow] 项
 * @param modifier Modifier
 * @param pageContent [HorizontalPager] 内容
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScrollableTabRowWithPager(
    pagerState: PagerState,
    tabs: List<String>,
    modifier: Modifier = Modifier,
    pageContent: @Composable PagerScope.(page: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
    ) {
        // TabRow
        ScrollableTabRow(
            selectedTabIndex = 0,
            edgePadding = 0.dp,
            divider = {
                // 调节分割线
                Divider(color = Color.LightGray.copy(0.2f))
            },
            indicator = { tabPositions ->
                // 调节当前项标记
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    contentAlignment = Alignment.Center
                ) {
                    Divider(
                        modifier = Modifier
                            .width(60.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 10.dp,
                                    topEnd = 10.dp,
                                    bottomStart = 5.dp,
                                    bottomEnd = 5.dp
                                )
                            ),
                        thickness = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = index == pagerState.currentPage,
                    text = { Text(text = tab) },
                    onClick = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(index)
                        }
                    }
                )
            }
        }

        // HorizontalPager
        HorizontalPager(state = pagerState, beyondBoundsPageCount = 1, pageContent = pageContent, verticalAlignment = Alignment.Top)
    }
}