package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.PairUtils
import com.kickstarter.libs.utils.extensions.isZero
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerBreakdownViewHolder
import rx.Observable
import rx.subjects.PublishSubject

interface CreatorDashboardReferrerBreakdownHolderViewModel {
    interface Inputs {
        /** current project and related stats object  */
        fun projectAndStatsInput(projectAndStats: Pair<Project, ProjectStatsEnvelope>)
    }

    interface Outputs {
        /** Emits a boolean that determines if breakdown chart is gone.  */
        fun breakdownViewIsGone(): Observable<Boolean>

        /** Emits the percentage of total pledges from a custom referrer.  */
        fun customReferrerPercent(): Observable<Float>

        /** Emits the text for the percentage of total pledges from a custom referrer.  */
        fun customReferrerPercentText(): Observable<String>

        /** Emits a boolean that determines if empty view is gone.  */
        fun emptyViewIsGone(): Observable<Boolean>

        /** Emits the percentage of total pledges from a external referrer.  */
        fun externalReferrerPercent(): Observable<Float>

        /** Emits the text for the percentage of total pledges from an external referrer.  */
        fun externalReferrerPercentText(): Observable<String>

        /** Emits the percentage of total pledges from a Kickstarter referrer.  */
        fun kickstarterReferrerPercent(): Observable<Float>

        /** Emits the text for the percentage of total pledges from a Kickstarter referrer.  */
        fun kickstarterReferrerPercentText(): Observable<String>

        /** Emits a boolean that determines if the pledged via custom layout is gone.  */
        fun pledgedViaCustomLayoutIsGone(): Observable<Boolean>

        /** Emits a boolean that determines if the pledged via external layout is gone.  */
        fun pledgedViaExternalLayoutIsGone(): Observable<Boolean>

        /** Emits a boolean that determines if the pledged via Kickstarter layout is gone.  */
        fun pledgedViaKickstarterLayoutIsGone(): Observable<Boolean>

        /** Emits the current project and the average pledge for that project.  */
        fun projectAndAveragePledge(): Observable<Pair<Project, Int>>

        /** Emits the current project and the amount pledged via custom referrers.  */
        fun projectAndCustomReferrerPledgedAmount(): Observable<Pair<Project, Float>>

        /** Emits the current project and the amount pledged via external referrers.  */
        fun projectAndExternalReferrerPledgedAmount(): Observable<Pair<Project, Float>>

