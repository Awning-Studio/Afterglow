package com.awning.afterglow.ui.screen

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Attractions
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudCircle
import androidx.compose.material.icons.rounded.FlashAuto
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.SyncLock
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.awning.afterglow.module.edusystem.EduSystemUtil
import com.awning.afterglow.module.webvpn.WebVPN
import com.awning.afterglow.navroute.BottomRoute
import com.awning.afterglow.request.waterfall.Waterfall
import com.awning.afterglow.store.Lighting
import com.awning.afterglow.store.Version
import com.awning.afterglow.type.User
import com.awning.afterglow.ui.NoBorder
import com.awning.afterglow.ui.borderOfSurfaceVariant
import com.awning.afterglow.ui.component.AfterglowTextFiled
import com.awning.afterglow.ui.component.PasswordOutlinedTextField
import com.awning.afterglow.ui.component.RadioButton
import com.awning.afterglow.ui.component.limitTextSize
import com.awning.afterglow.ui.halfOfPadding
import com.awning.afterglow.ui.padding
import com.awning.afterglow.ui.theme.ErrorColor
import com.awning.afterglow.ui.theme.SuccessColor
import com.awning.afterglow.ui.twiceOfPadding
import com.awning.afterglow.viewmodel.RuntimeVM
import com.awning.afterglow.viewmodel.controller.LoginController
import com.awning.afterglow.viewmodel.controller.SettingController
import com.awning.afterglow.viewmodel.controller.UserController
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate

private val itemIconSize = 19.dp

var meScreenDatePickerBottomSheetVisible by mutableStateOf(false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeScreen() {
    val avatarSize = 60.dp
    val stateIconSize = 18.dp

    val users by UserController.userFlow().collectAsState(emptyList())
    val lastLoginUser by SettingController.lastUsernameFlow().collectAsState(null)
    var loginDialogVisible by remember { mutableStateOf(false) }
    val initialUser by remember {
        derivedStateOf {
            for (i in users) {
                if (i.username == lastLoginUser) {
                    return@derivedStateOf i
                }
            }
            return@derivedStateOf null
        }
    }
    var userDialogVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { MeTopAppBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box {
                // 头像背景 + 信息
                OutlinedCard(
                    onClick = {
                        loginDialogVisible = true
                    },
                    shape = RectangleShape,
                    border = NoBorder
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(avatarSize + twiceOfPadding * 2)
                            .padding(
                                start = avatarSize + twiceOfPadding * 2,
                                end = twiceOfPadding,
                                top = twiceOfPadding,
                                bottom = twiceOfPadding
                            )
                    ) {
                        Column {
                            Text(
                                text = lastLoginUser ?: "点击登录",
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(modifier = Modifier.padding(top = halfOfPadding)) {
                                Icon(
                                    imageVector = if (RuntimeVM.isLoggingIn) Icons.Rounded.SyncLock else Icons.Rounded.CloudCircle,
                                    contentDescription = "CloudCircle",
                                    modifier = Modifier.size(stateIconSize),
                                    tint = RuntimeVM.eduSystem?.let { SuccessColor } ?: ErrorColor
                                )
                            }
                        }
                    }
                }

                // 头像
                OutlinedCard(
                    onClick = {
                        userDialogVisible = true
                    },
                    shape = CircleShape,
                    border = NoBorder,
                    elevation = CardDefaults.outlinedCardElevation(5.dp),
                    modifier = Modifier.padding(twiceOfPadding)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Attractions,
                        contentDescription = "Attractions",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(avatarSize)
                            .padding(15.dp)
                    )
                }
            }


            val materialYou by SettingController.martialYouFlow().collectAsState(initial = true)
            val autoLogin by SettingController.autoLoginFlow().collectAsState(initial = false)
            val schoolStart by SettingController.schoolStartFlow()
                .collectAsState(initial = LocalDate.now().toString())
            val schedule by SettingController.scheduleFlow().collectAsState(initial = 0)

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(padding)
            ) {
                item {
                    SettingItem(
                        name = "Afterglow 版本",
                        icon = Icons.Rounded.Verified,
                        visibleValue = Version.name
                    )
                }
                item {
                    BooleanSettingItem(
                        name = "Material You(12+)",
                        icon = Icons.Rounded.AutoAwesome,
                        value = materialYou
                    ) {
                        SettingController.setMaterialYou(it)
                    }
                }
                item {
                    BooleanSettingItem(
                        name = "自动登录",
                        icon = Icons.Rounded.FlashAuto,
                        value = autoLogin
                    ) {
                        SettingController.setAutoLogin(it)
                    }
                }
                item {
                    StringSettingItem(
                        name = "开学日期",
                        icon = Icons.Rounded.Timelapse,
                        value = schoolStart
                    ) {
                        meScreenDatePickerBottomSheetVisible = true
                    }
                }
                item {
                    OpsSettingItem(
                        name = "时间表",
                        icon = Icons.Rounded.Alarm,
                        value = schedule,
                        ops = listOf("广州校区", "佛山校区")
                    ) {
                        SettingController.setSchedule(it)
                    }
                }
            }
        }
    }

    LoginDialog(loginDialogVisible, initialUser) {
        loginDialogVisible = false
    }

    var switchLoginDialogVisible by remember { mutableStateOf(false) }
    var toLoginUser by remember { mutableStateOf<User?>(null) }
    LoginDialog(switchLoginDialogVisible, toLoginUser) {
        switchLoginDialogVisible = false
    }

    UserDialog(
        visible = userDialogVisible,
        users = users,
        lastLoginUser = lastLoginUser,
        onDismiss = { userDialogVisible = false }
    ) {
        toLoginUser = it
        switchLoginDialogVisible = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingItem(
    name: String,
    icon: ImageVector,
    visibleValue: String,
    onClick: () -> Unit = {}
) {
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(twiceOfPadding),
        border = borderOfSurfaceVariant(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
        modifier = Modifier.padding(padding)
    ) {
        Column(
            modifier = Modifier.padding(twiceOfPadding)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Version",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(itemIconSize)
            )
            Text(
                text = name,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = halfOfPadding)
            )
            Text(
                text = visibleValue,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = halfOfPadding)
            )
        }
    }
}

