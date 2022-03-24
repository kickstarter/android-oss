package com.kickstarter.services.apiresponses

import android.os.Parcelable
import com.kickstarter.models.ShippingRule
import kotlinx.parcelize.Parcelize

@Parcelize
class ShippingRulesEnvelope private constructor(
    private val shippingRules: List<ShippingRule>
) : Parcelable {
    fun shippingRules() = this.shippingRules

    @Parcelize
    data class Builder(
        private var shippingRules: List<ShippingRule> = emptyList()
    ) : Parcelable {
        fun shippingRules(shippingRules: List<ShippingRule>) = apply { this.shippingRules = shippingRules }
        fun build() = ShippingRulesEnvelope(
            shippingRules = shippingRules
        )
    }

    fun toBuilder() = Builder(
        shippingRules = shippingRules
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is ShippingRulesEnvelope) {
            equals = shippingRules() == obj.shippingRules()
        }
        return equals
    }
}
