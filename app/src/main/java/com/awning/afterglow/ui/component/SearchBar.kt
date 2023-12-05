package com.awning.afterglow.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.awning.afterglow.toolkit.Trigger


/**
 * 搜索框
 * @param modifier Modifier
 * @param value 值
 * @param onValueChange 值改变回调
 * @param onSearch 搜索回调
 */
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (value: String) -> Unit,
    placeholder: @Composable() (() -> Unit)? = null,
    onSearch: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val trigger = remember { Trigger() }

    Row(modifier = modifier.padding(8.dp)) {
        AfterglowTextFiled(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(
                    onClick = {
                        onSearch()
                        focusManager.clearFocus()
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    trigger.touch {
                        onSearch()
                        focusManager.clearFocus()
                    }
                }
            ),
            singleLine = true
        )
    }
}