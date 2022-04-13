package com.kickstarter.services.apiresponses

import com.kickstarter.libs.qualifiers.AutoGson
import auto.parcel.AutoParcel
import android.os.Parcelable
import com.kickstarter.models.Update
import com.kickstarter.services.apiresponses.UpdatesEnvelope
import com.kickstarter.services.apiresponses.AutoParcel_UpdatesEnvelope_UrlsEnvelope_ApiEnvelope
import com.kickstarter.services.apiresponses.AutoParcel_UpdatesEnvelope_UrlsEnvelope
import com.kickstarter.services.apiresponses.AutoParcel_UpdatesEnvelope
import kotlinx.parcelize.Parcelize

@Parcelize
class UpdatesEnvelope internal constructor(
    private val updates: List<Update>,
    private val urls: UrlsEnvelope
): Parcelable {
    fun updates() = this.updates
    fun urls() = this.urls

    @Parcelize
    data class Builder(
        private var updates: List<Update> = emptyList(),
        private var urls: UrlsEnvelope = UrlsEnvelope.builder().build()
    ) : Parcelable {
        fun updates(updates: List<Update>?) = apply { this.updates = updates }
        fun urls(urls: UrlsEnvelope) = apply { this.urls = urls }
        fun build(): UpdatesEnvelope?
    }

    abstract fun toBuilder(): Builder?

    @AutoGson
    @AutoParcel
    abstract class UrlsEnvelope : Parcelable {
        abstract fun api(): ApiEnvelope?

        @AutoParcel.Builder
        abstract class Builder {
            abstract fun api(__: ApiEnvelope?): Builder?
            abstract fun build(): UrlsEnvelope?
        }

        abstract fun toBuilder(): Builder?

        @AutoGson
        @AutoParcel
        abstract class ApiEnvelope : Parcelable {
            abstract fun moreUpdates(): String?

            @AutoParcel.Builder
            abstract class Builder {
                abstract fun moreUpdates(__: String?): Builder?
                abstract fun build(): ApiEnvelope?
            }

            companion object {
                @JvmStatic
                fun builder(): Builder {
                    return AutoParcel_UpdatesEnvelope_UrlsEnvelope_ApiEnvelope.Builder()
                }
            }
        }

        companion object {
            @JvmStatic
            fun builder(): Builder {
                return AutoParcel_UpdatesEnvelope_UrlsEnvelope.Builder()
            }
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return AutoParcel_UpdatesEnvelope.Builder()
        }
    }
}