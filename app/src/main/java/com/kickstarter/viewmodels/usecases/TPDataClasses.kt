package com.kickstarter.viewmodels.usecases

import android.util.Pair
import com.kickstarter.libs.FirebaseHelper
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
// import com.kickstarter.type.TriggerThirdPartyEventInput

/***
 * ThirdPartyEventInputData data model to sent items values to third party analytics systems
 * an item makes reference to a reward or addOn
 */
data class TPEventItemInputData(val itemId: String, val itemName: String, val price: Double? = null)
/***
 * ThirdPartyAppDataInput information required for certain types of events for third party actors
 * @param androidConsent reflects the user opt-in value for consent management
 * @param iOSConsent flase in Android by default, reflects the user opt-in value for iOS users
 */
data class TPAppDataInput(
    val iOSConsent: Boolean = false,
    val androidConsent: Boolean = true,
    val extInfo: List<String> = listOf(
        "a2",
        "",
        "",
        "",
        android.os.Build.VERSION.RELEASE,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
    )
)
/***
 * ThirdPartyEventInputData data structure that represents a third party Event, should match 1:1
 * with the GraphQL model [TriggerThirdPartyEventInput]
 */
data class TPEventInputData(
    val eventName: String,
    val deviceId: String,
    val projectId: String,
    val userId: String? = null,
    val items: List<TPEventItemInputData> = emptyList(),
    val pledgeAmount: Double? = null,
    val shipping: Double? = null,
    val transactionId: String,
    val appData: TPAppDataInput,
    val firebaseScreen: String? = null,
    val firebasePreviousScreen: String? = null
)

interface BuildInput {
    fun buildInput(
        eventName: ThirdPartyEventValues.EventName,
        canSendEventFlag: Boolean,
        firebaseScreen: String? = null,
        firebasePreviousScreen: String? = null,
        draftPledge: Pair<Double, Double>? = null,
        rawData: Pair<Pair<Project, User?>, Pair<CheckoutData, PledgeData>?>
    ): TPEventInputData {

        val eventName = eventName.value
        val userId = rawData.first.second?.id().toString()
        val deviceId = FirebaseHelper.identifier
        val projectId = encodeRelayId(rawData.first.first)
        var items: List<TPEventItemInputData> = emptyList()
        var pAmount: Double? = null
        var shipping: Double? = null
        var transactionId = ""

        rawData.second?.second?.let { pledgeData ->
            val rewardsAndAddons = mutableListOf(pledgeData.reward())
            pledgeData.addOns()?.forEach { addon ->
                rewardsAndAddons.add(addon)
            }

            items = rewardsAndAddons.map { rewards ->
                TPEventItemInputData(
                    itemId = rewards.id().toString(),
                    itemName = rewards.title().toString(),
                    price = rewards.minimum() * (rewards.quantity() ?: 1)
                )
            }
        }

        // - Checkout information will be available after user becomes a backer, util for `Purchase` type of events, empty otherwise
        rawData.second?.first?.let { checkoutData ->
            pAmount = checkoutData.amount()
            shipping = checkoutData.shippingAmount()
            transactionId = checkoutData.id().toString()
        }

        // - Draft pledge information available when pledge economic values are required before the user becomes a backer, util for
        // `Add_payment_method` type of events
        draftPledge?.let {
            pAmount = it.first
            shipping = it.second
        }

        return TPEventInputData(
            eventName = eventName,
            userId = userId,
            deviceId = deviceId,
            projectId = projectId,
            items = items,
            pledgeAmount = pAmount,
            shipping = shipping,
            transactionId = transactionId,
            appData = TPAppDataInput(androidConsent = canSendEventFlag),
            firebaseScreen = firebaseScreen,
            firebasePreviousScreen = firebasePreviousScreen
        )
    }
}
