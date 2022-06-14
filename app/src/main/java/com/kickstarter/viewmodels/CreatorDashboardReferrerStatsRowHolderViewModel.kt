package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.PairUtils
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.ReferrerStats
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.ReferrerStats.Companion.referrerTypeEnum
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerStatsViewHolder
import rx.Observable
import rx.subjects.PublishSubject

interface CreatorDashboardReferrerStatsRowHolderViewModel {
    interface Inputs {
        /** Current project and list of referrer stats.  */
        fun projectAndReferrerStatsInput(projectAndReferrerStats: Pair<Project, ReferrerStats>)
    }

    interface Outputs {
        /** Emits project and the amount pledged for this referrer.  */
        fun projectAndPledgedForReferrer(): Observable<Pair<Project, Float>>

        /** Emits string number of backers.  */
        fun referrerBackerCount(): Observable<String>

        /** Emits resource ID of referrer color.  */
        fun referrerSourceColorId(): Observable<Int>

        /** Emits source name of referrer.  */
        fun referrerSourceName(): Observable<String>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<CreatorDashboardReferrerStatsViewHolder?>(environment), Inputs, Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this
        private val projectAndReferrerStats = PublishSubject.create<Pair<Project, ReferrerStats>>()
        private val projectAndPledgedForReferrer: Observable<Pair<Project, Float>>
        private val referrerBackerCount: Observable<String>
        private val referrerSourceColorId: Observable<Int>
        private val referrerSourceName: Observable<String>

        init {
            projectAndPledgedForReferrer = projectAndReferrerStats
                .map {
                    Pair.create(
                        it.first,
                        it.second.pledged()
                    )
                }

            referrerSourceColorId = projectAndReferrerStats
                .map { PairUtils.second(it) }
                .map { referrerStat: ReferrerStats -> referrerTypeEnum(referrerStat.referrerType()).referrerColorId }

            referrerSourceName = projectAndReferrerStats
                .map { PairUtils.second(it) }
                .map { it.referrerName() }

            referrerBackerCount = projectAndReferrerStats
                .map { PairUtils.second(it) }
                .map { it.backersCount() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .map {
                    NumberUtils.format(it)
                }
        }

        override fun projectAndReferrerStatsInput(projectAndReferrerStats: Pair<Project, ReferrerStats>) {
            this.projectAndReferrerStats.onNext(projectAndReferrerStats)
        }

        override fun projectAndPledgedForReferrer(): Observable<Pair<Project, Float>> =
            projectAndPledgedForReferrer

        override fun referrerBackerCount(): Observable<String> =
            referrerBackerCount

        override fun referrerSourceColorId(): Observable<Int> =
            referrerSourceColorId

        override fun referrerSourceName(): Observable<String> =
            referrerSourceName
    }
}
