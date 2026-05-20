package com.smartnode.app.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

/**
 * Room [TypeConverter]s that bridge Kotlin types to SQLite columns.
 *
 * Currently only [Map] of arbitrary scalar values <-> JSON string conversion is
 * defined because [com.smartnode.app.data.local.entity.IdentityEntity.jsonData]
 * is the only flexible column in the schema. The conversion is offered both as
 * a Room converter (in case a future entity wants to store the map directly)
 * and as plain helpers that domain mappers can call.
 */
class Converters {

    @TypeConverter
    fun mapToJson(map: Map<String, Any?>?): String? =
        map?.let { encodeMap(it) }

    @TypeConverter
    fun jsonToMap(json: String?): Map<String, Any?>? =
        json?.let { decodeMap(it) }

    companion object {
        private val JSON: Json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            isLenient = true
            coerceInputValues = true
        }

        fun encodeMap(map: Map<String, Any?>): String {
            val jsonMap = LinkedHashMap<String, kotlinx.serialization.json.JsonElement>(map.size)
            for ((k, v) in map) {
                jsonMap[k] = when (v) {
                    null -> JsonPrimitive(null as String?)
                    is String -> JsonPrimitive(v)
                    is Boolean -> JsonPrimitive(v)
                    is Number -> JsonPrimitive(v)
                    else -> JsonPrimitive(v.toString())
                }
            }
            return JSON.encodeToString(JsonObject.serializer(), JsonObject(jsonMap))
        }

        fun decodeMap(json: String): Map<String, Any?> {
            if (json.isBlank()) return emptyMap()
            val obj = JSON.parseToJsonElement(json).jsonObject
            val out = LinkedHashMap<String, Any?>(obj.size)
            for ((k, v) in obj) {
                out[k] = when (v) {
                    is JsonPrimitive -> when {
                        v.isString -> v.contentOrNull
                        v.booleanOrNull != null -> v.boolean
                        v.longOrNull != null -> v.long
                        v.intOrNull != null -> v.int
                        v.doubleOrNull != null -> v.double
                        else -> v.contentOrNull
                    }
                    else -> v.toString()
                }
            }
            return out
        }
    }
}