        /** Emits the current project and the amount pledged via Kickstarter referrers.  */
        fun projectAndKickstarterReferrerPledgedAmount(): Observable<Pair<Project, Float>>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<CreatorDashboardReferrerBreakdownViewHolder?>(environment),
        Inputs,
        Outputs {

        @JvmField
        val inputs: Inputs = this
        @JvmField
        val outputs: Outputs = this

        private val projectAndProjectStatsInput =
            PublishSubject.create<Pair<Project, ProjectStatsEnvelope>>()

        private val breakdownViewIsGone: Observable<Boolean>
        private val customReferrerPercent: Observable<Float>
        private val customReferrerPercentText: Observable<String>
        private val customReferrerPledgedAmount: Observable<Float>
        private val emptyViewIsGone: Observable<Boolean>
        private val externalReferrerPercent: Observable<Float>
        private val externalReferrerPercentText: Observable<String>
        private val externalReferrerPledgedAmount: Observable<Float>
        private val kickstarterReferrerPercent: Observable<Float>
        private val kickstarterReferrerPercentText: Observable<String>
        private val kickstarterReferrerPledgedAmount: Observable<Float>
        private val pledgedViaCustomLayoutIsGone: Observable<Boolean>
        private val pledgedViaExternalLayoutIsGone: Observable<Boolean>
        private val pledgedViaKickstarterLayoutIsGone: Observable<Boolean>
        private val projectAndAveragePledge: Observable<Pair<Project, Int>>
        private val projectAndCustomReferrerPledgedAmount: Observable<Pair<Project, Float>>
        private val projectAndExternalReferrerPledgedAmount: Observable<Pair<Project, Float>>
        private val projectAndKickstarterReferrerPledgedAmount: Observable<Pair<Project, Float>>

        init {
            val currentProject = projectAndProjectStatsInput
                .map { PairUtils.first(it) }

            val projectStats = projectAndProjectStatsInput
                .map { PairUtils.second(it) }

            val referralAggregates = projectAndProjectStatsInput
                .map { PairUtils.second(it) }
                .map { it.referralAggregates() }

            val referrerStats = projectStats
                .map { it.referralDistribution() }

            val cumulativeStats = projectStats
                .filter { ObjectUtils.isNotNull(it.cumulative()) }
                .map { it.cumulative() }

            val averagePledge = cumulativeStats
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .map { it.averagePledge() }
                .map { it.toInt() }

            val pledged = cumulativeStats
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .map { it.pledged() }

            projectAndAveragePledge = Observable.combineLatest(
                currentProject,
                averagePledge
            ) { a: Project?, b: Int? -> Pair.create(a, b) }

            val emptyStats = referrerStats
                .map { it.isEmpty() }

            breakdownViewIsGone = emptyStats

            emptyViewIsGone = emptyStats
                .map { it.negate() }

            customReferrerPercent = referralAggregates
                .map { it.custom() }
                .compose(Transformers.combineLatestPair(pledged))
                .map {
                    if (it.second.toInt().isZero())
                        0f
                    else
                        it.first / it.second
                }

            customReferrerPercentText = customReferrerPercent
                .map { percent: Float -> NumberUtils.flooredPercentage(percent * 100f) }

            customReferrerPledgedAmount = referralAggregates
                .map { it.custom() }

            projectAndCustomReferrerPledgedAmount = Observable.combineLatest(
                currentProject,
                customReferrerPledgedAmount
            ) { a: Project?, b: Float? -> Pair.create(a, b) }

            externalReferrerPercent = referralAggregates
                .map { it.external() }
                .compose(Transformers.combineLatestPair(pledged))
                .map { externalAndPledged: Pair<Float, Float> ->
                    if (externalAndPledged.second.toInt()
                        .isZero()
                    ) 0f else externalAndPledged.first / externalAndPledged.second
                }

            externalReferrerPercentText = externalReferrerPercent
                .map { percent: Float -> NumberUtils.flooredPercentage(percent * 100f) }

            externalReferrerPledgedAmount = referralAggregates
                .map { it.external() }

            projectAndExternalReferrerPledgedAmount = Observable.combineLatest(
                currentProject,
                externalReferrerPledgedAmount
            ) { a: Project?, b: Float? -> Pair.create(a, b) }
            kickstarterReferrerPercent = referralAggregates
                .map { it.internal() }
                .compose(Transformers.combineLatestPair(pledged))
                .map { internalAndPledged: Pair<Float, Float> ->
                    if (internalAndPledged.second.toInt().isZero()
                    ) 0f else internalAndPledged.first / internalAndPledged.second
                }

            kickstarterReferrerPercentText = kickstarterReferrerPercent
                .map { percent: Float -> NumberUtils.flooredPercentage(percent * 100f) }

            kickstarterReferrerPledgedAmount = referralAggregates
                .map { it.internal() }

            projectAndKickstarterReferrerPledgedAmount = Observable.combineLatest(
                currentProject,
                kickstarterReferrerPledgedAmount
            ) { a: Project?, b: Float? -> Pair.create(a, b) }

            pledgedViaCustomLayoutIsGone = customReferrerPledgedAmount
                .map { it <= 0f }

            pledgedViaExternalLayoutIsGone = externalReferrerPledgedAmount
                .map { it <= 0f }

            pledgedViaKickstarterLayoutIsGone = kickstarterReferrerPledgedAmount
                .map { it <= 0f }
        }

        override fun projectAndStatsInput(projectAndStats: Pair<Project, ProjectStatsEnvelope>) {
            projectAndProjectStatsInput.onNext(projectAndStats)
        }

        override fun breakdownViewIsGone(): Observable<Boolean> = breakdownViewIsGone
        override fun customReferrerPercent(): Observable<Float> = customReferrerPercent
        override fun customReferrerPercentText(): Observable<String> = customReferrerPercentText
        override fun emptyViewIsGone(): Observable<Boolean> = emptyViewIsGone
        override fun externalReferrerPercent(): Observable<Float> = externalReferrerPercent
        override fun externalReferrerPercentText(): Observable<String> = externalReferrerPercentText
        override fun kickstarterReferrerPercent(): Observable<Float> = kickstarterReferrerPercent
        override fun kickstarterReferrerPercentText(): Observable<String> = kickstarterReferrerPercentText
        override fun pledgedViaCustomLayoutIsGone(): Observable<Boolean> = pledgedViaCustomLayoutIsGone
        override fun pledgedViaExternalLayoutIsGone(): Observable<Boolean> = pledgedViaExternalLayoutIsGone
        override fun pledgedViaKickstarterLayoutIsGone(): Observable<Boolean> = pledgedViaKickstarterLayoutIsGone
        override fun projectAndAveragePledge(): Observable<Pair<Project, Int>> = projectAndAveragePledge
        override fun projectAndCustomReferrerPledgedAmount(): Observable<Pair<Project, Float>> = projectAndCustomReferrerPledgedAmount
        override fun projectAndExternalReferrerPledgedAmount(): Observable<Pair<Project, Float>> = projectAndExternalReferrerPledgedAmount
        override fun projectAndKickstarterReferrerPledgedAmount(): Observable<Pair<Project, Float>> = projectAndKickstarterReferrerPledgedAmount
    }
}
