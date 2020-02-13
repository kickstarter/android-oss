package com.kickstarter.models

import android.os.Parcelable
import auto.parcel.AutoParcel

@AutoParcel
abstract class Checkout : Parcelable {
    abstract fun id(): Long?
    abstract fun backing(): Backing

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun id(id: Long?): Builder
        abstract fun backing(backing: Backing): Builder
        abstract fun build(): Checkout
    }

    abstract fun toBuilder(): Builder

    companion object {

        fun builder(): Builder {
            return AutoParcel_Checkout.Builder()
        }
    }

    @AutoParcel
    abstract class Backing : Parcelable {

        abstract fun clientSecret(): String?
        abstract fun requiresAction(): Boolean

        @AutoParcel.Builder
        abstract class Builder {
            abstract fun clientSecret(secret: String?): Builder
            abstract fun requiresAction(requiresAction: Boolean): Builder
            abstract fun build(): Backing
        }

        abstract fun toBuilder(): Builder

        companion object {

            fun builder(): Builder {
                return AutoParcel_Checkout_Backing.Builder()
            }
        }

    }

}
