package com.kickstarter.models

import android.os.Parcelable
import auto.parcel.AutoParcel
import org.joda.time.DateTime

@AutoParcel
abstract class ErroredBacking : Parcelable {
    abstract fun project(): Project

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun project(project: Project): Builder
        abstract fun build(): ErroredBacking
    }

    abstract fun toBuilder(): Builder

    companion object {

        fun builder(): Builder {
            return AutoParcel_ErroredBacking.Builder()
        }
    }

    @AutoParcel
    abstract class Project : Parcelable {

        abstract fun finalCollectionDate(): DateTime
        abstract fun name(): String
        abstract fun slug(): String

        @AutoParcel.Builder
        abstract class Builder {
            abstract fun finalCollectionDate(finalCollectionDate: DateTime?): Builder
            abstract fun name(name: String?): Builder
            abstract fun slug(slug: String?): Builder
            abstract fun build(): Project
        }

        abstract fun toBuilder(): Builder

        companion object {

            fun builder(): Builder {
                return AutoParcel_ErroredBacking_Project.Builder()
            }
        }

    }

}
