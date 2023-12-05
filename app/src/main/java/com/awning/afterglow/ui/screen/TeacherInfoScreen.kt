package com.awning.afterglow.ui.screen

import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.awning.afterglow.module.edusystem.api.TeacherInfoDetail
import com.awning.afterglow.module.edusystem.api.TeacherInfoList
import com.awning.afterglow.module.edusystem.api.TeachingCourse
import com.awning.afterglow.module.edusystem.api.getTeacherInfoDetail
import com.awning.afterglow.module.edusystem.api.getTeacherInfoList
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.ui.borderOfGray
import com.awning.afterglow.ui.component.IconText
import com.awning.afterglow.ui.component.SearchBar
import com.awning.afterglow.ui.component.MTopAppBar
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.padding
import com.awning.afterglow.ui.twiceOfPadding
import com.awning.afterglow.viewmodel.RuntimeVM
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


private const val ROUTE_SEARCH = "SearchScreen"
private const val ROUTE_DETAIL = "DetailScreen"

@Composable
fun TeacherInfoScreen(navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val innerNavController = rememberNavController()
    var teacherName by remember { mutableStateOf("") }
    var teacherInfoList by remember { mutableStateOf<TeacherInfoList?>(null) }
    var teacherInfoDetail by remember { mutableStateOf<TeacherInfoDetail?>(null) }

    NavHost(
        navController = innerNavController,
        startDestination = ROUTE_SEARCH,
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it } },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally { it } }
    ) {
        composable(ROUTE_SEARCH) { _ ->
            SearchScreen(navController, teacherName, teacherInfoList, {
                teacherName = it
            }, {
                teacherInfoList = it
            }) { id ->
                teacherInfoDetail = null

                coroutineScope.launch {
                    RuntimeVM.eduSystem?.getTeacherInfoDetail(id)?.catch {
                        Toast.makeText(
                            context,
                            "获取失败: $it",
                            Toast.LENGTH_SHORT
                        ).show()
                    }?.collect {
                        teacherInfoDetail = it
                    }
                }

                if (RuntimeVM.eduSystem == null) {
                    Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                } else {
                    innerNavController.navigate(ROUTE_DETAIL)
                }
            }
        }
        composable(ROUTE_DETAIL) {
            DetailScreen(navController = innerNavController, teacherInfoDetail)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    navController: NavHostController,
    name: String,
    teacherInfoList: TeacherInfoList?,
    onValueChange: (name: String) -> Unit,
    onGetTeacherBasicInfoList: (TeacherInfoList) -> Unit,
    onCheckTeacherInfo: (id: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val firstVisibleItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = firstVisibleItemIndex) {
        if (!isLoading && firstVisibleItemIndex != 0 && firstVisibleItemIndex + lazyListState.layoutInfo.visibleItemsInfo.size + 5 >= lazyListState.layoutInfo.totalItemsCount) {
            isLoading = true
            teacherInfoList?.let { infoList ->
                if (infoList.currentPage < infoList.totalPages) {
                    RuntimeVM.eduSystem?.getTeacherInfoList(
                        name,
                        infoList.currentPage + 1,
                        infoList
                    )?.catch {
                        isLoading = false
                        Toast.makeText(
                            context,
                            "加载失败: $it",
                            Toast.LENGTH_SHORT
                        ).show()
                    }?.collect {
                        onGetTeacherBasicInfoList(it)
                        isLoading = false
                    }
                }
            }
        }
    }

    MTopAppBar(
        title = ModuleRoute.ModuleTeacherInfo.title,
        navController = navController
    ) {
        Column {
            SearchBar(value = name, onValueChange = onValueChange) {
                coroutineScope.launch {
                    if (RuntimeVM.eduSystem == null) {
                        Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        RuntimeVM.eduSystem?.getTeacherInfoList(name)?.catch {
                            isLoading = false
                            Toast.makeText(
                                context,
                                "搜索失败: $it",
                                Toast.LENGTH_SHORT
                            ).show()
                        }?.collect {
                            onGetTeacherBasicInfoList(it)
                            isLoading = false
                        }
                    }
                }
            }

            if (teacherInfoList == null) {
                EmptyScreen { Text(text = "先搜索试试") }
            } else {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(halfOfPadding)
                ) {
                    teacherInfoList.list.forEach {
                        item {
                            ElevatedCard(
                                onClick = {},
                                modifier = Modifier.padding(halfOfPadding)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(twiceOfPadding)
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = it.name,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight(600)
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.Numbers,
                                            contentDescription = "Numbers",
                                            text = "工号: ${it.id}"
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        IconText(
                                            icon = Icons.Rounded.Apartment,
                                            contentDescription = "Apartment",
                                            text = "院系: ${it.department}"
                                        )
                                    }
                                    TextButton(onClick = { onCheckTeacherInfo(it.id) }) {
                                        Text(text = "查看")
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
private fun DetailScreen(navController: NavHostController, teacherInfoDetail: TeacherInfoDetail?) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    fun copy(text: String) {
        clipboardManager.setText(buildAnnotatedString { append(text) })
        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
    }

    MTopAppBar(title = "教师信息查看", navController = navController) {
        teacherInfoDetail?.let {
            LazyColumn(
                contentPadding = PaddingValues(halfOfPadding)
            ) {
                teacherInfoRow("姓名", it.name)
                teacherInfoRow("性别", it.gender)
                teacherInfoRow("政治面貌", it.politics)
                teacherInfoRow("民族", it.nation)
                teacherInfoRow("职务", it.duty)
                teacherInfoRow("职称", it.title)
                teacherInfoRow("教职工类别", it.category)
                teacherInfoRow("部门（院系）", it.department)
                teacherInfoRow("科室（系）", it.office)
                teacherInfoRow("最高学历", it.qualifications)
                teacherInfoRow("学位", it.degree)
                teacherInfoRow("研究方向", it.field)
                teacherInfoRow("手机", it.phoneNumber) { copy(it.phoneNumber) }
                teacherInfoRow("QQ", it.qQ) { copy(it.qQ) }
                teacherInfoRow("微信", it.weChat) { copy(it.weChat) }
                teacherInfoRow("电子邮件", it.email) { copy(it.email) }

                teacherInfoPanel("个人简介", it.introduction)
                teacherInfoInnerList("近 4 学期主讲课程", it.historyTeaching)
                teacherInfoInnerList("下学期计划开设课程", it.futureTeaching)

                teacherInfoPanel("教学理念", it.philosophy)
                teacherInfoPanel("最想对学时说的话", it.mostWantToSay)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.teacherInfoBasic(
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    item {
        ElevatedCard(
            onClick = { onClick?.let { it() } },
            modifier = Modifier.padding(halfOfPadding)
        ) {
            content()
        }
    }
}

private fun LazyListScope.teacherInfoRow(
    key: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    teacherInfoBasic(onClick) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(twiceOfPadding)
        ) {
            Text(
                text = key,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight(600)
            )
            Text(text = value)
        }
    }
}

private fun LazyListScope.teacherInfoColumn(
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    teacherInfoBasic(onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(twiceOfPadding)
        ) {
            content()
        }
    }
}

private fun LazyListScope.teacherInfoPanel(
    key: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    teacherInfoColumn(onClick) {
        Text(text = key, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight(600))
        if (value.isNotBlank()) {
            Text(
                text = value,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = padding)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.teacherInfoInnerList(
    key: String,
    value: List<TeachingCourse>,
    onClick: ((text: String) -> Unit)? = null
) {
    teacherInfoColumn({ }) {
        Text(text = key, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight(600))
        if (value.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(top = padding)
            ) {
                value.forEach {
                    OutlinedCard(
                        onClick = { onClick?.run { this(it.name) } },
                        modifier = Modifier.padding(vertical = halfOfPadding),
                        border = borderOfGray()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(twiceOfPadding)
                        ) {
                            Text(
                                text = it.name,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                            IconText(
                                icon = Icons.Rounded.AccessTime,
                                contentDescription = "AccessTime",
                                text = it.time
                            )
                            IconText(
                                icon = Icons.Rounded.Sort,
                                contentDescription = "Sort",
                                text = it.sort
                            )
                        }
                    }
                }
            }
        }
    }
}