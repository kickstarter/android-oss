package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.EnvironmentalCommitmentCategories
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.ui.viewholders.EnvironmentalCommitmentsViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class EnvironmentalCommitmentsViewHolderViewModel {
    interface Inputs {
        /** Configure the view model with the [EnvironmentalCommitment]. */
        fun configureWith(environmentalCommitmentInput: EnvironmentalCommitment)
    }

    interface Outputs {
        /** Emits the String for the description  */
        fun description(): Observable<String>
        /** Emits the String for the answer */
        fun category(): Observable<Int>
    }

    class ViewModel(@NonNull val environment: Environment) :
        ActivityViewModel<EnvironmentalCommitmentsViewHolder>(environment), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val projectEnvironmentalCommitmentInput = PublishSubject
            .create<EnvironmentalCommitment>()
        private val description = BehaviorSubject.create<String>()
        private val category = BehaviorSubject.create<Int>()

        init {

            this.projectEnvironmentalCommitmentInput
                .map { it.description }
                .subscribe(this.description)

            this.projectEnvironmentalCommitmentInput
                .map {
                    EnvironmentalCommitmentCategories.values().firstOrNull { environmentalCommitmentCategory ->
                        environmentalCommitmentCategory.name == it.category
                    }?.title
                }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .subscribe(this.category)
        }

        override fun configureWith(environmentalCommitmentInput: EnvironmentalCommitment) = this.projectEnvironmentalCommitmentInput.onNext(environmentalCommitmentInput)

        override fun description(): Observable<String> = this.description

        override fun category(): Observable<Int> = this.category
    }
}
