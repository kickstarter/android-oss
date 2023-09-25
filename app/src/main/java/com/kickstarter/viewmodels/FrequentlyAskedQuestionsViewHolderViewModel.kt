package com.kickstarter.viewmodels

import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.ProjectFaq
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface FrequentlyAskedQuestionsViewHolderViewModel {
    interface Inputs {
        /** Configure the view model with the [ProjectFaq]. */
        fun configureWith(projectFaq: ProjectFaq)

        fun onDestroy()
    }

    interface Outputs {
        /** Emits the String for the question  */
        fun question(): Observable<String>
        /** Emits the String for the answer */
        fun answer(): Observable<String>
        /** Emits the String for the updatedDate */
        fun updatedDate(): Observable<String>
    }

    class FrequentlyAskedQuestionsViewHolderViewModel() : Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val projectFaqInput = PublishSubject.create<ProjectFaq>()
        private val question = BehaviorSubject.create<String>()
        private val answer = BehaviorSubject.create<String>()
        private val updatedDate = BehaviorSubject.create<String>()
        private val disposables = CompositeDisposable()

        init {
            val projectFaqInput = this.projectFaqInput

            projectFaqInput
                .map { it.question }
                .subscribe { this.question.onNext(it) }
                .addToDisposable(disposables)

            projectFaqInput
                .map { it.answer }
                .subscribe { this.answer.onNext(it) }
                .addToDisposable(disposables)

            projectFaqInput
                .filter { it.createdAt.isNotNull() }
                .map { it.createdAt?.let { createdAt -> DateTimeUtils.longDate(createdAt) } }
                .subscribe { this.updatedDate.onNext(it) }
                .addToDisposable(disposables)
        }

        override fun onDestroy() {
            disposables.clear()
        }

        override fun configureWith(projectFaq: ProjectFaq) = this.projectFaqInput.onNext(projectFaq)

        override fun question(): Observable<String> = this.question

        override fun answer(): Observable<String> = this.answer

        override fun updatedDate(): Observable<String> = this.updatedDate
    }
}
