package de.flapdoodle.photosync.ui.io

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigDecimal
import java.util.*

object UUIDAdapter {
    @FromJson
    fun fromJson(string: String) = UUID.fromString(string)

    @ToJson
    fun toJson(value: UUID) = value.toString()
}