package com.awning.afterglow.ui.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


/**
 * PDF 显示
 * @receiver [LazyListScope]
 * @param list PDF 列表
 * @param modifier Modifier
 */
fun LazyListScope.pdfView(
    list: List<ImageBitmap>,
    modifier: Modifier = Modifier
) {
    items(items = list) {
        Image(
            bitmap = it,
            contentDescription = "PDF Content",
            contentScale = ContentScale.FillWidth,
            filterQuality = FilterQuality.High,
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
        )
    }
}


/**
 * 加载 PDF 内容
 * @param content
 * @param url 文件 url
 * @param fileName 文件名
 * @return [Flow]
 */
fun loadPDF(
    content: Context,
    url: String,
    fileName: String
) = flow {
    val file = File(content.cacheDir, "$fileName.pdf").apply {
        if (!exists()) {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(this)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.close()
            inputStream.close()
            connection.disconnect()
        }
    }

    val renderer = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))

    for (i in 0 until renderer.pageCount) {
        val page = renderer.openPage(i)

        val bitmap = Bitmap.createBitmap(page.width * 4, page.height * 4, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
        page.close()

        emit(bitmap.asImageBitmap())
    }
}.flowOn(Dispatchers.IO)