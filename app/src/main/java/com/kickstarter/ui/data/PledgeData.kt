package com.kickstarter.ui.data

import android.os.Parcelable
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import kotlinx.parcelize.Parcelize

@Parcelize
class PledgeData private constructor(
    private val pledgeFlowContext: PledgeFlowContext,
    private val projectData: ProjectData,
    private val addOns: List<Reward>?,
    private val shippingRule: ShippingRule?,
    private val reward: Reward,
    private val bonusAmount: Double
) : Parcelable {
    fun pledgeFlowContext() = this.pledgeFlowContext
    fun projectData() = this.projectData
    fun addOns() = this.addOns
    fun shippingRule() = this.shippingRule
    fun reward() = this.reward
    fun bonusAmount() = this.bonusAmount

    @Parcelize
    data class Builder(
        private var pledgeFlowContext: PledgeFlowContext = PledgeFlowContext.MANAGE_REWARD,
        private var projectData: ProjectData = ProjectData.builder().build(),
        private var addOns: List<Reward>? = null,
        private var shippingRule: ShippingRule? = null,
        private var reward: Reward = Reward.builder().build(),
        private var bonusAmount: Double = 0.0
    ) : Parcelable {
        fun pledgeFlowContext(pledgeFlowContext: PledgeFlowContext) = apply { this.pledgeFlowContext = pledgeFlowContext }
        fun projectData(projectData: ProjectData) = apply { this.projectData = projectData }
        fun reward(reward: Reward) = apply { this.reward = reward }
        fun addOns(addOns: List<Reward>) = apply { this.addOns = addOns }
        fun shippingRule(shippingRule: ShippingRule) = apply { this.shippingRule = shippingRule }
        fun bonusAmount(amount: Double) = apply { this.bonusAmount = amount }
        fun build() = PledgeData(
            pledgeFlowContext = pledgeFlowContext,
            projectData = projectData,
            reward = reward,
            addOns = addOns,
            shippingRule = shippingRule,
            bonusAmount = bonusAmount
        )
    }

    fun toBuilder() = Builder(
        pledgeFlowContext = pledgeFlowContext,
        projectData = projectData,
        reward = reward,
        addOns = addOns,
        shippingRule = shippingRule,
        bonusAmount = bonusAmount
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is PledgeData) {
            equals = pledgeFlowContext() == other.pledgeFlowContext() &&
                projectData() == other.projectData() &&
                reward() == other.reward() &&
                addOns() == other.addOns() &&
                shippingRule() == other.shippingRule() &&
                bonusAmount() == other.bonusAmount()
        }
        return equals
    }

    companion object {

        @JvmStatic
        fun builder() = Builder()

        fun with(pledgeFlowContext: PledgeFlowContext, projectData: ProjectData, reward: Reward, addOns: List<Reward>? = null, shippingRule: ShippingRule? = null, bonusAmount: Double = 0.0) =
            builder()
                .pledgeFlowContext(pledgeFlowContext)
                .projectData(projectData)
                .reward(reward)
                .bonusAmount(bonusAmount)
                .apply {
                    if (addOns != null) {
                        this.addOns(addOns)
                    }
                    if (shippingRule != null) {
                        this.shippingRule(shippingRule)
                    }
                }
                .build()
    }
}
