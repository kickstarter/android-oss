package com.kickstarter.models

import android.os.Parcelable
import androidx.annotation.StringDef
import com.kickstarter.libs.utils.extensions.isZero
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

@Parcelize
class Reward private constructor(
    private val backersCount: Int?,
    private val convertedMinimum: Double,
    private val description: String?,
    private val endsAt: DateTime?,
    private val id: Long,
    private val limit: Int?,
    private val minimum: Double,
    private val pledgeAmount: Double,
    private val latePledgeAmount: Double,
    private val estimatedDeliveryOn: DateTime?,
    private val remaining: Int?,
    private val rewardsItems: List<RewardsItem>?,
    private val shippingPreference: String?,
    private val shippingSingleLocation: SingleLocation?,
    @ShippingType
    private val shippingType: String?,
    private val title: String?,
    private val isAddOn: Boolean,
    private val addOnsItems: List<RewardsItem>?,
    private val quantity: Int?,
    private val hasAddons: Boolean,
    private val startsAt: DateTime?,
    /**
     * This field will be available just for GraphQL, in V1 it would be null
     * A Reward is available when:
     * - Limit has not been reached
     * - ExpireData has not been reached
     *
     * @return true is the Reward is available
     */
    private val isAvailable: Boolean,
    /**
     * this field will be available just for GraphQL, in V1 it would be empty
     */
    private val shippingPreferenceType: ShippingPreference?,
    /**
     * this field will be available just for GraphQL, in V1 it would be empty
     */
    private val shippingRules: List<ShippingRule>?,
    /**
     * field reflecting the local pickup location, available only at GraphQL, in V1 it would be empty
     */
    private val localReceiptLocation: Location?
) : Parcelable, Relay {
    fun backersCount() = this.backersCount
    fun convertedMinimum() = this.convertedMinimum
    fun description() = this.description
    fun endsAt() = this.endsAt
    override fun id() = this.id
    fun limit() = this.limit
    fun minimum() = this.minimum
    fun pledgeAmount() = this.pledgeAmount
    fun latePledgeAmount() = this.latePledgeAmount
    fun estimatedDeliveryOn() = this.estimatedDeliveryOn
    fun remaining() = this.remaining
    fun rewardsItems() = this.rewardsItems
    fun shippingPreference() = this.shippingPreference
    fun shippingSingleLocation() = this.shippingSingleLocation
    fun shippingType() = this.shippingType
    fun title() = this.title
    fun isAddOn() = this.isAddOn
    fun addOnsItems() = this.addOnsItems
    fun quantity() = this.quantity
    fun hasAddons() = this.hasAddons
    fun startsAt() = this.startsAt
    fun isAvailable() = this.isAvailable
    fun shippingPreferenceType() = this.shippingPreferenceType
    fun shippingRules() = this.shippingRules
    fun isAllGone() = remaining().isZero()
    fun isLimited() = limit() != null && !isAllGone()
    fun localReceiptLocation() = this.localReceiptLocation

    @Parcelize
    data class Builder(
        private var backersCount: Int? = null,
        private var convertedMinimum: Double = 0.0,
        private var description: String? = null,
        private var endsAt: DateTime? = null,
        private var id: Long = 0L,
        private var limit: Int? = null,
        private var minimum: Double = 0.0,
        private var pledgeAmount: Double = 0.0,
        private var latePledgeAmount: Double = 0.0,
        private var estimatedDeliveryOn: DateTime? = null,
        private var remaining: Int? = null,
        private var rewardsItems: List<RewardsItem>? = emptyList(),
        private var shippingPreference: String? = null,
        private var shippingSingleLocation: SingleLocation? = null,
        private var shippingType: String? = null,
        private var title: String? = null,
        private var isAddOn: Boolean = false,
        private var addOnsItems: List<RewardsItem>? = emptyList(),
        private var quantity: Int? = null,
        private var hasAddons: Boolean = false,
        private var startsAt: DateTime? = null,
        private var isAvailable: Boolean = false,
        private var shippingPreferenceType: ShippingPreference? = null,
        private var shippingRules: List<ShippingRule>? = null,
        private var localReceiptLocation: Location? = null
    ) : Parcelable {
        fun backersCount(backersCount: Int?) = apply { this.backersCount = backersCount }
        fun convertedMinimum(convertedMinimum: Double?) = apply { this.convertedMinimum = convertedMinimum ?: 0.0 }
        fun description(description: String?) = apply { this.description = description }
        fun endsAt(endsAt: DateTime?) = apply { this.endsAt = endsAt }
        fun startsAt(startsAt: DateTime?) = apply { this.startsAt = startsAt }
        fun id(id: Long?) = apply { this.id = id ?: -1L }
        fun limit(limit: Int?) = apply { this.limit = limit }
        fun minimum(minimum: Double?) = apply { this.minimum = minimum ?: 0.0 }
        fun pledgeAmount(pledgeAmount: Double?) = apply { this.pledgeAmount = pledgeAmount ?: 0.0 }
        fun latePledgeAmount(latePledgeAmount: Double?) = apply { this.latePledgeAmount = latePledgeAmount ?: 0.0 }
        fun estimatedDeliveryOn(estimatedDeliveryOn: DateTime?) = apply {
            this.estimatedDeliveryOn = estimatedDeliveryOn
        }
        fun remaining(remaining: Int?) = apply { this.remaining = remaining }
        fun rewardsItems(rewardsItems: List<RewardsItem>?) = apply { this.rewardsItems = rewardsItems ?: emptyList() }
        fun shippingPreference(shippingPreference: String?) = apply { this.shippingPreference = shippingPreference }
        fun shippingSingleLocation(shippingSingleLocation: SingleLocation?) = apply { this.shippingSingleLocation = shippingSingleLocation }
        fun shippingType(shippingType: String?) = apply { this.shippingType = shippingType }
        fun title(title: String?) = apply { this.title = title }
        fun isAddOn(isAddOn: Boolean?) = apply { this.isAddOn = isAddOn ?: false }
        fun addOnsItems(addOnsItems: List<RewardsItem>?) = apply { this.addOnsItems = addOnsItems ?: emptyList() }
        fun quantity(quantity: Int?) = apply { this.quantity = quantity }
        fun hasAddons(hasAddons: Boolean?) = apply { this.hasAddons = hasAddons ?: false }
        fun shippingRules(shippingRules: List<ShippingRule>?) = apply { this.shippingRules = shippingRules }
        fun shippingPreferenceType(shippingPreferenceType: ShippingPreference?) = apply { this.shippingPreferenceType = shippingPreferenceType }
        fun isAvailable(isAvailable: Boolean?) = apply { this.isAvailable = isAvailable ?: false }
        fun localReceiptLocation(localReceiptLocation: Location?) = apply { this.localReceiptLocation = localReceiptLocation }
        fun build() = Reward(
            backersCount = backersCount,
            convertedMinimum = convertedMinimum,
            description = description,
            endsAt = endsAt,
            startsAt = startsAt,
            id = id,
            limit = limit,
            minimum = minimum,
            pledgeAmount = pledgeAmount,
            latePledgeAmount = latePledgeAmount,
            estimatedDeliveryOn = estimatedDeliveryOn,
            remaining = remaining,
            rewardsItems = rewardsItems,
            shippingPreference = shippingPreference,
            shippingSingleLocation = shippingSingleLocation,
            shippingType = shippingType,
            title = title,
            isAddOn = isAddOn,
            addOnsItems = addOnsItems,
            quantity = quantity,
            hasAddons = hasAddons,
            shippingRules = shippingRules,
            shippingPreferenceType = shippingPreferenceType,
            isAvailable = isAvailable,
            localReceiptLocation = localReceiptLocation
        )
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
        const val SHIPPING_TYPE_ANYWHERE = "anywhere"
        const val SHIPPING_TYPE_MULTIPLE_LOCATIONS = "multiple_locations"
        const val SHIPPING_TYPE_NO_SHIPPING = "no_shipping"
        const val SHIPPING_TYPE_SINGLE_LOCATION = "single_location"
        const val SHIPPING_TYPE_LOCAL_PICKUP = "local"
    }

    fun toBuilder() = Builder(
        backersCount = backersCount,
        convertedMinimum = convertedMinimum,
        description = description,
        endsAt = endsAt,
        startsAt = startsAt,
        id = id,
        limit = limit,
        minimum = minimum,
        pledgeAmount = pledgeAmount,
        latePledgeAmount = latePledgeAmount,
        estimatedDeliveryOn = estimatedDeliveryOn,
        remaining = remaining,
        rewardsItems = rewardsItems,
        shippingPreference = shippingPreference,
        shippingSingleLocation = shippingSingleLocation,
        shippingType = shippingType,
        title = title,
        isAddOn = isAddOn,
        addOnsItems = addOnsItems,
        quantity = quantity,
        hasAddons = hasAddons,
        shippingRules = shippingRules,
        shippingPreferenceType = shippingPreferenceType,
        isAvailable = isAvailable,
        localReceiptLocation = localReceiptLocation
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Reward) {
            equals = backersCount() == other.backersCount() &&
                convertedMinimum() == other.convertedMinimum() &&
                description() == other.description() &&
                endsAt() == other.endsAt() &&
                startsAt() == other.startsAt() &&
                id() == other.id() &&
                limit() == other.limit() &&
                minimum() == other.minimum() &&
                pledgeAmount() == other.pledgeAmount() &&
                latePledgeAmount() == other.latePledgeAmount() &&
                estimatedDeliveryOn() == other.estimatedDeliveryOn() &&
                remaining() == other.remaining() &&
                rewardsItems() == other.rewardsItems() &&
                shippingPreference() == other.shippingPreference() &&
                shippingSingleLocation() == other.shippingSingleLocation() &&
                shippingType() == other.shippingType() &&
                title() == other.title() &&
                isAddOn() == other.isAddOn() &&
                addOnsItems() == other.addOnsItems() &&
                quantity() == other.quantity() &&
                hasAddons() == other.hasAddons() &&
                shippingRules() == other.shippingRules() &&
                shippingPreferenceType() == other.shippingPreferenceType() &&
                isAvailable() == other.isAvailable() &&
                localReceiptLocation() == other.localReceiptLocation()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(
        SHIPPING_TYPE_ANYWHERE,
        SHIPPING_TYPE_MULTIPLE_LOCATIONS,
        SHIPPING_TYPE_NO_SHIPPING,
        SHIPPING_TYPE_SINGLE_LOCATION
    )
    annotation class ShippingType

    enum class ShippingPreference(private val type: String) {
        NONE("none"),
        RESTRICTED("restricted"),
        UNRESTRICTED("unrestricted"),
        NOSHIPPING(
            SHIPPING_TYPE_NO_SHIPPING
        ),
        LOCAL("local"),
        UNKNOWN("\$UNKNOWN");
    }
}