@Composable
private fun BooleanSettingItem(
    name: String,
    icon: ImageVector,
    value: Boolean,
    onClick: (Boolean) -> Unit
) {
    SettingItem(name = name, icon = icon, visibleValue = if (value) "开" else "关") {
        onClick(!value)
    }
}

@Composable
private fun OpsSettingItem(
    name: String,
    icon: ImageVector,
    value: Int,
    ops: List<String>,
    onClick: (Int) -> Unit
) {
    SettingItem(name = name, icon = icon, visibleValue = ops[value]) {
        onClick(if (value < ops.size - 1) value + 1 else 0)
    }
}


@Composable
fun StringSettingItem(name: String, icon: ImageVector, value: String, onClick: () -> Unit) {
    SettingItem(name = name, icon = icon, visibleValue = value, onClick)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeTopAppBar() {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { Lighting.OrangeGemstone = !Lighting.OrangeGemstone }) {
                Icon(
                    imageVector = BottomRoute.Me.icon,
                    contentDescription = "NavIcon",
                    tint = if (Lighting.OrangeGemstone) Lighting.MColor.OrangeGemstone else Color.LightGray
                )
            }
        },
        title = {}
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDialog(
    visible: Boolean,
    users: List<User>,
    lastLoginUser: String?,
    onDismiss: () -> Unit,
    onLogin: (User) -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "关闭")
                }
            },
            title = { Text(text = "已保存用户(${users.size})") },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .width(250.dp)
                        .height(260.dp)
                ) {
                    users.forEach { user ->
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
                                    Text(text = user.username)
                                    Row {
                                        IconButton(onClick = { onLogin(user) }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Login,
                                                contentDescription = "Login"
                                            )
                                        }
                                        IconButton(onClick = {
                                            if (user.username == lastLoginUser) {
                                                SettingController.removeLastUsername()
                                                RuntimeVM.eduSystem = null
                                            }
                                            UserController.remove(user)
                                        }) {
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
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginDialog(visible: Boolean, initialUser: User?, onDismiss: () -> Unit) {
    if (visible) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        var username by remember { mutableStateOf(initialUser?.username ?: "") }
        var password by remember { mutableStateOf(initialUser?.password ?: "") }
        var secondClassPwd by remember {
            mutableStateOf(
                initialUser?.secondClassPwd?.let { if (it == username) null else it } ?: "")
        }
        var session by remember { mutableStateOf(initialUser?.session ?: Waterfall.Session()) }
        var interactable by remember { mutableStateOf(true) }
        val withWebVPN by SettingController.withWebVPNFlow().collectAsState(initial = false)
        val rememberMe by SettingController.rememberMeFlow().collectAsState(initial = true)
        val skipCaptcha by SettingController.skipCaptchaFlow().collectAsState(initial = true)
        var captcha by remember { mutableStateOf<Bitmap?>(null) }
        var captchaContent by remember { mutableStateOf("") }
        var webVPN by remember { mutableStateOf<WebVPN?>(null) }

        fun getCaptcha() {
            coroutineScope.launch {
                if (withWebVPN) {
                    LoginController.getCaptcha(
                        webVPN,
                        User(username, password, secondClassPwd, session)
                    ).catch {
                        Toast.makeText(context, it.message ?: it.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }.collect {
                        webVPN = it.first
                        captcha = it.second
                    }
                } else {
                    LoginController.getCaptcha(session).catch {
                        Toast.makeText(context, it.message ?: it.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }.collect {
                        captcha = it
                    }
                }
            }
        }

        AlertDialog(
            properties = DialogProperties(dismissOnClickOutside = interactable),
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    enabled = !RuntimeVM.isLoggingIn && interactable,
                    onClick = {
                        coroutineScope.launch {
                            if (RuntimeVM.eduSystem != null) {
                                RuntimeVM.eduSystem?.logout()?.catch {
                                    Toast.makeText(context, "登出失败: ${it.message?:it.toString()}", Toast.LENGTH_SHORT).show()
                                }?.collect {
                                    RuntimeVM.eduSystem = null
                                    Toast.makeText(context, "已登出", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                interactable = false

                                if (withWebVPN) {
                                    LoginController.login(
                                        webVPN,
                                        User(username, password, secondClassPwd, session),
                                        captchaContent.ifBlank { null },
                                        captcha
                                    ).catch {
                                        Toast.makeText(
                                            context,
                                            it.message ?: it.toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        interactable = true
                                    }.collect {
                                        interactable = true
                                        Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT)
                                            .show()
                                        if (rememberMe) {
                                            UserController.set(it)
                                        }
                                        onDismiss()
                                    }
                                } else {
                                    LoginController.login(
                                        session,
                                        username,
                                        password,
                                        secondClassPwd,
                                        captchaContent.ifBlank { null },
                                        captcha
                                    ).catch {
                                        if (it.message == "登录失败: 请求超时") {
                                            Toast.makeText(
                                                context,
                                                "正在尝试 WebVPN 登录",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            LoginController.login(
                                                webVPN,
                                                User(username, password, secondClassPwd, session),
                                                captchaContent.ifBlank { null },
                                                captcha
                                            ).catch {
                                                Toast.makeText(
                                                    context,
                                                    it.message ?: it.toString(),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                interactable = true
                                            }.collect {
                                                interactable = true
                                                Toast.makeText(
                                                    context,
                                                    "登录成功",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                if (rememberMe) {
                                                    UserController.set(it)
                                                }
                                                onDismiss()
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                it.message ?: it.toString(),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            interactable = true
                                        }
                                    }.collect {
                                        interactable = true
                                        Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT)
                                            .show()
                                        if (rememberMe) {
                                            UserController.set(it)
                                        }
                                        onDismiss()
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text(
                        text = if (RuntimeVM.isLoggingIn) "登录中" else (if (RuntimeVM.eduSystem?.user?.username == username) "登出" else "登录"),
                        color = if (RuntimeVM.eduSystem?.user?.username == username) ErrorColor else LocalTextStyle.current.color
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = interactable,
                    onClick = onDismiss
                ) {
                    Text(text = "取消")
                }
            },
            title = { Text(text = "登录账号") },
            text = {
                Column {
                    AfterglowTextFiled(
                        value = username,
                        onValueChange = {
                            username = limitTextSize(it, 11)
                            if (EduSystemUtil.verifyUsername(username) == null) {
                                if (username != initialUser?.username && session == initialUser?.session) {
                                    session = Waterfall.Session()
                                    if (!skipCaptcha) {
                                        getCaptcha()
                                    }
                                }
                            }
                        },
                        numberOnly = true,
                        enabled = interactable,
                        label = { Text(text = "学号") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(twiceOfPadding))

                    PasswordOutlinedTextField(
                        enabled = interactable,
                        label = { Text(text = "密码（门户）") },
                        value = password,
                        singleLine = true,
                        onValueChange = {
                            password = it
                        }
                    )
                    Spacer(modifier = Modifier.height(twiceOfPadding))

                    PasswordOutlinedTextField(
                        enabled = interactable,
                        label = { Text(text = "第二课堂密码 (可选)") },
                        placeholder = { Text(text = "默认可不写", color = Color.Gray) },
                        value = secondClassPwd,
                        singleLine = true,
                        onValueChange = {
                            secondClassPwd = it
                        }
                    )
                    Spacer(modifier = Modifier.height(twiceOfPadding))

                    // 记住我选项
                    RadioButton(
                        label = "记住我",
                        enabled = interactable,
                        selected = rememberMe,
                        onCheckedChange = { SettingController.setRememberMe(it) }
                    )

                    // WebVPN 选项
                    RadioButton(
                        label = "WebVPN",
                        enabled = interactable,
                        selected = withWebVPN,
                        onCheckedChange = {
                            SettingController.setWithWebVPN(it)
                            if (!skipCaptcha && withWebVPN && EduSystemUtil.verifyUsername(username) == null) {
                                getCaptcha()
                            }
                        }
                    )

                    // 跳过验证码选项
                    RadioButton(
                        label = "跳过验证码",
                        enabled = interactable,
                        selected = skipCaptcha,
                        onCheckedChange = { SettingController.setSkipCaptcha(it) }
                    )

                    if (!skipCaptcha) {
                        LaunchedEffect(Unit) {
                            // 获取验证码
                            getCaptcha()
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 验证码图片
                            ElevatedCard(
                                onClick = {
                                    if (interactable) {
                                        getCaptcha()
                                    }
                                },
                                shape = RectangleShape,
                                modifier = Modifier
                                    .height(45.dp)
                                    .aspectRatio(2.8f)
                            ) {
                                if (captcha == null) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = if (LoginController.isGettingCaptcha) "获取中" else "点击重试",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                } else {
                                    captcha?.let {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = "Captcha",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }

                            // 验证码输入
                            AfterglowTextFiled(
                                value = captchaContent,
                                onValueChange = {
                                    captchaContent = limitTextSize(it, 4)
                                },
                                modifier = Modifier.padding(start = twiceOfPadding, bottom = 4.dp),
                                enabled = interactable,
                                label = { Text(text = "验证码") },
                                singleLine = true,
                            )
                        }
                    }
                }
            }
        )
    }
}