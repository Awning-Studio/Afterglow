package com.awning.afterglow.ui.component

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle


@Composable
fun TitleText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text, style = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        ),
        modifier = modifier
    )
}

@Composable
fun TitleText(text: String, modifier: Modifier = Modifier, onClick: (Int) -> Unit) {
    ClickableText(
        text = buildAnnotatedString {
            withStyle(
                MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ).toSpanStyle()
            ) {
                append(text)
            }
        },
        onClick = onClick,
        modifier = modifier
    )
}