package com.github.kittinunf.fuse.sample

import com.github.kittinunf.fuse.core.Fuse
import org.json.JSONObject

data class LocalTime(
    val utcDateTime: String,
    val timezone: String,
    val dateTime: String,
    val abbrev: String
) {

    companion object {
        fun fromJson(content: String): LocalTime {
            return JSONObject(content).let {
                val utcDateTime = it.getString("utc_datetime")
                val timezone = it.getString("timezone")
                val dateTime = it.getString("datetime")
                val abbrev = it.getString("abbreviation")

                LocalTime(utcDateTime, timezone, dateTime, abbrev)
            }
        }

        val deserializer = object : Fuse.DataConvertible<LocalTime> {
            override fun convertFromData(bytes: ByteArray): LocalTime {
                return fromJson(String(bytes))
            }

            override fun convertToData(value: LocalTime): ByteArray {
                return value.toJson().toString(2).toByteArray()
            }
        }
    }

    fun toJson() = JSONObject().apply {
        put("utc_datetime", utcDateTime)
        put("timezone", timezone)
        put("datetime", dateTime)
        put("abbreviation", abbrev)
    }
}
