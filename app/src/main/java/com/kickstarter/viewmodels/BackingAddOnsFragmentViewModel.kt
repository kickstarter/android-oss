package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.models.Reward
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.BackingAddOnsFragment
import rx.Observable
import rx.subjects.PublishSubject

class BackingAddOnsFragmentViewModel {

    interface Inputs {
        /** Configure with the current [ProjectData] and [Reward].
         * @param projectData we get the Project for currency
         */
        fun configureWith(pledgeDataAndReason: Pair<PledgeData, PledgeReason>)
    }

    interface Outputs {
        fun showPledgeFragment(): Observable<Pair<PledgeData, PledgeReason>>
        fun AddOnsList(): Observable<Pair<ProjectData, List<Reward>>>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<BackingAddOnsFragment>(environment), Outputs, Inputs {
        val inputs = this
        val outputs = this

        private val pledgeDataAndReason = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val showPledgeFragment = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val addOnsList = PublishSubject.create<Pair<ProjectData, List<Reward>>>()

        init {

            val pledgeData = arguments()
                    .map { it.getParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA) as PledgeData? }
                    .ofType(PledgeData::class.java)

            val pledgeReason = arguments()
                    .map { it.getSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON) as PledgeReason }

            val projectData = pledgeData
                    .map { it.projectData() }

            val project = projectData
                    .map { it.project() }

            val reward = pledgeData
                    .map { it.reward() }

            this.pledgeDataAndReason
                    .compose(bindToLifecycle())
                    .subscribe(this.showPledgeFragment)

            // TODO: Add-ons query will be added in here in exchange of the reward
            projectData
                    .compose<Pair<ProjectData, Reward>>(combineLatestPair(reward))
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.addOnsList.onNext(Pair(it.first, listOf(it.second, it.second)))
                    }
        }

        @NonNull
        override fun showPledgeFragment(): Observable<Pair<PledgeData, PledgeReason>> = this.showPledgeFragment
        override fun configureWith(pledgeDataAndReason: Pair<PledgeData, PledgeReason>) = this.pledgeDataAndReason.onNext(pledgeDataAndReason)

        override fun AddOnsList() = this.addOnsList
    }
}