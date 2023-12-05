package com.awning.afterglow.toolkit

import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * 类型转换器
 */
object Transformer {
    /**
     * [String] 转 [HashMap]
     * @param json
     * @return [HashMap]
     */
    fun jsonToHashMap(json: String): HashMap<String, String> {
        val hashMap = HashMap<String, String>()
        val jsonObject = JSONObject(json)
        jsonObject.keys().forEach {
            hashMap[it] = jsonObject.getString(it)
        }
        return hashMap
    }


    /**
     * [Date] 转 [ZonedDateTime]
     * @param date
     * @return [ZonedDateTime]
     */
    private fun zonedDateTimeOf(date: Date) =
        date.toInstant().atZone(ZoneId.systemDefault())

    /**
     * [Date] 转 [LocalDateTime]
     * @param date
     * @return [LocalDateTime]
     */
    fun localDateTimeOf(date: Date): LocalDateTime = zonedDateTimeOf(date).toLocalDateTime()


    /**
     * [Date] 转 [LocalDate]
     * @param date Date
     * @return [LocalDate]
     */
    fun localDateOf(date: Date): LocalDate = zonedDateTimeOf(date).toLocalDate()


    /**
     * [LocalDate] 转时间戳
     * @param localDate
     * @return [Long]
     */
    fun timeStampOf(localDate: LocalDate) = localDate.toEpochDay() * 24 * 60 * 60 * 1000


    /**
     * [LocalDateTime] 转 [String]
     * @param localDateTime LocalDateTime
     * @return [String]
     */
    fun stringOf(localDateTime: LocalDateTime) =
        localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))


    /**
     * [Date] 转 [String]
     * @param date
     * @return [String]
     */
    fun stringOf(date: Date) = stringOf(localDateTimeOf(date))
}