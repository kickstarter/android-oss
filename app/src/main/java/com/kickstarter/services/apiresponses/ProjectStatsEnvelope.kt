package com.kickstarter.services.apiresponses

import com.kickstarter.libs.qualifiers.AutoGson
import auto.parcel.AutoParcel
import android.os.Parcelable
import com.kickstarter.libs.ReferrerType
import com.kickstarter.models.Photo
import com.kickstarter.models.Project
import java.util.*
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

@Parcelize
class ProjectStatsEnvelope private constructor(
    private val cumulativeStats: CumulativeStats,
    private val fundingDistribution: List<FundingDateStats>,
    private val referralAggregates: ReferralAggregateStats,
    private val referralDistribution: List<ReferrerStats>,
    private val rewardDistribution: List<RewardStats>,
    private val videoStats: VideoStats?
) : Parcelable {
    fun cumulative() = this.cumulativeStats
    fun fundingDistribution() = this.fundingDistribution
    fun referralAggregates() = this.referralAggregates
    fun referralDistribution() = this.referralDistribution
    fun rewardDistribution() = this.rewardDistribution
    fun videoStats() = this.videoStats

    @Parcelize
    data class Builder(
        private var cumulativeStats: CumulativeStats = CumulativeStats.builder().build(),
        private var fundingDistribution: List<FundingDateStats> = emptyList(),
        private var referralAggregates: ReferralAggregateStats = ReferralAggregateStats.builder().build(),
        private var referralDistribution: List<ReferrerStats> = emptyList(),
        private var rewardDistribution: List<RewardStats> = emptyList(),
        private var videoStats: VideoStats? = null
    ) {
        fun cumulativeStats(cumulativeStats: CumulativeStats?) = apply { this.cumulativeStats = cumulativeStats }
        fun fundingDistribution(fundingDistribution: List<FundingDateStats>?) = apply { this.fundingDistribution = fundingDistribution }
        fun referralAggregates(referralAggregates: ReferralAggregateStats?) = apply { this.referralAggregates = referralAggregates }
        fun referralDistribution(referralDistribution: List<ReferrerStats>?) = apply { this.referralDistribution = referralDistribution }
        fun rewardDistribution(rewardDistribution: List<RewardStats>?) = apply { this.rewardDistribution = rewardDistribution }
        fun videoStats(videoStats: VideoStats?) = apply { this.videoStats = videoStats }
        fun build() = ProjectStatsEnvelope(
            cumulativeStats = cumulativeStats,
            fundingDistribution = fundingDistribution,
            referralAggregates = referralAggregates,
            referralDistribution = referralDistribution,
            rewardDistribution = rewardDistribution,
            videoStats = videoStats
        )
    }

    fun toBuilder() = Builder(
        cumulativeStats = cumulativeStats,
        fundingDistribution = fundingDistribution,
        referralAggregates = referralAggregates,
        referralDistribution = referralDistribution,
        rewardDistribution = rewardDistribution,
        videoStats = videoStats
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is ProjectStatsEnvelope) {
            equals = cumulativeStats() == other.cumulativeStats() &&
                    fundingDistribution() == other.fundingDistribution() &&
                    referralAggregates() == other.referralAggregates() &&
                    referralDistribution() == other.referralDistribution() &&
                    rewardDistribution() == other.rewardDistribution() &&
                    videoStats() == other.videoStats()
        }
        return equals
    }

    companion object {
        @JvmStatic
        fun builder() = CumulativeStats.Builder()
    }

    @Parcelize
    class CumulativeStats private constructor(
        private val averagePledge: Float,
        private val backersCount: Int,
        private val goal: Int,
        private val percentRaised: Float,
        private val pledged: Float
    ): Parcelable {
        fun averagePledge() = this.averagePledge
        fun backersCount() = this.backersCount
        fun goal() = this.goal
        fun percentRaised() = this.percentRaised
        fun pledged() = this.pledged

        @Parcelize
        data class Builder(
            private var averagePledge: Float = 0f,
            private var backersCount: Int = 0,
            private var goal: Int = 0,
            private var percentRaised: Float = 0f,
            private var pledged: Float = 0f,
        ) : Parcelable {
            fun averagePledge(averagePledge: Float?) = apply { this.averagePledge = averagePledge ?: 0f }
            fun backersCount(backersCount: Int?) = apply { this.backersCount = backersCount ?: 0 }
            fun goal(goal: Int?) = apply { this.goal = goal ?: 0 }
            fun percentRaised(percentRaised: Float?) = apply { this.percentRaised = percentRaised ?: 0f }
            fun pledged(pledged: Float?) = apply { this.pledged = pledged ?: 0f }
            fun build() = CumulativeStats(
                averagePledge = averagePledge,
                backersCount = backersCount,
                goal = goal,
                percentRaised = percentRaised,
                pledged = pledged
            )
        }

        fun toBuilder() = Builder(
            averagePledge = averagePledge,
            backersCount = backersCount,
            goal = goal,
            percentRaised = percentRaised,
            pledged = pledged
        )

        override fun equals(other: Any?): Boolean {
            var equals = super.equals(other)
            if (other is CumulativeStats) {
                equals = averagePledge() == other.averagePledge() &&
                        backersCount() == other.backersCount() &&
                        goal() == other.goal() &&
                        percentRaised() == other.percentRaised() &&
                        pledged() == other.pledged()
            }
            return equals
        }

        companion object {
            @JvmStatic
            fun builder() = Builder()
        }
    }

    @Parcelize
    class FundingDateStats private constructor(
        private val backersCount: Int,
        private val cumulativePledged: Float,
        private val cumulativeBackersCount: Int,
        private val date: DateTime,
        private val pledged: Float
    ): Parcelable {
        fun backersCount() = this.backersCount
        fun cumulativePledged() = this.cumulativePledged
        fun cumulativeBackersCount() = this.cumulativeBackersCount
        fun date() = this.date
        fun pledged() = this.pledged

        @Parcelize
        data class Builder(
            private var backersCount: Int = 0,
            private var cumulativePledged: Float = 0f,
            private var cumulativeBackersCount: Int = 0,
            private var date: DateTime = DateTime.now(),
            private var pledged: Float = 0f
        ) : Parcelable {
            fun backersCount(backersCount: Int?) = apply { this.backersCount = backersCount ?: 0 }
            fun cumulativePledged(cumulativePledged: Float?) = apply { this.cumulativePledged = cumulativePledged ?: 0f }
            fun cumulativeBackersCount(cumulativeBackersCount: Int?) = apply { this.cumulativeBackersCount = cumulativeBackersCount ?: 0 }
            fun date(date: DateTime?) = apply { date?.let { this.date = it } }
            fun pledged(pledged: Float?) = apply { this.pledged = pledged ?: 0f }
            fun build() = FundingDateStats(
                backersCount = backersCount,
                cumulativePledged = cumulativePledged,
                cumulativeBackersCount = cumulativeBackersCount,
                date = date,
                pledged = pledged
            )
        }

        fun toBuilder() = Builder(
            backersCount = backersCount,
            cumulativePledged = cumulativePledged,
            cumulativeBackersCount = cumulativeBackersCount,
            date = date,
            pledged = pledged
        )

        companion object {
            @JvmStatic
            fun builder() = Builder()
        }

        override fun equals(other: Any?): Boolean {
            var equals = super.equals(other)
            if (other is FundingDateStats) {
                equals = backersCount() == other.backersCount() &&
                        cumulativePledged() == other.cumulativePledged() &&
                        cumulativeBackersCount() == other.cumulativeBackersCount() &&
                        date() == other.date() &&
                        pledged() == other.pledged()
            }
            return equals
        }
    }

    @Parcelize
    class ReferralAggregateStats private constructor(
        private val custom: Float,
        private val external: Float,
        private val internal: Float
    ) : Parcelable {
        fun custom() = this.custom
        fun external() = this.external
        fun internal() = this.internal

        @Parcelize
        data class Builder(
            private var custom: Float = 0f,
            private var external: Float = 0f,,
            private var internal: Float = 0f
        ) : Parcelable {
            fun custom(custom: Float?) = apply { this.custom = custom ?: 0f }
            fun external(external: Float?) = apply { this.external = external ?: 0f }
            fun internal(internal: Float?) = apply { this.internal = internal ?: 0f }
            fun build() = ReferralAggregateStats(
                custom = custom,
                external = external,
                internal = internal
            )
        }

        fun toBuilder() = Builder(
            custom = custom,
            external = external,
            internal = internal
        )

        companion object {
            @JvmStatic
            fun builder() = Builder()
        }

        override fun equals(other: Any?): Boolean {
            var equals = super.equals(other)
            if (other is ReferralAggregateStats) {
                equals = custom() == other.custom() &&
                        external() == other.external() &&
                        internal() == other.internal()
            }
            return equals
        }
    }

    @Parcelize
    class ReferrerStats private constructor(
        private val backersCount: Int,
        private val code: String,
        private val percentageOfDollars: Float,
        private val pledged: Float,
        private val referrerName: String,
        private val referrerType: String
    ) : Parcelable {
        fun backersCount() = this.backersCount
        fun code() = this.code
        fun percentageOfDollars() = this.percentageOfDollars
        fun pledged() = this.pledged
        fun referrerName() = this.referrerName
        fun referrerType() = this.referrerType

        @Parcelize
        data class Builder(
            private var backersCount: Int = 0,
            private var code: String = "",
            private var percentageOfDollars: Float = 0f,
            private var pledged: Float = 0f,
            private var referrerName: String = "",
            private var referrerType: String = ""
        ) : Parcelable {
            fun backersCount(backersCount: Int?) = apply { this.backersCount = backersCount ?: 0 }
            fun code(code: String?) = apply { this.code = code ?: "" }
            fun percentageOfDollars(percentageOfDollars: Float?) = apply { this.percentageOfDollars = percentageOfDollars ?: 0f }
            fun pledged(pledged: Float?) = apply { this.pledged = pledged  ?: 0f}
            fun referrerName(referrerName: String?) = apply { this.referrerName = referrerName ?: "" }
            fun referrerType(referrerType: String?) = apply { this.referrerType = referrerType ?: "" }
            fun build() = ReferrerStats(
                backersCount = backersCount,
                code = code,
                percentageOfDollars = percentageOfDollars,
                pledged = pledged,
                referrerName = referrerName,
                referrerType = referrerType
            )
        }

        fun toBuilder() = Builder(
            backersCount = backersCount,
            code = code,
            percentageOfDollars = percentageOfDollars,
            pledged = pledged,
            referrerName = referrerName,
            referrerType = referrerType
        )

        companion object {
            // Deserialize the referrer type string names into the corresponding
            // enum type.
            @JvmStatic
            fun referrerTypeEnum(referrerType: String): ReferrerType {
                return when (referrerType.lowercase(Locale.getDefault())) {
                    "custom" -> ReferrerType.CUSTOM
                    "external" -> ReferrerType.EXTERNAL
                    "kickstarter" -> ReferrerType.KICKSTARTER
                    else -> ReferrerType.KICKSTARTER
                }
            }

            @JvmStatic
            fun builder(): Builder {
                return Builder()
            }
        }

        override fun equals(other: Any?): Boolean {
            var equals = super.equals(other)
            if (other is ReferrerStats) {
                equals = backersCount() == other.backersCount() &&
                        code() == other.code() &&
                        percentageOfDollars() == other.percentageOfDollars() &&
                        pledged() == other.pledged() &&
                        referrerName() == other.referrerName() &&
                        referrerType() == other.referrerType()
            }
            return equals
        }
    }

    @Parcelize
    class RewardStats private constructor(
        
    ) : Parcelable {
        abstract fun backersCount(): Int
        abstract fun rewardId(): Int
        abstract fun minimum(): Int
        abstract fun pledged(): Float

        @AutoParcel.Builder
        abstract class Builder {
            abstract fun backersCount(__: Int): Builder?
            abstract fun rewardId(__: Int): Builder?
            abstract fun minimum(__: Int): Builder?
            abstract fun pledged(__: Float): Builder?
            abstract fun build(): RewardStats?
        }

        abstract fun toBuilder(): Builder?

        companion object {
            @JvmStatic
            fun builder(): Builder {
                return AutoParcel_ProjectStatsEnvelope_RewardStats.Builder()
            }
        }
    }

    @AutoParcel
    @AutoGson
    abstract class VideoStats : Parcelable {
        abstract fun externalCompletions(): Int
        abstract fun externalStarts(): Int
        abstract fun internalCompletions(): Int
        abstract fun internalStarts(): Int

        @AutoParcel.Builder
        abstract class Builder {
            abstract fun externalCompletions(__: Int): Builder?
            abstract fun externalStarts(__: Int): Builder?
            abstract fun internalCompletions(__: Int): Builder?
            abstract fun internalStarts(__: Int): Builder?
            abstract fun build(): VideoStats?
        }

        abstract fun toBuilder(): Builder?

        companion object {
            @JvmStatic
            fun builder(): Builder {
                return AutoParcel_ProjectStatsEnvelope_VideoStats.Builder()
            }
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return AutoParcel_ProjectStatsEnvelope.Builder()
        }
    }
}