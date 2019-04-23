package com.kickstarter.services.apiresponses

import android.os.Parcelable
import auto.parcel.AutoParcel
import com.kickstarter.libs.qualifiers.AutoGson
import com.kickstarter.models.ShippingRule

@AutoGson
@AutoParcel
abstract class ShippingRulesEnvelope : Parcelable {
    abstract fun shippingRules(): List<ShippingRule>

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun shippingRules(rules: List<ShippingRule>): Builder
        abstract fun build(): ShippingRulesEnvelope
    }

    fun builder(): Builder {
        return AutoParcel_ShippingRulesEnvelope(shippingRules()).builder()
    }
}