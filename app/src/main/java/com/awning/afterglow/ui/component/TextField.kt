package com.awning.afterglow.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp


/**
 * 限制字符长度
 */
fun limitTextSize(input: String, maxLength: Int): String {
    return if (maxLength >= 0) {
        if (maxLength == Int.MAX_VALUE) {
            input
        } else {
            if (input.length <= maxLength) {
                input
            } else {
                input.substring(0 until maxLength)
            }
        }
    } else {
        ""
    }
}


private val numberRegex = Regex("^\\d+")

private val onNumberValueChange: (input: String, callback: (String) -> Unit) -> Unit =
    { input, callback ->
        callback(numberRegex.find(input)?.value ?: "")
    }


/**
 * 自定义 [OutlinedTextField]
 * @param value 值
 * @param onValueChange 值改变回调
 * @param modifier Modifier
 * @param numberOnly 是否只支持数字
 * @param enabled 是否可用
 * @param readOnly 是否只读
 * @param textStyle 文本样式
 * @param label 标签
 * @param placeholder 占位符
 * @param leadingIcon 头部图标
 * @param trailingIcon 尾部图标
 * @param isError 是否错误
 * @param visualTransformation 内容可见性
 * @param keyboardOptions 软键盘设置
 * @param keyboardActions 软键盘右下角按钮
 * @param singleLine 单行
 * @param maxLines 最大行数
 * @param minLines 最小行数
 * @param shape 形状
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AfterglowTextFiled(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    numberOnly: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable() (() -> Unit)? = null,
    placeholder: @Composable() (() -> Unit)? = null,
    leadingIcon: @Composable() (() -> Unit)? = null,
    trailingIcon: @Composable() (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Done,
        keyboardType = if (numberOnly) KeyboardType.Number else KeyboardType.Text
    ),
    keyboardActions: KeyboardActions? = null,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    shape: Shape = RoundedCornerShape(30.dp)
) {
    val focusManager = keyboardActions?.let { null } ?: LocalFocusManager.current

    OutlinedTextField(
        value = value,
        onValueChange = {
            if (numberOnly) onNumberValueChange(it, onValueChange) else onValueChange(
                it
            )
        },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions ?: KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            }
        ),
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        shape = shape,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}


/**
 * 密码输入框
 */
@Composable
fun PasswordOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    var visible by remember { mutableStateOf(false) }

    AfterglowTextFiled(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        label = label,
        placeholder = placeholder,
        trailingIcon = {
            IconButton(onClick = {
                visible = !visible
            }) {
                val icon = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility
                Icon(imageVector = icon, contentDescription = "Visibility")
            }
        },
        isError = isError,
        visualTransformation = (if (visible) VisualTransformation.None else PasswordVisualTransformation()),
        singleLine = singleLine,
        maxLines = maxLines
    )
}