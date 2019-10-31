package com.github.kittinunf.fuse.sample

import com.github.kittinunf.fuel.core.ResponseDeserializable
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

        val deserializer = object : ResponseDeserializable<LocalTime> {
            override fun deserialize(content: String): LocalTime? {
                return fromJson(content)
            }
        }
    }
}
