package com.awning.afterglow.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.awning.afterglow.navroute.BottomRoute
import com.awning.afterglow.store.Lighting
import com.awning.afterglow.type.Plan
import com.awning.afterglow.ui.component.AfterglowTextFiled
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.padding
import com.awning.afterglow.ui.twiceOfPadding
import com.awning.afterglow.viewmodel.controller.PlanController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val plans by PlanController.planFlow().collectAsState(initial = emptyList())
    var planAddDialogVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { PlanTopAppBar() }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FloatingActionButton(
                onClick = { planAddDialogVisible = true }, modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 30.dp, bottom = 60.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add")
            }
            if (plans.isEmpty()) {
                EmptyScreen { Text(text = "空空的") }
            } else {
                LazyColumn {
                    plans.forEach { plan ->
                        item {
                            ElevatedCard(
                                onClick = {},
                                modifier = Modifier.padding(twiceOfPadding)
                            ) {
                                var checked by remember { mutableStateOf(false) }

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(twiceOfPadding)
                                ) {
                                    Column {
                                        Text(
                                            text = plan.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(padding))
                                        Text(
                                            text = "期限: ${plan.deadline.ifBlank { " 无期限 " }}",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(modifier = Modifier.height(halfOfPadding))
                                        Text(
                                            text = "描述: ${plan.desc}",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = {
                                            checked = it
                                            if (it) {
                                                coroutineScope.launch {
                                                    delay(300)
                                                    PlanController.remove(plan)
                                                    Toast.makeText(
                                                        context,
                                                        "已完成",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier.padding(start = twiceOfPadding)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    PlanAddDialog(visible = planAddDialogVisible) {
        planAddDialogVisible = false
    }
}


@Composable
private fun PlanAddDialog(visible: Boolean, onDismiss: () -> Unit) {
    if (visible) {
        val context = LocalContext.current

        var name by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var deadline by remember { mutableStateOf("") }

        AlertDialog(
            title = { Text(text = "添加计划") },
            onDismissRequest = onDismiss,
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "取消")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "请输入一个名字", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    if (deadline.isNotBlank()) {
                        deadline = deadline.replace(Regex("\\D+"), "-")
                        try {
                            LocalDate.parse(deadline)
                        } catch (_: Exception) {
                            Toast.makeText(context, "期限格式错误", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                    }
                    PlanController.set(Plan(name, desc, deadline))
                    Toast.makeText(context, "已添加", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }) {
                    Text(text = "添加")
                }
            },
            text = {
                Column {
                    AfterglowTextFiled(
                        value = name,
                        onValueChange = {
                            name = it
                        },
                        label = { Text(text = "名称") }
                    )
                    Spacer(modifier = Modifier.height(padding))
                    AfterglowTextFiled(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text(text = "截止日期(可选)") },
                        placeholder = { Text(text = "YYYY-MM-DD", color = Color.Gray) }
                    )
                    Spacer(modifier = Modifier.height(padding))
                    AfterglowTextFiled(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text(text = "描述(可选)") }
                    )
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanTopAppBar() {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { Lighting.Ruby = !Lighting.Ruby }) {
                Icon(
                    imageVector = BottomRoute.Plan.icon,
                    contentDescription = "NavIcon",
                    tint = if (Lighting.Ruby) Lighting.MColor.Ruby else Color.LightGray
                )
            }
        },
        title = {}
    )
}