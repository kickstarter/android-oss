package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import com.kickstarter.libs.EnvironmentalCommitmentCategories
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.EnvironmentalCommitment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface EnvironmentalCommitmentsViewHolderViewModel {
    interface Inputs {
        /** Configure the view model with the [EnvironmentalCommitment]. */
        fun configureWith(environmentalCommitmentInput: EnvironmentalCommitment)

       fun clearDisposables()
    }

    interface Outputs {
        /** Emits the String for the description  */
        fun description(): Observable<String>
        /** Emits the String for the answer */
        fun category(): Observable<Int>
    }

    class EnvironmentalCommitmentsViewHolderViewModel() :
        ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val projectEnvironmentalCommitmentInput = PublishSubject
            .create<EnvironmentalCommitment>()
        private val description = BehaviorSubject.create<String>()
        private val category = BehaviorSubject.create<Int>()
        private val disposables = CompositeDisposable()

        init {

            this.projectEnvironmentalCommitmentInput
                .map { it.description }
                .subscribe { this.description.onNext(it) }
                .addToDisposable(disposables)


            this.projectEnvironmentalCommitmentInput
                .map {
                    EnvironmentalCommitmentCategories.values().firstOrNull { environmentalCommitmentCategory ->
                        environmentalCommitmentCategory.name == it.category
                    }?.title
                }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .subscribe { this.category.onNext(it) }
                .addToDisposable(disposables)
        }

        override fun clearDisposables() {
            disposables.clear()
        }

        override fun configureWith(environmentalCommitmentInput: EnvironmentalCommitment) = this.projectEnvironmentalCommitmentInput.onNext(environmentalCommitmentInput)

        override fun description(): Observable<String> = this.description

        override fun category(): Observable<Int> = this.category
    }
}
