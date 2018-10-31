package com.kickstarter.libs.graphql

import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateAdapter: CustomTypeAdapter<Date> {

    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd")

    override fun encode(value: Date): CustomTypeValue<*> {
        return CustomTypeValue.GraphQLString(DATE_FORMAT.format(value))
    }

    override fun decode(value: CustomTypeValue<*>): Date {
        try {
            return DATE_FORMAT.parse(value.value.toString())
        } catch (exception: ParseException) {
            throw RuntimeException(exception)
        }
    }

}