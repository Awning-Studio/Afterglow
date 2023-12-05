package com.awning.afterglow.toolkit

import android.graphics.Bitmap
import android.graphics.Color
import com.awning.afterglow.AssetManager
import com.awning.afterglow.FileDir
import com.awning.afterglow.module.edusystem.EduSystemUtil
import com.awning.afterglow.module.edusystem.TitleMatcher
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream

/**
 * 光学文字识别
 */
object OCR {
    private var isInit = false

    // 数据文件文件名
    private const val FILENAME = "eng.traineddata"

    // 数据文件存储路径
    private val dataPath = "${FileDir}/tessdata/"
    private var tessBaseAPI = TessBaseAPI()


    /**
     * 识别教务系统验证码
     * @param captcha 验证码
     * @return [Flow]
     */
    fun recognizeCaptcha(captcha: Bitmap) = flow {
        recognize(clearBorder(twoBinary(captcha))).collect {
            if (EduSystemUtil.checkCaptcha(it)) {
                emit(it)
            } else {
                throw Exception(TitleMatcher.MatchMessage.CaptchaError)
            }
        }
    }


    /**
     * 识别 [bitmap]
     * @param bitmap 待识别 [Bitmap]
     * @return [Flow]
     */
    private fun recognize(bitmap: Bitmap) = flow {
        if (!isInit) {
            val dataFile = File(dataPath, FILENAME)

            // 数据文件不存在
            if (!dataFile.exists()) {
                // 将训练数据文件写入dataPath
                val inputStream = AssetManager.open(FILENAME)
                File(dataPath).mkdirs()

                // 创建数据文件并写入
                if (dataFile.createNewFile()) {
                    val outputStream = FileOutputStream(dataFile)
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()
                }
            }

            // 初始化
            tessBaseAPI.init(FileDir, "eng")
            tessBaseAPI.pageSegMode = TessBaseAPI.PageSegMode.PSM_RAW_LINE
            isInit = true
        }

        // 识别图片
        tessBaseAPI.setImage(bitmap)

        emit(tessBaseAPI.utF8Text)
    }.flowOn(Dispatchers.IO)


    /**
     * 释放资源
     */
    fun release() {
        if (isInit) {
            tessBaseAPI.recycle()
            tessBaseAPI = TessBaseAPI()
            isInit = false
        }
    }


    /**
     * 对 [bitmap] 二值化处理
     * @param bitmap 待处理的 [Bitmap]
     * @return 处理后的 [Bitmap]
     */
    private fun twoBinary(bitmap: Bitmap): Bitmap {
        // 创建二值化图像
        val binaryBitmap: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        // 依次循环，对图像的像素进行处理
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                // 得到当前像素的值
                val pixel = binaryBitmap.getPixel(x, y)

                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                val gray = (red + green + blue) / 3

                val binaryPixel = if (gray >= 128) {
                    Color.rgb(255, 255, 255) // white
                } else {
                    Color.rgb(0, 0, 0) // black
                }

                // 设置新图像的当前像素值
                binaryBitmap.setPixel(x, y, binaryPixel)
            }
        }
        return binaryBitmap
    }


    /**
     * 为 [bitmap] 去除边框
     * @param bitmap 待处理的 [Bitmap]
     * @return 处理后的 [Bitmap]
     */
    private fun clearBorder(bitmap: Bitmap): Bitmap {
        val borderWidth = 2
        // 去除上下边框
        for (x in 0 until bitmap.width) {
            for (y in 0..borderWidth) {
                bitmap.setPixel(x, y, Color.WHITE)
                bitmap.setPixel(x, bitmap.height - y - 1, Color.WHITE)
            }
        }
        // 去除左右边框
        for (y in borderWidth until bitmap.height - borderWidth) {
            for (x in 0..borderWidth) {
                bitmap.setPixel(x, y, Color.WHITE)
                bitmap.setPixel(bitmap.width - x - 1, y, Color.WHITE)
            }
        }
        return bitmap
    }
}