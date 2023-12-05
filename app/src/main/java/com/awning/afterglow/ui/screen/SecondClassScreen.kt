package com.awning.afterglow.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.ui.component.MTopAppBar
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.theme.SuccessColor
import com.awning.afterglow.ui.twiceOfPadding
import com.awning.afterglow.viewmodel.controller.ModuleController
import com.awning.afterglow.viewmodel.controller.SettingController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondClassScreen(navController: NavHostController) {
    val username by SettingController.lastUsernameFlow().collectAsState(initial = null)
    val secondClass by ModuleController.secondClassFlow(username).collectAsState(initial = null)

    MTopAppBar(
        title = ModuleRoute.ModuleSecondClass.title,
        navController = navController
    ) {
        if (secondClass == null) {
            EmptyScreen()
        } else {
            LazyColumn(contentPadding = PaddingValues(halfOfPadding)) {
                secondClass?.list?.forEach {
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
                                val percentage =
                                    remember { it.score.toFloat() / it.requiredScore.toFloat() }

                                Column {
                                    Text(
                                        text = it.name,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight(600)
                                    )
                                    Text(
                                        text = "${it.score} / ${it.requiredScore}",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }

                                CircularProgressIndicator(
                                    progress = percentage,
                                    strokeWidth = if (percentage > 1) 4.dp else 3.dp,
                                    strokeCap = StrokeCap.Round,
                                    color = if (percentage > 1) SuccessColor else MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}