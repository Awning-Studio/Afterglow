package com.awning.afterglow.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.request.HttpRequest
import com.awning.afterglow.request.HttpResponse
import com.awning.afterglow.request.waterfall.Waterfall
import com.awning.afterglow.ui.borderOfSurfaceVariant
import com.awning.afterglow.ui.component.AfterglowTextFiled
import com.awning.afterglow.ui.component.MTopAppBar
import com.awning.afterglow.ui.component.RadioButton
import com.awning.afterglow.ui.component.RunAndResetButton
import com.awning.afterglow.ui.component.TitleText
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.padding
import com.awning.afterglow.ui.theme.ErrorColor
import com.awning.afterglow.ui.theme.SuccessColor
import com.awning.afterglow.ui.theme.WarnColor
import com.awning.afterglow.ui.twiceOfPadding
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Date


@Composable
fun RequestScreen(navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var isHttps by remember { mutableStateOf(true) }

    val methods = listOf("GET", "POST")
    // 0 GET  1 POST
    var requestMethod by remember { mutableIntStateOf(0) }

    var url by remember { mutableStateOf("") }
    val params = remember { mutableStateListOf<Pair<String, () -> String>>() }
    val form = remember { mutableStateListOf<Pair<String, () -> String>>() }
    val headers = remember { mutableStateListOf<Pair<String, () -> String>>() }

    var response by remember { mutableStateOf<HttpResponse?>(null) }

    var paramsVisible by remember { mutableStateOf(true) }
    var formVisible by remember { mutableStateOf(true) }
    var headersVisible by remember { mutableStateOf(true) }

    val pairsName = listOf("参数", "表单", "请求头")
    var modifyingPairsIndex = 0
    var pairsAddDialogVisible by remember { mutableStateOf(false) }
    var responseResultSheetVisible by remember { mutableStateOf(false) }

    MTopAppBar(title = ModuleRoute.ModuleRequest.title, navController = navController) {
        LazyColumn(contentPadding = PaddingValues(twiceOfPadding)) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { isHttps = !isHttps }) {
                        Text(text = "http${if (isHttps) "s" else ""}://")
                    }
                    MTextField(
                        value = url,
                        onValueChange = { url = it },
                        placeholder = { Text(text = "www.example.com", color = Color.Gray) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(twiceOfPadding))
            }
            item {
                TextButton(onClick = {
                    if (requestMethod < methods.size - 1) {
                        requestMethod++
                    } else {
                        requestMethod = 0
                    }
                }) {
                    Text(text = "方法: ${methods[requestMethod]}")
                }
                Spacer(modifier = Modifier.height(twiceOfPadding))
            }

            item {
                PairDisplay(
                    name = "参数",
                    pairsVisible = paramsVisible,
                    pairs = params,
                    onPairsVisibleChange = { paramsVisible = !paramsVisible }
                ) {
                    modifyingPairsIndex = 0
                    pairsAddDialogVisible = true
                }
            }
            item {
                PairDisplay(
                    name = "表单",
                    pairsVisible = formVisible,
                    pairs = form,
                    onPairsVisibleChange = { formVisible = !formVisible }
                ) {
                    modifyingPairsIndex = 1
                    pairsAddDialogVisible = true
                }
            }
            item {
                PairDisplay(
                    name = "请求头",
                    pairsVisible = headersVisible,
                    pairs = headers,
                    onPairsVisibleChange = { headersVisible = !headersVisible }
                ) {
                    modifyingPairsIndex = 2
                    pairsAddDialogVisible = true
                }
            }

            item {
                RunAndResetButton(
                    onReset = {
                        url = ""
                        params.clear()
                        form.clear()
                        headers.clear()
                        focusManager.clearFocus()
                    }
                ) {
                    if (url.isBlank()) {
                        Toast.makeText(context, "缺少一个网址", Toast.LENGTH_SHORT).show()
                        return@RunAndResetButton
                    }

                    val method = HttpRequest.Method.valueOf(methods[requestMethod])

                    response = null
                    coroutineScope.launch {
                        Waterfall.request(
                            method,
                            (if (isHttps) "https://" else "http://") + url,
                            fromPairs(params),
                            fromPairs(form),
                            fromPairs(headers)
                        ).catch {
                            Toast.makeText(
                                context,
                                "请求失败: ${it.message ?: it.toString()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.collect {
                            response = it
                            responseResultSheetVisible = true
                        }
                    }
                    focusManager.clearFocus()
                }
            }
        }

        PairAddDialog(
            visible = pairsAddDialogVisible,
            title = { Text(text = "添加${pairsName[modifyingPairsIndex]}") },
            onDismiss = { pairsAddDialogVisible = false },
            onAdd = {
                when (modifyingPairsIndex) {
                    0 -> {
                        params.add(it)
                        paramsVisible = true
                    }

                    1 -> {
                        form.add(it)
                        formVisible = true
                    }

                    2 -> {
                        headers.add(it)
                        headersVisible = true
                    }
                }
            }
        )

        ResponseResultSheet(visible = responseResultSheetVisible, response = response) {
            responseResultSheetVisible = false
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResponseResultSheet(visible: Boolean, response: HttpResponse?, onDismiss: () -> Unit) {
    if (visible) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss
        ) {
            val stateColor = response?.let {
                when (it.statusCode) {
                    in 200 until 300 -> {
                        SuccessColor
                    }

                    in 300 until 400 -> {
                        WarnColor
                    }

                    in 400 until 600 -> {
                        ErrorColor
                    }

                    else -> {
                        MaterialTheme.colorScheme.onBackground
                    }
                }
            } ?: MaterialTheme.colorScheme.onBackground

            LazyColumn(
                contentPadding = PaddingValues(twiceOfPadding),
                modifier = Modifier.weight(1f)
            ) {
                response?.let { httpResponse ->
                    item {
                        ResponseResultItem(
                            name = "响应码",
                            value = buildAnnotatedString { append(httpResponse.statusCode.toString()) }
                        ) {
                            it.copy(color = stateColor, fontWeight = FontWeight.Bold)
                        }
                    }
                    item {
                        ResponseResultItem(
                            name = "URL",
                            value = buildAnnotatedString { append(httpResponse.url) }
                        )
                    }
                    item {
                        ResponseResultItem(
                            name = "Cookie",
                            value = httpResponse.cookies?.let {
                                buildAnnotatedString {
                                    it.forEachIndexed { index, httpCookie ->
                                        append("$httpCookie")
                                        if (index != it.size - 1) {
                                            append("\n")
                                        }
                                    }
                                }
                            } ?: buildAnnotatedString { append("无") }
                        )
                    }
                    item {
                        ResponseResultItem(
                            name = "响应头",
                            value = buildAnnotatedString {
                                httpResponse.headers.keys.forEachIndexed { index, key ->
                                    append("$key: ${httpResponse.headers[key]}")
                                    if (index != httpResponse.headers.size - 1) {
                                        append("\n")
                                    }
                                }
                            }
                        )
                    }
                    item {
                        ResponseResultItem(
                            name = "响应",
                            value = buildAnnotatedString { append(httpResponse.text) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResponseResultItem(
    name: String,
    value: AnnotatedString,
    getStyle: (TextStyle) -> TextStyle = { it }
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    TitleText(text = name) {
        clipboardManager.setText(value)
        Toast.makeText(context, "已复制$name", Toast.LENGTH_SHORT).show()
    }
    Spacer(modifier = Modifier.height(halfOfPadding))
    ElevatedCard(
        onClick = {
            clipboardManager.setText(value)
            Toast.makeText(context, "已复制$name", Toast.LENGTH_SHORT).show()
        }
    ) {
        Text(
            text = value,
            style = getStyle(MaterialTheme.typography.bodyMedium),
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        )
    }
    Spacer(modifier = Modifier.height(padding))
}


private fun fromPairs(pairs: SnapshotStateList<Pair<String, () -> String>>): Map<String, String> {
    return HashMap<String, String>().also { hashMap ->
        pairs.forEach {
            hashMap[it.first] = it.second()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PairDisplay(
    name: String,
    pairsVisible: Boolean,
    pairs: SnapshotStateList<Pair<String, () -> String>>,
    onPairsVisibleChange: () -> Unit,
    onRequestAdd: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Row {
        TextButton(onClick = onPairsVisibleChange) {
            Text(text = "$name(${pairs.size})")
        }
        TextButton(onClick = onRequestAdd) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add")
        }
    }
    AnimatedVisibility(
        visible = pairsVisible,
        enter = expandIn(),
        exit = shrinkOut()
    ) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            border = borderOfSurfaceVariant(),
        ) {
            if (pairs.isEmpty()) {
                Text(
                    text = "空的",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(padding)
                )
            } else {
                for (i in pairs.indices.reversed()) {
                    val pair = pairs[i]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = pair.first, onValueChange = {
                                pairs[0] = Pair(it, pair.second)
                            },
                            placeholder = { Text(text = "键", color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        val isTimeStamp = pair.second == ::getTimeStamp
                        OutlinedTextField(
                            value = if (isTimeStamp) "时间戳" else pair.second(),
                            enabled = !isTimeStamp,
                            onValueChange = {
                                pairs[0] = Pair(pair.first) { it }
                            },
                            placeholder = { Text(text = "值", color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                        IconButton(onClick = { pairs.remove(pair) }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(twiceOfPadding))
}

private fun getTimeStamp() = Date().time.toString()

@Composable
private fun PairAddDialog(
    visible: Boolean,
    title: @Composable () -> Unit,
    onDismiss: () -> Unit,
    onAdd: (Pair<String, () -> String>) -> Unit
) {
    if (visible) {
        val context = LocalContext.current
        var key by remember { mutableStateOf("") }
        var value by remember { mutableStateOf("") }
        var isTimeStamp by remember { mutableStateOf(false) }

        val getValue: () -> String = { value }

        AlertDialog(
            onDismissRequest = onDismiss,
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "取消")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAdd(Pair(key, if (isTimeStamp) ::getTimeStamp else getValue))
                        onDismiss()
                        Toast.makeText(context, "已添加", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(text = "添加")
                }
            },
            title = title,
            text = {
                Column {
                    AfterglowTextFiled(
                        value = key,
                        onValueChange = { key = it },
                        label = { Text(text = "键") }
                    )
                    if (!isTimeStamp) {
                        AfterglowTextFiled(
                            value = value,
                            onValueChange = { value = it },
                            label = { Text(text = "值") }
                        )
                    }
                    RadioButton(
                        label = "值为时间戳",
                        selected = isTimeStamp,
                        onCheckedChange = { isTimeStamp = it })
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: (@Composable () -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current

    TextField(
        value = value,
        onValueChange = onValueChange,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        placeholder = placeholder,
        singleLine = true,
        modifier = modifier,
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            }
        )
    )
}