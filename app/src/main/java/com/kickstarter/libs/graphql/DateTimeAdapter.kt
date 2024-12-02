package com.kickstarter.libs.graphql
import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.text.ParseException

class DateTimeAdapter : Adapter<DateTime> {

    override fun fromJson(
        reader: JsonReader,
        customScalarAdapters: CustomScalarAdapters
    ): DateTime {
        try {
            return DateTime(java.lang.Long.valueOf(reader.nextString()!!.toLong() * 1000L), DateTimeZone.UTC)
        } catch (exception: ParseException) {
            throw RuntimeException(exception)
        }
    }

    override fun toJson(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        value: DateTime
    ) {
        writer.value(value.toString().toLong() / 1000L)
    }
}
