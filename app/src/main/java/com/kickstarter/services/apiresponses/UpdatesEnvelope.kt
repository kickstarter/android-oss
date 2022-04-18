package com.kickstarter.services.apiresponses

import android.os.Parcelable
import com.kickstarter.models.Update
import kotlinx.parcelize.Parcelize

@Parcelize
class UpdatesEnvelope private constructor(
    private val updates: List<Update>,
    private val urls: UrlsEnvelope
) : Parcelable {
    fun updates() = this.updates
    fun urls() = this.urls

    @Parcelize
    data class Builder(
        private var updates: List<Update> = emptyList(),
        private var urls: UrlsEnvelope = UrlsEnvelope.builder().build()
    ) : Parcelable {
        fun updates(updates: List<Update>) = apply { this.updates = updates }
        fun urls(urls: UrlsEnvelope) = apply { this.urls = urls }
        fun build() = UpdatesEnvelope(
            updates = updates,
            urls = urls
        )
    }

    fun toBuilder() = Builder(
        updates = updates,
        urls = urls
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is UpdatesEnvelope) {
            equals = updates() == other.updates() &&
                urls() == other.urls()
        }
        return equals
    }

    @Parcelize
    class UrlsEnvelope private constructor(
        private val api: ApiEnvelope
    ) : Parcelable {
        fun api() = this.api

        @Parcelize
        data class Builder(
            private var api: ApiEnvelope = ApiEnvelope.builder().build()
        ) : Parcelable {
            fun api(api: ApiEnvelope) = apply { this.api = api }
            fun build() = UrlsEnvelope(
                api = api
            )
        }

        fun toBuilder() = Builder(
            api = api
        )

        companion object {
            @JvmStatic
            fun builder() = Builder()
        }

        override fun equals(other: Any?): Boolean {
            var equals = super.equals(other)
            if (other is UrlsEnvelope) {
                equals = api() == other.api()
            }
            return equals
        }

        @Parcelize
        class ApiEnvelope private constructor(
            private val moreUpdates: String
        ) : Parcelable {
            fun moreUpdates() = this.moreUpdates

            @Parcelize
            data class Builder(
                private var moreUpdates: String = ""
            ) : Parcelable {
                fun moreUpdates(moreUpdates: String?) = apply { this.moreUpdates = moreUpdates ?: "" }
                fun build() = ApiEnvelope(
                    moreUpdates = moreUpdates
                )
            }

            fun toBuilder() = Builder(
                moreUpdates = moreUpdates
            )

            companion object {
                @JvmStatic
                fun builder() = Builder()
            }

            override fun equals(other: Any?): Boolean {
                var equals = super.equals(other)
                if (other is ApiEnvelope) {
                    equals = moreUpdates() == other.moreUpdates()
                }
                return equals
            }
        }
    }
}
