package com.kickstarter.libs.graphql

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter

class EmailAdapter : Adapter<String> {

    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): String {
        TODO("Not yet implemented")
    }

    override fun toJson(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        value: String
    ) {
        TODO("Not yet implemented")
    }
}