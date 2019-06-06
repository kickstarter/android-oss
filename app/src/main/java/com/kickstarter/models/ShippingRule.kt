package com.kickstarter.models

import android.os.Parcelable
import androidx.annotation.Nullable
import auto.parcel.AutoParcel
import com.kickstarter.libs.qualifiers.AutoGson

@AutoGson
@AutoParcel
abstract class ShippingRule : Parcelable {
    @Nullable abstract fun  id(): Long?
    abstract fun cost(): Double
    abstract fun location(): Location

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun id(id: Long?): Builder
        abstract fun cost(cost: Double): Builder
        abstract fun location(location: Location): Builder
        abstract fun build(): ShippingRule
    }

    override fun toString(): String {
        return location().displayableName()
    }

    abstract fun toBuilder(): Builder

    companion object {

        fun builder(): Builder {
            return AutoParcel_ShippingRule.Builder()
        }
    }

}
