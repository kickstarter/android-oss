package com.kickstarter.models

import android.os.Parcelable
import auto.parcel.AutoParcel

@AutoParcel
abstract class CreatorDetails : Parcelable {
    abstract fun backingsCount(): Int
    abstract fun launchedProjectsCount(): Int
    abstract fun name(): String

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun backingsCount(id: Int): Builder
        abstract fun launchedProjectsCount(id: Int): Builder
        abstract fun name(name: String): Builder
        abstract fun build(): CreatorDetails
    }

    abstract fun toBuilder(): Builder

    companion object {

        fun builder(): Builder {
            return AutoParcel_CreatorDetails.Builder()
        }
    }

}
