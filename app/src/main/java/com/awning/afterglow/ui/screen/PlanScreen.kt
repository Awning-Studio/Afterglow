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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.awning.afterglow.toolkit.Transformer
import com.awning.afterglow.type.Plan
import com.awning.afterglow.ui.component.AfterglowTextFiled
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.padding
import com.awning.afterglow.ui.twiceOfPadding
import com.awning.afterglow.viewmodel.controller.PlanController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Date

var deadline by mutableStateOf<LocalDate?>(null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val plans by PlanController.planFlow().collectAsState(initial = emptyList())
    var planAddDialogVisible by remember { mutableStateOf(false) }
    var datePickerDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        deadline = null
    }

    Scaffold(
        topBar = { PlanTopAppBar() }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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

            FloatingActionButton(
                onClick = { planAddDialogVisible = true }, modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 30.dp, bottom = 60.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add")
            }
        }
    }

    PlanAddDialog(
        visible = planAddDialogVisible,
        onPickDateRequest = {
            datePickerDialogVisible = true
        }
    ) {
        planAddDialogVisible = false
    }

    DatePickerAlertDialog(visible = datePickerDialogVisible, onConfirm = {
        deadline = it
        datePickerDialogVisible = false
    }) {
        datePickerDialogVisible = false
    }
}


@Composable
private fun PlanAddDialog(visible: Boolean, onPickDateRequest: () -> Unit, onDismiss: () -> Unit) {
    if (visible) {
        val context = LocalContext.current

        var name by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

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
                    PlanController.set(Plan(name, desc, deadline.toString()))
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
                    ElevatedSuggestionChip(
                        onClick = {
                            onPickDateRequest()
                        },
                        label = { Text(text = deadline?.toString() ?: "截止日期（可选）") }
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
private fun DatePickerAlertDialog(
    visible: Boolean,
    onConfirm: (LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    if (visible) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm(
                            datePickerState.selectedDateMillis?.let { timeStamp ->
                                Transformer.localDateOf(Date(timeStamp))
                            }
                        )
                    }
                ) {
                    Text(text = "确定")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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