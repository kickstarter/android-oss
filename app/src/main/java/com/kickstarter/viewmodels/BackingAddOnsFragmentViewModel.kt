package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.fragments.RewardsFragment
import rx.Observable
import rx.subjects.PublishSubject

class BackingAddOnsFragmentViewModel {
    interface Inputs {
        /** Emits the pledgeData and Reason needed to work with in BackingAddOnsFragment.kt */
        fun configureWith(pledgeDataAndReason: Pair<PledgeData, PledgeReason>)
    }

    interface Outputs {
        fun showPledgeFragment(): Observable<Pair<PledgeData, PledgeReason>>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<RewardsFragment>(environment), Inputs, Outputs {
        val inputs = this
        val outputs = this

        private val pledgeDataAndReason = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val showPledgeFragment = PublishSubject.create<Pair<PledgeData, PledgeReason>>()

        init {

            val pledgeData = arguments()
                    .map { it.getParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA) as PledgeData? }
                    .ofType(PledgeData::class.java)

            val pledgeReason = arguments()
                    .map { it.getSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON) as PledgeReason }

            // - Get the reward either from intent or via input configureWith
            val project = pledgeDataAndReason
                    .map { it.first.projectData().project() }
                    .compose<Pair<Project, PledgeData>>(combineLatestPair(pledgeData))
                    .map { if (ObjectUtils.isNull(it.first)) it.second.projectData().project() else it.first }

            // - Get the reward either from intent or via input configureWith
            val reward = pledgeDataAndReason
                    .map { it.first.reward() }
                    .compose<Pair<Reward, PledgeData>>(combineLatestPair(pledgeData))
                    .map { if (ObjectUtils.isNull(it.first)) it.second.reward() else it.first }

            this.pledgeDataAndReason
                    .compose(bindToLifecycle())
                    .subscribe(this.showPledgeFragment)
        }

        @NonNull
        override fun showPledgeFragment(): Observable<Pair<PledgeData, PledgeReason>> = this.showPledgeFragment

        override fun configureWith(pledgeDataAndReason: Pair<PledgeData, PledgeReason>) {
            this.pledgeDataAndReason.onNext(pledgeDataAndReason)
        }
    }
}