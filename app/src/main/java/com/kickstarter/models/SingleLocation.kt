package com.kickstarter.models

import android.os.Parcelable
import auto.parcel.AutoParcel
import com.kickstarter.libs.qualifiers.AutoGson

@AutoParcel
@AutoGson
abstract class SingleLocation : Parcelable {
    abstract fun id(): Long
    abstract fun localizedName(): String?

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun id(id: Long): Builder?
        abstract fun localizedName(localizedName: String?): Builder?
        abstract fun build(): SingleLocation?
    }

    abstract fun toBuilder(): Builder?

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return AutoParcel_SingleLocation.Builder()
        }
    }
}
