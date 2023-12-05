package com.awning.afterglow.ui.screen

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.awning.afterglow.module.careerguidance.CareerGuidance
import com.awning.afterglow.module.careerguidance.PresentationBasic
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.ui.borderOfGray
import com.awning.afterglow.ui.component.NetworkImage
import com.awning.afterglow.ui.component.MTopAppBar
import com.awning.afterglow.ui.component.TabRowWithPager
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.padding
import com.awning.afterglow.ui.twiceOfPadding
import kotlinx.coroutines.flow.catch


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PresentationScreen(navController: NavHostController) {
    val context = LocalContext.current

    val tabs = listOf("校内", "校外")
    val days = listOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
    val pagerState = rememberPagerState { tabs.size }
    var presentationBasicListInner by remember { mutableStateOf<List<PresentationBasic>?>(null) }
    var presentationBasicListOuter by remember { mutableStateOf<List<PresentationBasic>?>(null) }

    LaunchedEffect(key1 = Unit) {
        CareerGuidance.getPresentationBasic().catch {
            Toast.makeText(context, it.message ?: it.toString(), Toast.LENGTH_SHORT).show()
        }.collect {
            presentationBasicListInner = it
        }
    }

    LaunchedEffect(key1 = Unit) {
        CareerGuidance.getPresentationBasic(true).catch {
            Toast.makeText(context, it.message ?: it.toString(), Toast.LENGTH_SHORT).show()
        }.collect {
            presentationBasicListOuter = it
        }
    }

    MTopAppBar(
        title = ModuleRoute.ModulePresentation.title,
        navController = navController
    ) {
        TabRowWithPager(pagerState = pagerState, tabs = tabs) { page ->
            LazyColumn(
                contentPadding = PaddingValues(padding)
            ) {
                (if (page == 0) presentationBasicListInner else presentationBasicListOuter)?.forEach {
                    item {
                        OutlinedCard(
                            onClick = {},
                            modifier = Modifier.padding(padding),
                            border = borderOfGray()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(twiceOfPadding)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                                    ) {
                                        NetworkImage(
                                            url = it.logo,
                                            modifier = Modifier
                                                .width(40.dp)
                                                .aspectRatio(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(padding))
                                    Text(text = it.meetingName, color = MaterialTheme.colorScheme.primary)
                                }

                                Spacer(modifier = Modifier.height(padding))
                                InfoRow(name = "企业", value = it.company)
                                InfoRow(name = "类型", value = it.type)
                                InfoRow(name = "行业", value = it.sort)
                                InfoRow(name = "专业", value = it.specificRequire)
                                InfoRow(name = "城市", value = it.city)
                                InfoRow(name = "地点", value = it.area)
                                InfoRow(name = "时间", value = "${it.time} ${days[it.dayOfWeek - 1]}")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun InfoRow(name: String, value: String) {
    Row {
        Text(
            text = name,
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(halfOfPadding))
        Text(
            text = value,
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
    }
}