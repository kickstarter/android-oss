package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
class PaymentSource private constructor(
    private val id: String,
    private val paymentType: String,
    private val state: String,
    private val type: String?,
    private val lastFour: String?,
    private val expirationDate: Date?
) : Parcelable {
    fun id() = this.id
    fun paymentType() = this.paymentType
    fun state() = this.state
    fun type() = this.type
    fun lastFour() = this.lastFour
    fun expirationDate() = this.expirationDate

    @Parcelize
    data class Builder(
        private var id: String = "",
        private var paymentType: String = "",
        private var state: String = "",
        private var type: String? = "",
        private var lastFour: String? = "",
        private var expirationDate: Date? = null
    ) : Parcelable {
        fun id(id: String?) = apply { this.id = id ?: "" }
        fun paymentType(type: String?) = apply { this.paymentType = type ?: "" }
        fun state(state: String?) = apply { this.state = state ?: "" }
        fun type(type: String?) = apply { this.type = type ?: "" }
        fun lastFour(lastFour: String?) = apply { this.lastFour = lastFour }
        fun expirationDate(expirationDate: Date?) = apply { this.expirationDate = expirationDate }
        fun build() = PaymentSource(
            id = id,
            paymentType = paymentType,
            state = state,
            type = type,
            lastFour = lastFour,
            expirationDate = expirationDate
        )
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        id = id,
        paymentType = paymentType,
        state = state,
        type = type,
        lastFour = lastFour,
        expirationDate = expirationDate
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is PaymentSource) {
            equals = this.id() == other.id() &&
                this.paymentType() == other.paymentType()
            this.state() == other.state() &&
                this.type() == other.type() &&
                this.lastFour() == other.lastFour() &&
                this.expirationDate() == other.expirationDate()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
