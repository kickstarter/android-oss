package com.kickstarter.libs.graphql

import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import org.joda.time.DateTime
import java.text.ParseException

class Iso8601DateTimeAdapter: CustomTypeAdapter<DateTime> {

    override fun encode(value: DateTime): CustomTypeValue<*> {
        return CustomTypeValue.GraphQLString(value.toString())
    }

    override fun decode(value: CustomTypeValue<*>): DateTime {
        try {
            return DateTime.parse(value.value.toString())
        } catch (exception: ParseException) {
            throw RuntimeException(exception)
        }
    }
}
