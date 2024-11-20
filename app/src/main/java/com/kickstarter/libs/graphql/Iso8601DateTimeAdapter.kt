package com.kickstarter.libs.graphql

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import org.joda.time.DateTime

class Iso8601DateTimeAdapter : Adapter<DateTime> {

    override fun fromJson(
        reader: JsonReader,
        customScalarAdapters: CustomScalarAdapters
    ): DateTime {
        TODO("Not yet implemented")
    }

    override fun toJson(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        value: DateTime
    ) {
        TODO("Not yet implemented")
    }
}
