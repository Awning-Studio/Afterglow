package com.awning.afterglow.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.awning.afterglow.module.edusystem.EduSystemUtil
import com.awning.afterglow.module.quicknetwork.NetworkUser
import com.awning.afterglow.module.quicknetwork.QuickNetwork
import com.awning.afterglow.module.quicknetwork.QuickNetworkAPI
import com.awning.afterglow.navroute.ModuleRoute
import com.awning.afterglow.ui.component.AfterglowTextFiled
import com.awning.afterglow.ui.component.PasswordOutlinedTextField
import com.awning.afterglow.ui.component.MTopAppBar
import com.awning.afterglow.ui.component.limitTextSize
import com.awning.afterglow.ui.padding
import com.awning.afterglow.ui.twiceOfPadding
import com.awning.afterglow.viewmodel.controller.NetworkUserController
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


private var configDialogVisible by mutableStateOf(false)
private var modifyingUser by mutableStateOf<NetworkUser?>(null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNetworkScreen(navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val networkUsers by NetworkUserController.userFlow().collectAsState(initial = emptyList())

    MTopAppBar(
        title = ModuleRoute.ModuleQuickNetwork.title,
        navController = navController
    ) {
        FloatingActionButton(
            onClick = { configDialogVisible = true }, modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 90.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add")
        }
        Column {
            ElevatedCard(
                onClick = {
                    clipboardManager.setText(buildAnnotatedString { append(QuickNetworkAPI.root) })
                    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                Text(
                    text = "${QuickNetworkAPI.root}（要先连上网络）",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(twiceOfPadding)
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(padding)
            ) {
                networkUsers.forEach { networkUser ->
                    item {
                        ElevatedCard(
                            onClick = {
                                modifyingUser = networkUser
                                configDialogVisible = true
                            },
                            modifier = Modifier.padding(bottom = padding)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(twiceOfPadding)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = networkUser.name,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight(600)
                                    )
                                    Text(
                                        text = "学号: ${networkUser.username}",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "ip: ${networkUser.ip}",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            QuickNetwork.login(networkUser).catch {
                                                Toast.makeText(
                                                    context,
                                                    it.message ?: it.toString(),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }.collect {
                                                Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                                    .show()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Link,
                                        contentDescription = "Link"
                                    )
                                }
                                IconButton(onClick = {
                                    NetworkUserController.remove(networkUser)
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "DeleteOutline"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    ConfigDialog()
}


@Composable
private fun ConfigDialog() {
    if (configDialogVisible) {
        val context = LocalContext.current

        var name by remember { mutableStateOf(modifyingUser?.name ?: "") }
        var username by remember { mutableStateOf(modifyingUser?.username ?: "") }
        var password by remember { mutableStateOf(modifyingUser?.password ?: "") }
        var ip by remember { mutableStateOf(modifyingUser?.ip ?: "") }

        AlertDialog(
            onDismissRequest = {
                configDialogVisible = false
                modifyingUser = null
            },
            title = { Text(text = modifyingUser?.let { "编辑配置" } ?: "新增配置") },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "请输入一个名称", Toast.LENGTH_SHORT).show()
                    } else if (username.isBlank()) {
                        Toast.makeText(context, "请输入学号", Toast.LENGTH_SHORT).show()
                    } else if (password.isBlank()) {
                        Toast.makeText(context, "请输入密码", Toast.LENGTH_SHORT).show()
                    } else {
                        EduSystemUtil.verifyUsername(username)?.let {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        val innerIp = ip.ifBlank { QuickNetwork.ownIp }

                        if (innerIp == null) {
                            Toast.makeText(
                                context,
                                "本机 Ip 获取失败，请手动输入",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            if (modifyingUser == null) {
                                NetworkUserController.set(
                                    NetworkUser(
                                        name,
                                        username,
                                        password,
                                        innerIp
                                    )
                                )
                            }else {
                                modifyingUser?.let { networkUser ->
                                    networkUser.name = name
                                    networkUser.username = username
                                    networkUser.password = password
                                    networkUser.ip = ip
                                    NetworkUserController.set(networkUser)
                                }
                            }

                            Toast.makeText(
                                context,
                                "已保存",
                                Toast.LENGTH_SHORT
                            ).show()
                            configDialogVisible = false
                        }
                    }
                }) {
                    Text(text = "保存")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    configDialogVisible = false
                    modifyingUser = null
                }) {
                    Text(text = "取消")
                }
            },
            text = {
                Column {
                    AfterglowTextFiled(
                        value = name,
                        onValueChange = {
                            name = it
                        },
                        label = { Text(text = "名称") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(padding))
                    AfterglowTextFiled(
                        value = username,
                        onValueChange = {
                            username = limitTextSize(it, 11)
                        },
                        numberOnly = true,
                        label = { Text(text = "学号") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(padding))
                    PasswordOutlinedTextField(
                        value = password,
                        label = { Text(text = "密码") },
                        placeholder = { Text(text = "默认为身份证后 8 位", color = Color.Gray) },
                        onValueChange = {
                            password = it
                        },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(padding))
                    AfterglowTextFiled(
                        value = ip,
                        onValueChange = {
                            ip = it
                        },
                        label = { Text(text = "ip 地址 (可选)") },
                        placeholder = { Text(text = "默认为本机 IP", color = Color.Gray) },
                        singleLine = true
                    )
                }
            }
        )
    }
}