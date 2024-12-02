package com.kickstarter.libs.graphql

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateAdapter : Adapter<Date> {

    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Date {
        return try {
            reader.nextString()?.let { DATE_FORMAT.parse(it) } ?: Date()
        } catch (exception: ParseException) {
            throw RuntimeException(exception)
        }
    }

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: Date) {
        writer.value(DATE_FORMAT.format(value))
    }
}
