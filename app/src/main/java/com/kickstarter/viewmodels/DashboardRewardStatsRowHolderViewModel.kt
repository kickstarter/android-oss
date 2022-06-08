package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.PairUtils
import com.kickstarter.libs.utils.extensions.wrapInParentheses
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.RewardStats
import com.kickstarter.ui.viewholders.CreatorDashboardRewardStatsRowViewHolder
import rx.Observable
import rx.subjects.PublishSubject

interface DashboardRewardStatsRowHolderViewModel {
    interface Inputs {
        /** Current project and reward stat.  */
        fun projectAndRewardStats(projectAndRewardStats: Pair<Project, RewardStats>)
    }

    interface Outputs {
        /** Emits percent of the total that came from this reward.  */
        fun percentageOfTotalPledged(): Observable<String>

        /** Emits project and the amount pledged for this reward.  */
        fun projectAndRewardPledged(): Observable<Pair<Project, Float>>

        /** Emits string number of backers.  */
        fun rewardBackerCount(): Observable<String>

        /** Emits minimum for reward.  */
        fun projectAndRewardMinimum(): Observable<Pair<Project, Int>>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<CreatorDashboardRewardStatsRowViewHolder?>(environment), Inputs, Outputs {

        @JvmField
        val inputs: Inputs = this
        @JvmField
        val outputs: Outputs = this

        private val projectAndRewardStats = PublishSubject.create<Pair<Project, RewardStats>>()

        private val percentageOfTotalPledged: Observable<String>
        private val projectAndRewardPledged: Observable<Pair<Project, Float>>
        private val rewardBackerCount: Observable<String>
        private val rewardMinimum: Observable<Pair<Project, Int>>

        init {
            val rewardStats = projectAndRewardStats
                .map { PairUtils.second(it) }

            rewardBackerCount = rewardStats
                .map { it.backersCount() }
                .map {
                    NumberUtils.format(
                        it
                    )
                }

            projectAndRewardPledged = projectAndRewardStats
                .map {
                    Pair.create(
                        it.first,
                        it.second.pledged()
                    )
                }

            rewardMinimum = projectAndRewardStats
                .map {
                    Pair.create(
                        it.first,
                        it.second.minimum()
                    )
                }

            percentageOfTotalPledged = projectAndRewardStats
                .map {
                    val p = it.first
                    val rs = it.second
                    NumberUtils.flooredPercentage(
                        rs.pledged() / p.pledged()
                            .toFloat() * 100
                    )
                }
                .map { it.wrapInParentheses() }
        }

        override fun projectAndRewardStats(projectAndRewardStats: Pair<Project, RewardStats>) {
            this.projectAndRewardStats.onNext(projectAndRewardStats)
        }

        override fun percentageOfTotalPledged(): Observable<String> = percentageOfTotalPledged
        override fun projectAndRewardPledged(): Observable<Pair<Project, Float>> = projectAndRewardPledged
        override fun rewardBackerCount(): Observable<String> = rewardBackerCount
        override fun projectAndRewardMinimum(): Observable<Pair<Project, Int>> = rewardMinimum
    }
}
