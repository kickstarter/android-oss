package com.kickstarter.services.apiresponses

import android.os.Parcelable
import com.kickstarter.models.MessageThread
import kotlinx.parcelize.Parcelize

@Parcelize
class MessageThreadsEnvelope private constructor(
    private val messageThreads: List<MessageThread>,
    private val urls: UrlsEnvelope
) : Parcelable {
    fun messageThreads() = this.messageThreads
    fun urls() = this.urls

    @Parcelize
    data class Builder(
        private var messageThreads: List<MessageThread> = emptyList(),
        private var urls: UrlsEnvelope = UrlsEnvelope.builder().build()
    ) : Parcelable {
        fun messageThreads(messageThreads: List<MessageThread>) = apply { this.messageThreads = messageThreads }
        fun urls(urls: UrlsEnvelope) = apply { this.urls = urls }
        fun build() = MessageThreadsEnvelope(
            messageThreads = messageThreads,
            urls = urls
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is MessageThreadsEnvelope) {
            equals =
                messageThreads() == obj.messageThreads() &&
                urls() == obj.urls()
        }
        return equals
    }

    fun toBuilder() = Builder(
        messageThreads = messageThreads,
        urls = urls
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    @Parcelize
    class UrlsEnvelope private constructor(
        private val api: ApiEnvelope
    ) :
        Parcelable {
        fun api() = this.api

        @Parcelize
        class ApiEnvelope private constructor(
            private val moreMessageThreads: String
        ) : Parcelable {
            fun moreMessageThreads() = this.moreMessageThreads

            @Parcelize
            data class Builder(
                private var moreMessageThreads: String = ""
            ) : Parcelable {
                fun moreMessageThreads(moreMessageThreads: String) = apply { this.moreMessageThreads = moreMessageThreads }
                fun build() = ApiEnvelope(
                    moreMessageThreads = moreMessageThreads
                )
            }
            override fun equals(obj: Any?): Boolean {
                var equals = super.equals(obj)
                if (obj is ApiEnvelope) {
                    equals = moreMessageThreads() == obj.moreMessageThreads()
                }
                return equals
            }

            fun toBuilder() = Builder(
                moreMessageThreads = moreMessageThreads
            )

            companion object {
                @JvmStatic
                fun builder() = Builder()
            }
        }

        @Parcelize
        data class Builder(
            private var api: ApiEnvelope = ApiEnvelope.builder().build()
        ) : Parcelable {
            fun api(api: ApiEnvelope) = apply { this.api = api }
            fun build() = UrlsEnvelope(
                api = api
            )
        }

        override fun equals(obj: Any?): Boolean {
            var equals = super.equals(obj)
            if (obj is UrlsEnvelope) {
                equals = api() == obj.api()
            }
            return equals
        }

        fun toBuilder() = Builder(
            api = api
        )

        companion object {
            @JvmStatic
            fun builder() = Builder()
        }
    }
}
