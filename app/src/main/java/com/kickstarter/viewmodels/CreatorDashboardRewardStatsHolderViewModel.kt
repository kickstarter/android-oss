package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.PairUtils
import com.kickstarter.libs.utils.extensions.compareDescending
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.RewardStats
import com.kickstarter.ui.viewholders.CreatorDashboardRewardStatsViewHolder
import rx.Observable
import rx.subjects.PublishSubject
import java.util.Collections

interface CreatorDashboardRewardStatsHolderViewModel {
    interface Inputs {
        /** Call when user clicks pledged column title.  */
        fun pledgedColumnTitleClicked()

        /** Current project and list of stats.  */
        fun projectAndRewardStatsInput(projectAndRewardStatsEnvelope: Pair<Project, List<RewardStats>>)
    }

    interface Outputs {
        /** Emits current project and sorted reward stats.  */
        fun projectAndRewardStats(): Observable<Pair<Project, List<RewardStats>>>

        /** Emits when there are no reward stats.  */
        fun rewardsStatsListIsGone(): Observable<Boolean>

        /** Emits when there are more than 10 reward stats.  */
        fun rewardsStatsTruncatedTextIsGone(): Observable<Boolean>

        /** Emits when there are more than 10 reward stats and title copy should reflect limited list.  */
        fun rewardsTitleIsTopTen(): Observable<Boolean>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<CreatorDashboardRewardStatsViewHolder?>(environment), Inputs, Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val pledgedColumnTitleClicked = PublishSubject.create<Void?>()
        private val projectAndRewardStatsInput =
            PublishSubject.create<Pair<Project, List<RewardStats>>>()
        private val projectAndRewardStats: Observable<Pair<Project, List<RewardStats>>>
        private val rewardsStatsListIsGone = PublishSubject.create<Boolean>()
        private val rewardsStatsTruncatedTextIsGone = PublishSubject.create<Boolean>()
        private val rewardsTitleIsLimitedCopy = PublishSubject.create<Boolean>()

        init {
            val sortedRewardStats = projectAndRewardStatsInput
                .map { PairUtils.second(it) }
                .map { sortRewardStats(it) }

            val limitedSortedRewardStats = sortedRewardStats
                .map<List<RewardStats>> {
                    ArrayList(
                        it.subList(
                            0,
                            Math.min(it.size, 10)
                        )
                    )
                }

            projectAndRewardStats = projectAndRewardStatsInput
                .map { PairUtils.first(it) }
                .compose(Transformers.combineLatestPair(limitedSortedRewardStats))

            sortedRewardStats
                .map { it.isEmpty() }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(rewardsStatsListIsGone)

            sortedRewardStats
                .map { it.size <= 10 }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(rewardsStatsTruncatedTextIsGone)

            sortedRewardStats
                .map { it.size > 10 }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(rewardsTitleIsLimitedCopy)
        }

        private class OrderByPledgedRewardStatsComparator : Comparator<RewardStats> {
            override fun compare(o1: RewardStats, o2: RewardStats): Int {
                return o1.pledged().compareDescending(o2.pledged())
            }
        }

        private fun sortRewardStats(rewardStatsList: List<RewardStats>): List<RewardStats> {
            val rewardStatsComparator = OrderByPledgedRewardStatsComparator()
            Collections.sort(rewardStatsList, rewardStatsComparator)
            return rewardStatsList
        }

        override fun pledgedColumnTitleClicked() {
            pledgedColumnTitleClicked.onNext(null)
        }

        override fun projectAndRewardStatsInput(projectAndRewardStats: Pair<Project, List<RewardStats>>) {
            projectAndRewardStatsInput.onNext(projectAndRewardStats)
        }

        override fun projectAndRewardStats(): Observable<Pair<Project, List<RewardStats>>> = projectAndRewardStats
        override fun rewardsStatsListIsGone(): Observable<Boolean> = rewardsStatsListIsGone
        override fun rewardsStatsTruncatedTextIsGone(): Observable<Boolean> = rewardsStatsTruncatedTextIsGone

        override fun rewardsTitleIsTopTen(): Observable<Boolean> = rewardsTitleIsLimitedCopy
    }
}
