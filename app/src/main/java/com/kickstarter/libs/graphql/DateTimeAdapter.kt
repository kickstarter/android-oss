package com.kickstarter.libs.graphql

import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.text.ParseException

class DateTimeAdapter : CustomTypeAdapter<DateTime> {

    override fun encode(value: DateTime): CustomTypeValue<*> {
        return CustomTypeValue.GraphQLNumber(value.toString().toLong() / 1000L)
    }

    override fun decode(value: CustomTypeValue<*>): DateTime {
        try {
            return DateTime(java.lang.Long.valueOf(value.value.toString().toLong() * 1000L), DateTimeZone.UTC)
        } catch (exception: ParseException) {
            throw RuntimeException(exception)
        }
    }
}
