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
    private val reward: Reward
) : Parcelable {
    fun pledgeFlowContext() = this.pledgeFlowContext
    fun projectData() = this.projectData
    fun addOns() = this.addOns
    fun shippingRule() = this.shippingRule
    fun reward() = this.reward

    @Parcelize
    data class Builder(
        private var pledgeFlowContext: PledgeFlowContext = PledgeFlowContext.MANAGE_REWARD,
        private var projectData: ProjectData = ProjectData.builder().build(),
        private var addOns: List<Reward>? = null,
        private var shippingRule: ShippingRule? = null,
        private var reward: Reward = Reward.builder().build()
    ) : Parcelable {
        fun pledgeFlowContext(pledgeFlowContext: PledgeFlowContext) = apply { this.pledgeFlowContext = pledgeFlowContext }
        fun projectData(projectData: ProjectData) = apply { this.projectData = projectData }
        fun reward(reward: Reward) = apply { this.reward = reward }
        fun addOns(addOns: List<Reward>) = apply { this.addOns = addOns }
        fun shippingRule(shippingRule: ShippingRule) = apply { this.shippingRule = shippingRule }
        fun build() = PledgeData(
            pledgeFlowContext = pledgeFlowContext,
            projectData = projectData,
            reward = reward,
            addOns = addOns,
            shippingRule = shippingRule
        )
    }

    fun toBuilder() = Builder(
        pledgeFlowContext = pledgeFlowContext,
        projectData = projectData,
        reward = reward,
        addOns = addOns,
        shippingRule = shippingRule
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is PledgeData) {
            equals = pledgeFlowContext() == other.pledgeFlowContext() &&
                projectData() == other.projectData() &&
                reward() == other.reward() &&
                addOns() == other.addOns() &&
                shippingRule() == other.shippingRule()
        }
        return equals
    }

    companion object {

        @JvmStatic
        fun builder() = Builder()

        fun with(pledgeFlowContext: PledgeFlowContext, projectData: ProjectData, reward: Reward, addOns: List<Reward>? = null, shippingRule: ShippingRule? = null) =
            addOns?.let { addOns ->
                shippingRule?.let { shippingRule ->
                    return@let builder()
                        .pledgeFlowContext(pledgeFlowContext)
                        .projectData(projectData)
                        .reward(reward)
                        .addOns(addOns)
                        .shippingRule(shippingRule)
                        .build()
                } ?: builder()
                    .pledgeFlowContext(pledgeFlowContext)
                    .projectData(projectData)
                    .reward(reward)
                    .addOns(addOns)
                    .build()
            } ?: builder()
                .pledgeFlowContext(pledgeFlowContext)
                .projectData(projectData)
                .reward(reward)
                .build()
    }
}
