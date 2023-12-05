package com.awning.afterglow.ui.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.awning.afterglow.request.waterfall.Waterfall
import kotlinx.coroutines.flow.catch


/**
 * 网络图片
 * @param url 图片 url
 * @param modifier Modifier
 */
@Composable
fun NetworkImage(url: String, modifier: Modifier = Modifier) {
    var image by remember { mutableStateOf<Bitmap?>(null) }

    // 加载图片
    LaunchedEffect(key1 = Unit) {
        Waterfall.getBitmap(url).catch {}.collect {
            image = it
        }
    }

    Box(modifier = modifier) {
        image?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Image", modifier = Modifier.fillMaxSize())
        }
    }
}