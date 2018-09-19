package com.kickstarter.libs.graphql

import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue

class EmailAdapter : CustomTypeAdapter<String> {
    override fun encode(value: String): CustomTypeValue<*> {
        return CustomTypeValue.GraphQLString(value)
    }

    override fun decode(value: CustomTypeValue<*>): String {
        return value.toString()
    }
}