package com.kickstarter.services.apiresponses

import android.os.Parcelable
import com.kickstarter.models.Activity
import kotlinx.parcelize.Parcelize

@Parcelize
class ActivityEnvelope private constructor(
    private val activities: List<Activity>,
    private val urls: UrlsEnvelope
) : Parcelable {
    fun activities() = this.activities
    fun urls() = this.urls
    @Parcelize
    data class Builder(
        private var activities: List<Activity> = emptyList(),
        private var urls: UrlsEnvelope = UrlsEnvelope.builder().build()
    ) : Parcelable {
        fun activities(activities: List<Activity>) = apply { this.activities = activities }
        fun urls(urls: UrlsEnvelope) = apply { this.urls = urls }
        fun build() = ActivityEnvelope(
            activities = activities,
            urls = urls
        )
    }

    fun toBuilder() = Builder(
        activities = activities,
        urls = urls
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is ActivityEnvelope) {
            equals = activities() == obj.activities() &&
                urls() == obj.urls()
        }
        return equals
    }
    @Parcelize
    class UrlsEnvelope private constructor(
        private val api: ApiEnvelope
    ) :
        Parcelable {
        fun api() = this.api

        @Parcelize
        class ApiEnvelope private constructor(
            private val moreActivities: String,
            private val newerActivities: String?
        ) : Parcelable {
            fun moreActivities() = this.moreActivities
            fun newerActivities() = this.newerActivities

            @Parcelize
            data class Builder(
                private var moreActivities: String = "",
                private var newerActivities: String? = null
            ) : Parcelable {
                fun moreActivities(moreActivities: String) = apply { this.moreActivities = moreActivities }
                fun newerActivities(newerActivities: String?) = apply { this.newerActivities = newerActivities }
                fun build() = ApiEnvelope(
                    moreActivities = moreActivities,
                    newerActivities = newerActivities,
                )
            }
            override fun equals(obj: Any?): Boolean {
                var equals = super.equals(obj)
                if (obj is ApiEnvelope) {
                    equals = moreActivities() == obj.moreActivities() &&
                        newerActivities() == obj.newerActivities()
                }
                return equals
            }

            fun toBuilder() = Builder(
                moreActivities = moreActivities,
                newerActivities = newerActivities
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
