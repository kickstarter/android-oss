package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.PairUtils
import com.kickstarter.libs.utils.extensions.compareDescending
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.ReferrerStats
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerStatsViewHolder
import rx.Observable
import rx.subjects.PublishSubject
import java.util.Collections

interface CreatorDashboardReferrerStatsHolderViewModel {
    interface Inputs {
        /** Current project and list of referrer stats.  */
        fun projectAndReferrerStatsInput(projectAndReferrerStats: Pair<Project, List<ReferrerStats>>)
    }

    interface Outputs {
        /** Emits current project and sorted referrer stats.  */
        fun projectAndReferrerStats(): Observable<Pair<Project, List<ReferrerStats>>>

        /** Emits when there are no referrer stats.  */
        fun referrerStatsListIsGone(): Observable<Boolean>

        /** Emits when there are more than 10 referrer stats and title copy should reflect limited list.  */
        fun referrersTitleIsTopTen(): Observable<Boolean>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<CreatorDashboardReferrerStatsViewHolder?>(environment), Inputs, Outputs {

        private val projectAndReferrerStatsInput =
            PublishSubject.create<Pair<Project, List<ReferrerStats>>>()
        private val projectAndReferrerStats: Observable<Pair<Project, List<ReferrerStats>>>
        private val referrerStatsListIsGone = PublishSubject.create<Boolean>()
        private val referrersTitleIsLimitedCopy = PublishSubject.create<Boolean>()

        @JvmField
        val inputs: Inputs = this
        @JvmField
        val outputs: Outputs = this

        init {
            val sortedReferrerStats = projectAndReferrerStatsInput
                .map { PairUtils.second(it) }
                .map { sortReferrerStats(it) }

            val limitedSortedReferrerStats = sortedReferrerStats
                .map<List<ReferrerStats>> { stats: List<ReferrerStats> ->
                    ArrayList(
                        stats.subList(
                            0,
                            Math.min(stats.size, 10)
                        )
                    )
                }

            projectAndReferrerStats = projectAndReferrerStatsInput
                .map { PairUtils.first(it) }
                .compose(Transformers.combineLatestPair(limitedSortedReferrerStats))

            sortedReferrerStats
                .map { it.isEmpty() }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(referrerStatsListIsGone)

            sortedReferrerStats
                .map { it.size > 10 }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(referrersTitleIsLimitedCopy)
        }

        private inner class OrderByBackersReferrerStatsComparator : Comparator<ReferrerStats> {
            override fun compare(o1: ReferrerStats, o2: ReferrerStats): Int {
                return o1.pledged().compareDescending(o2.pledged())
            }
        }

        private fun sortReferrerStats(referrerStatsList: List<ReferrerStats>): List<ReferrerStats> {
            val referrerStatsComparator = OrderByBackersReferrerStatsComparator()
            Collections.sort(referrerStatsList, referrerStatsComparator)
            return referrerStatsList
        }

        override fun projectAndReferrerStatsInput(projectAndReferrerStats: Pair<Project, List<ReferrerStats>>) {
            projectAndReferrerStatsInput.onNext(projectAndReferrerStats)
        }

        override fun projectAndReferrerStats(): Observable<Pair<Project, List<ReferrerStats>>> = projectAndReferrerStats

        override fun referrerStatsListIsGone(): Observable<Boolean> = referrerStatsListIsGone
        override fun referrersTitleIsTopTen(): Observable<Boolean> = referrersTitleIsLimitedCopy
    }
}
