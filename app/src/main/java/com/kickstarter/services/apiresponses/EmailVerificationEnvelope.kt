package com.kickstarter.services.apiresponses

import android.os.Parcelable
import auto.parcel.AutoParcel
import com.kickstarter.libs.qualifiers.AutoGson

@AutoGson
@AutoParcel
abstract class EmailVerificationEnvelope : Parcelable {
    abstract fun message(): String
    abstract fun code(): Int

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun message(message: String): Builder
        abstract fun code(code: Int): Builder
        abstract fun build(): EmailVerificationEnvelope
    }

    @Override
    override fun equals(other: Any?): Boolean {
        return if (other is EmailVerificationEnvelope) {
            other.code() == this.code() && other.message() == this.message()
        } else super.equals(other)
    }

    abstract fun toBuilder(): Builder

    companion object {
        fun builder(): Builder {
            return AutoParcel_EmailVerificationEnvelope.Builder()
        }
    }
}
