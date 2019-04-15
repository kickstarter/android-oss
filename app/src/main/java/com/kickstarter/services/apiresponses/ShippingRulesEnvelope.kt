package com.kickstarter.services.apiresponses

import android.os.Parcelable
import auto.parcel.AutoParcel
import com.kickstarter.libs.qualifiers.AutoGson
import com.kickstarter.models.ShippingRule

@AutoGson
@AutoParcel
abstract class ShippingRulesEnvelope : Parcelable {
    abstract fun shippingRules(): List<ShippingRule>
}