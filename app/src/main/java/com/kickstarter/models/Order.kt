package com.kickstarter.models

import android.os.Parcelable
import com.kickstarter.type.CurrencyCode
import kotlinx.parcelize.Parcelize

/**
 * Order Data Model
 *
 */
@Parcelize
class Order private constructor(
    private val id: String,
    private val checkoutState: CheckoutStateEnum, // The checkout state of the order
    private val currency: CurrencyCode, // The currency of the order
    private val total: Int? // The total cost for the order including taxes and shipping
) : Parcelable {

    fun id() = this.id
    fun currency() = this.currency
    fun total() = this.total

    @Parcelize
    data class Builder(
        private var id: String = "",
        private var checkoutState: CheckoutStateEnum = CheckoutStateEnum.NOT_STARTED,
        private var currency: CurrencyCode = CurrencyCode.UNKNOWN__,
        private var total: Int? = null
    ) : Parcelable {
        fun id(id: String?) = apply { this.id = id ?: "" }
        fun checkoutState(checkoutState: CheckoutStateEnum) = apply { this.checkoutState = checkoutState }
        fun currency(currency: CurrencyCode?) = apply { this.currency = currency ?: CurrencyCode.UNKNOWN__ }
        fun total(total: Int?) = apply { this.total = total }
        fun build() = Order(id = id, checkoutState = checkoutState, currency = currency, total = total)
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(id = id, currency = currency, total = total)

    override fun equals(other: Any?): Boolean =
        if (other is Order) {
            other.id == this.id && other.checkoutState == this.checkoutState && other.currency == this.currency && other.total == this.total
        } else false

    enum class CheckoutStateEnum(private val type: String) {
        COMPLETE("complete"),
        IN_PROGRESS("in_progress"),
        NOT_STARTED("not_started"),
    }
}
