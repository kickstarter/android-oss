package com.kickstarter.mock.factories

import com.kickstarter.libs.models.Country
import com.kickstarter.mock.factories.RewardsItemFactory.rewardsItem
import com.kickstarter.models.Reward
import com.kickstarter.models.Reward.Companion.builder
import com.kickstarter.models.SingleLocation
import org.joda.time.DateTime

object RewardFactory {
    val ESTIMATED_DELIVERY = DateTime.parse("2019-03-26T19:26:09Z")

    @JvmStatic
    fun addOn(): Reward {
        return reward().toBuilder()
            .isAddOn(true)
            .isAvailable(true)
            .limit(10)
            .build()
    }

    fun addOnSingle(): Reward {
        return reward().toBuilder()
            .quantity(1)
            .isAddOn(true)
            .isAvailable(true)
            .limit(10)
            .build()
    }

    fun addOnMultiple(): Reward {
        return reward().toBuilder()
            .quantity(5)
            .isAddOn(true)
            .isAvailable(true)
            .limit(10)
            .build()
    }

    fun rewardHasAddOns(): Reward {
        return reward().toBuilder()
            .hasAddons(true)
            .build()
    }

    fun digitalReward(): Reward {
        return reward().toBuilder()
            .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING)
            .shippingPreference("none")
            .build()
    }

    @JvmStatic
    fun reward(): Reward {
        val description = "A digital download of the album and documentary."
        return builder()
            .backersCount(123)
            .convertedMinimum(20.0)
            .id(IdFactory.id().toLong())
            .description(description)
            .estimatedDeliveryOn(ESTIMATED_DELIVERY)
            .minimum(20.0)
            .pledgeAmount(20.0)
            .latePledgeAmount(30.0)
            .shippingPreference("unrestricted")
            .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING)
            .title("Digital Bundle")
            .build()
    }

    fun backers(): Reward {
        return reward().toBuilder()
            .backersCount(100)
            .build()
    }

    fun ended(): Reward {
        return reward().toBuilder()
            .endsAt(DateTime.now().minusDays(2))
            .build()
    }

    fun endingSoon(): Reward {
        return reward().toBuilder()
            .endsAt(DateTime.now().plusDays(2))
            .build()
    }

    fun itemized(): Reward {
        val rewardId = IdFactory.id().toLong()
        return reward().toBuilder()
            .id(rewardId)
            .rewardsItems(
                listOf(
                    rewardsItem().toBuilder()
                        .rewardId(rewardId)
                        .build()
                )
            )
            .build()
    }

    fun itemizedAddOn(): Reward {
        val rewardId = IdFactory.id().toLong()
        return reward().toBuilder()
            .id(rewardId)
            .minimum(10.0)
            .pledgeAmount(10.0)
            .latePledgeAmount(20.0)
            .isAddOn(true)
            .addOnsItems(
                listOf(
                    rewardsItem().toBuilder()
                        .rewardId(rewardId)
                        .build()
                )
            )
            .build()
    }

    @JvmStatic
    fun limited(): Reward {
        return reward().toBuilder()
            .limit(10)
            .remaining(5)
            .build()
    }

    fun noBackers(): Reward {
        return reward().toBuilder()
            .backersCount(0)
            .build()
    }

    fun maxReward(country: Country): Reward {
        return reward().toBuilder()
            .minimum(country.maxPledge.toDouble())
            .pledgeAmount(country.maxPledge.toDouble())
            .latePledgeAmount(country.maxPledge.toDouble() + 10.0)
            .backersCount(0)
            .build()
    }

    @JvmStatic
    fun limitReached(): Reward {
        return builder()
            .backersCount(123)
            .convertedMinimum(20.0)
            .id(IdFactory.id().toLong())
            .description("A digital download of the album and documentary.")
            .limit(50)
            .minimum(20.0)
            .pledgeAmount(20.0)
            .latePledgeAmount(30.0)
            .remaining(0)
            .title("Digital Bundle")
            .build()
    }

    fun multipleLocationShipping(): Reward {
        return reward().toBuilder()
            .shippingType(Reward.SHIPPING_TYPE_MULTIPLE_LOCATIONS)
            .estimatedDeliveryOn(ESTIMATED_DELIVERY)
            .build()
    }

    fun rewardWithShipping(): Reward {
        return reward().toBuilder()
            .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name)
            .shippingType(Reward.SHIPPING_TYPE_ANYWHERE)
            .estimatedDeliveryOn(ESTIMATED_DELIVERY)
            .build()
    }

    fun rewardRestrictedShipping(): Reward {
        return reward().toBuilder()
            .shippingPreference(Reward.ShippingPreference.RESTRICTED.name)
            .build()
    }

    fun singleLocationShipping(localizedLocationName: String): Reward {
        return reward().toBuilder()
            .shippingType(Reward.SHIPPING_TYPE_SINGLE_LOCATION)
            .shippingSingleLocation(
                SingleLocation.builder()
                    .id(IdFactory.id().toLong())
                    .localizedName(localizedLocationName)
                    .build()
            )
            .estimatedDeliveryOn(ESTIMATED_DELIVERY)
            .build()
    }

    fun localReceiptLocation(): Reward {
        return reward().toBuilder()
            .shippingType(Reward.SHIPPING_TYPE_LOCAL_PICKUP)
            .shippingPreference(Reward.ShippingPreference.LOCAL.name)
            .shippingPreferenceType(Reward.ShippingPreference.LOCAL)
            .localReceiptLocation(LocationFactory.germany())
            .estimatedDeliveryOn(null)
            .build()
    }

    @JvmStatic
    fun noReward(): Reward {
        return builder()
            .convertedMinimum(1.0)
            .id(0)
            .estimatedDeliveryOn(null)
            .description("No reward")
            .minimum(1.0)
            .pledgeAmount(1.0)
            .latePledgeAmount(1.0)
            .build()
    }

    fun noDescription(): Reward {
        return reward().toBuilder()
            .description("")
            .build()
    }
}
