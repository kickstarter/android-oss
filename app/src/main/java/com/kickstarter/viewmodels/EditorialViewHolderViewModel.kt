package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.data.Editorial
import com.kickstarter.ui.viewholders.EditorialViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface EditorialViewHolderViewModel {
    interface Inputs {
        fun editorialClicked()

        fun configureWith(editorial: Editorial)
    }

    interface Outputs {
        fun ctaDescription(): Observable<Int>
        fun ctaTitle(): Observable<Int>
        fun tag(): Observable<String>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<EditorialViewHolder>(environment), Inputs, Outputs {
        private val editorial = PublishSubject.create<Editorial>()
        private val editorialClicked = PublishSubject.create<Void>()

        private val ctaDescription = BehaviorSubject.create<Int>()
        private val ctaTitle = BehaviorSubject.create<Int>()
        private val tag = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        override fun configureWith(editorial: Editorial) = this.editorial.onNext(editorial)

        override fun editorialClicked() = this.editorialClicked.onNext(null)

        override fun ctaDescription(): Observable<Int> = this.ctaDescription

        override fun ctaTitle(): Observable<Int> = this.ctaTitle

        override fun tag(): Observable<String> = this.tag
    }
}
