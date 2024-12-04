package com.kickstarter.models

import android.os.Parcelable
import androidx.annotation.StringDef
import com.google.firebase.remoteconfig.internal.ConfigFetchHandler
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

@Parcelize
class Backing private constructor(
    private val amount: Double,
    private val backer: User?,
    private val backerNote: String?,
    private val backerId: Long,
    private val backerName: String?,
    private val backerUrl: String?,
    private val backerCompletedAt: DateTime?,
    private val cancelable: Boolean,
    private val completedAt: DateTime?,
    private val completedByBacker: Boolean,
    private val id: Long,
    private val incremental: Boolean?,
    private val location: Location?,
    private val locationId: Long?,
    private val locationName: String?,
    private val paymentSource: PaymentSource?,
    private val pledgedAt: DateTime?,
    private val project: Project?,
    private val projectId: Long,
    private val reward: Reward?,
    private val rewardId: Long?,
    private val sequence: Long,
    private val shippingAmount: Float,
    @ConfigFetchHandler.FetchResponse.Status
    private val status: String,
    private val addOns: List<Reward>?,
    private val bonusAmount: Double,
    private val isPostCampaign: Boolean
) : Parcelable, Relay {
    fun amount() = this.amount
    fun backer() = this.backer
    fun backerNote() = this.backerNote
    fun backerId() = this.backerId
    fun backerName() = this.backerName
    fun backerUrl() = this.backerUrl
    fun backerCompletedAt() = this.backerCompletedAt
    fun cancelable() = this.cancelable
    fun completedAt() = this.completedAt
    fun completedByBacker() = this.completedByBacker
    override fun id() = this.id
    fun incremental() = this.incremental
    fun location() = this.location
    fun locationId() = this.locationId
    fun locationName() = this.locationName
    fun paymentSource() = this.paymentSource
    fun pledgedAt() = this.pledgedAt
    fun project() = this.project
    fun projectId() = this.projectId
    fun reward() = this.reward
    fun rewardId() = this.rewardId
    fun sequence() = this.sequence
    fun shippingAmount() = this.shippingAmount
    @Status
    fun status() = this.status
    fun addOns() = this.addOns
    fun bonusAmount() = this.bonusAmount
    fun isPostCampaign() = this.isPostCampaign

    @Parcelize
    data class Builder(
        private var amount: Double = 0.0,
        private var backer: User? = null,
        private var backerNote: String? = null,
        private var backerId: Long = 0L,
        private var backerName: String? = null,
        private var backerUrl: String? = null,
        private var backerCompletedAt: DateTime? = null,
        private var cancelable: Boolean = false,
        private var completedAt: DateTime? = null,
        private var completedByBacker: Boolean = false,
        private var id: Long = 0L,
        private var incremental: Boolean? = null,
        private var location: Location? = null,
        private var locationId: Long? = null,
        private var locationName: String? = null,
        private var paymentSource: PaymentSource? = null,
        private var pledgedAt: DateTime? = null,
        private var project: Project? = null,
        private var projectId: Long = 0L,
        private var reward: Reward? = null,
        private var rewardId: Long? = null,
        private var sequence: Long = 0L,
        private var shippingAmount: Float = 0.0f,
        @ConfigFetchHandler.FetchResponse.Status
        private var status: String = "",
        private var addOns: List<Reward>? = null,
        private var bonusAmount: Double = 0.0,
        private var isPostCampaign: Boolean = false
    ) : Parcelable {
        fun amount(amount: Double?) = apply { this.amount = amount ?: 0.0 }
        fun backer(backer: User?) = apply { this.backer = backer }
        fun backerNote(backerNote: String?) = apply { this.backerNote = backerNote }
        fun backerName(backerName: String?) = apply { this.backerName = backerName }
        fun backerUrl(backerUrl: String?) = apply { this.backerUrl = backerUrl }
        fun backerId(backerId: Long?) = apply { this.backerId = backerId ?: 0L }
        fun backerCompletedAt(backerCompletedAt: DateTime?) = apply { this.backerCompletedAt = backerCompletedAt }
        fun cancelable(cancelable: Boolean?) = apply { this.cancelable = cancelable ?: false }
        fun completedAt(completedAt: DateTime?) = apply { this.completedAt = completedAt }
        fun completedByBacker(completedByBacker: Boolean?) = apply { this.completedByBacker = completedByBacker ?: false }
        fun id(id: Long?) = apply { this.id = id ?: 0L }
        fun incremental(incremental: Boolean?) = apply { this.incremental = incremental }
        fun location(location: Location?) = apply { this.location = location }
        fun locationId(locationId: Long?) = apply { this.locationId = locationId }
        fun locationName(locationName: String?) = apply { this.locationName = locationName }
        fun paymentSource(paymentSource: PaymentSource?) = apply { this.paymentSource = paymentSource }
        fun pledgedAt(pledgedAt: DateTime?) = apply { this.pledgedAt = pledgedAt }
        fun project(project: Project?) = apply { this.project = project }
        fun projectId(projectId: Long?) = apply { this.projectId = projectId ?: 0L }
        fun reward(reward: Reward?) = apply { this.reward = reward }
        fun rewardId(rewardId: Long?) = apply { this.rewardId = rewardId }
        fun sequence(sequence: Long) = apply { this.sequence = sequence }
        fun shippingAmount(shippingAmount: Float?) = apply { this.shippingAmount = shippingAmount ?: 0.0f }
        fun status(status: String?) = apply { this.status = status ?: "" }
        fun addOns(addOns: List<Reward>?) = apply { this.addOns = addOns ?: emptyList() }
        fun bonusAmount(bonusAmount: Double?) = apply { this.bonusAmount = bonusAmount ?: 0.0 }
        fun isPostCampaign(isPostCampaign: Boolean) = apply { this.isPostCampaign = isPostCampaign }
        fun build() = Backing(
            amount = amount,
            backer = backer,
            backerNote = backerNote,
            backerName = backerName,
            backerUrl = backerUrl,
            backerId = backerId,
            backerCompletedAt = backerCompletedAt,
            cancelable = cancelable,
            completedAt = completedAt,
            completedByBacker = completedByBacker,
            id = id,
            incremental = incremental,
            location = location,
            locationId = locationId,
            locationName = locationName,
            paymentSource = paymentSource,
            pledgedAt = pledgedAt,
            project = project,
            projectId = projectId,
            reward = reward,
            rewardId = rewardId,
            sequence = sequence,
            shippingAmount = shippingAmount,
            status = status,
            addOns = addOns,
            bonusAmount = bonusAmount,
            isPostCampaign = isPostCampaign
        )
    }

    fun toBuilder() = Builder(
        amount = amount,
        backer = backer,
        backerNote = backerNote,
        backerName = backerName,
        backerUrl = backerUrl,
        backerId = backerId,
        backerCompletedAt = backerCompletedAt,
        cancelable = cancelable,
        completedAt = completedAt,
        completedByBacker = completedByBacker,
        id = id,
        incremental = incremental,
        location = location,
        locationId = locationId,
        locationName = locationName,
        paymentSource = paymentSource,
        pledgedAt = pledgedAt,
        project = project,
        projectId = projectId,
        reward = reward,
        rewardId = rewardId,
        sequence = sequence,
        shippingAmount = shippingAmount,
        status = status,
        addOns = addOns,
        bonusAmount = bonusAmount,
        isPostCampaign = isPostCampaign
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Backing) {
            equals = id() == other.id() &&
                incremental() == other.incremental() &&
                amount() == other.amount() &&
                backer() == other.backer() &&
                backerNote() == other.backerNote() &&
                backerName() == other.backerName() &&
                backerUrl() == other.backerUrl() &&
                backerId() == other.backerId() &&
                backerCompletedAt() == other.backerCompletedAt() &&
                cancelable() == other.cancelable() &&
                completedAt() == other.completedAt() &&
                completedByBacker() == other.completedByBacker() &&
                location() == other.location() &&
                locationId() == other.locationId() &&
                locationName() == other.locationName() &&
                paymentSource() == other.paymentSource() &&
                pledgedAt() == other.pledgedAt() &&
                project() == other.project() &&
                projectId() == other.projectId() &&
                reward() == other.reward() &&
                rewardId() == other.rewardId() &&
                sequence() == other.sequence() &&
                shippingAmount() == other.shippingAmount() &&
                status() == other.status() &&
                addOns() == other.addOns() &&
                bonusAmount() == other.bonusAmount()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(
        STATUS_CANCELED,
        STATUS_COLLECTED,
        STATUS_DROPPED,
        STATUS_ERRORED,
        STATUS_PLEDGED,
        STATUS_PREAUTH
    )
    annotation class Status

    companion object {
        @JvmStatic
        fun builder() = Builder()

        const val STATUS_CANCELED = "canceled"
        const val STATUS_COLLECTED = "collected"
        const val STATUS_DROPPED = "dropped"
        const val STATUS_ERRORED = "errored"
        const val STATUS_PLEDGED = "pledged"
        const val STATUS_PREAUTH = "preauth"
    }
}
