package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * CheckoutWave Data Model
 *
 */
@Parcelize
class CheckoutWave private constructor(
    private val id: Long?,
    private val active: Boolean, // Whether this CheckoutWave is active or not
   ) : Parcelable {

    fun id() = this.id
    fun active() = this.active

    @Parcelize
    data class Builder(
        private var id: Long? = -1,
        private var active: Boolean = false,
    ) : Parcelable {
        fun id(id: Long?) = apply { this.id = id }

        fun active(active: Boolean) =
            apply { this.active = active }

        fun build() = CheckoutWave(
            id = id,
            active = active,
        )
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    fun toBuilder() =
        Builder(id = id, active = active)

    override fun equals(other: Any?): Boolean =
        if (other is CheckoutWave) {
            other.id == id && other.active == active
        } else false
}