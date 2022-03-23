package com.kickstarter.services.apirequests

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class MessageBody private constructor(
    private val body: String?
) : Parcelable {
    fun body() = this.body

    @Parcelize
    data class Builder(
        private var body: String? = null
    ) : Parcelable {
        fun body(body: String?) = apply { this.body = body }
        fun build() = MessageBody(
            body = body
        )
    }

    fun toBuilder() = Builder(
        body = body
    )

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is MessageBody) {
            equals = body() == obj.body()
        }
        return equals
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
