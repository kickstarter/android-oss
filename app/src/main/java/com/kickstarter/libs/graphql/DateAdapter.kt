package com.kickstarter.libs.graphql

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import org.joda.time.Instant
import java.time.OffsetDateTime
import java.util.Date

class DateAdapter : Adapter<Date> {

    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Date {
        return Date(OffsetDateTime.parse(reader.nextString()!!).toInstant().toEpochMilli())
    }

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: Date) {
        writer.value(Instant.ofEpochMilli(value.time).toString())
    }
}
