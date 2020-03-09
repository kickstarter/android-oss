package com.kickstarter.models

import android.os.Parcelable
import auto.parcel.AutoParcel

@AutoParcel
abstract class CreatorDetails : Parcelable {
    abstract fun backingsCount(): Int
    abstract fun launchedProjectsCount(): Int

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun backingsCount(backingsCount: Int): Builder
        abstract fun launchedProjectsCount(launchedProjectsCount: Int): Builder
        abstract fun build(): CreatorDetails
    }

    abstract fun toBuilder(): Builder

    companion object {

        fun builder(): Builder {
            return AutoParcel_CreatorDetails.Builder()
        }
    }

}
