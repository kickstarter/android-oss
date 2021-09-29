package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.models.ProjectFaq
import com.kickstarter.ui.viewholders.FrequentlyAskedQuestionsViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface FrequentlyAskedQuestionsViewHolderViewModel {
    interface Inputs {
        /** Configure the view model with the [ProjectFaq]. */
        fun configureWith(projectFaq: ProjectFaq)
    }

    interface Outputs {
        /** Emits the String for the question  */
        fun question(): Observable<String>
        /** Emits the String for the answer */
        fun answer(): Observable<String>
        /** Emits the String for the updatedDate */
        fun updatedDate(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) :
        ActivityViewModel<FrequentlyAskedQuestionsViewHolder>(environment), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val projectFaqInput = PublishSubject.create<ProjectFaq>()
        private val question = BehaviorSubject.create<String>()
        private val answer = BehaviorSubject.create<String>()
        private val updatedDate = BehaviorSubject.create<String>()

        init {
            val projectFaqInput = this.projectFaqInput

            projectFaqInput
                .map { it.question }
                .subscribe(this.question)

            projectFaqInput
                .map { it.answer }
                .subscribe(this.answer)

            projectFaqInput
                .map { requireNotNull(it.createdAt) }
                .map { DateTimeUtils.longDate(it) }
                .subscribe(this.updatedDate)
        }

        override fun configureWith(projectFaq: ProjectFaq) = this.projectFaqInput.onNext(projectFaq)

        override fun question(): Observable<String> = this.question

        override fun answer(): Observable<String> = this.answer

        override fun updatedDate(): Observable<String> = this.updatedDate
    }
}
