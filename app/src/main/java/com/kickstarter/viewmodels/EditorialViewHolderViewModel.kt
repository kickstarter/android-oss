package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.ui.data.Editorial
import com.kickstarter.ui.viewholders.EditorialViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface EditorialViewHolderViewModel {
    interface Inputs {
        /** Call when the user clicks the editorial tout. */
        fun editorialClicked()

        /** Configure the view model with the [Editorial]. */
        fun configureWith(editorial: Editorial)
    }

    interface Outputs {
        /** Emits the @ColorRes ID of the background color of the [Editorial]. */
        fun backgroundColor(): Observable<Int>

        /** Emits the @StringRes ID of the CTA description of the [Editorial]. */
        fun ctaDescription(): Observable<Int>

        /** Emits the @StringRes ID of the CTA title of the [Editorial]. */
        fun ctaTitle(): Observable<Int>

        /** Emits the current [Editorial] when it's clicked. */
        fun editorial(): Observable<Editorial>

        /** Emits the @DrawableRes ID of the graphic of the [Editorial]. */
        fun graphic(): Observable<Int>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<EditorialViewHolder>(environment), Inputs, Outputs {
        private val editorialInput = PublishSubject.create<Editorial>()
        private val editorialClicked = PublishSubject.create<Void>()

        private val backgroundColor = BehaviorSubject.create<Int>()
        private val ctaDescription = BehaviorSubject.create<Int>()
        private val ctaTitle = BehaviorSubject.create<Int>()
        private val editorial = PublishSubject.create<Editorial>()
        private val graphic = BehaviorSubject.create<Int>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.editorialInput
                .map { it.backgroundColor }
                .compose(bindToLifecycle())
                .subscribe(this.backgroundColor)

            this.editorialInput
                .map { it.ctaTitle }
                .compose(bindToLifecycle())
                .subscribe(this.ctaTitle)

            this.editorialInput
                .map { it.ctaDescription }
                .compose(bindToLifecycle())
                .subscribe(this.ctaDescription)

            this.editorialInput
                .map { it.graphic }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe(this.graphic)

            this.editorialInput
                .compose<Editorial>(takeWhen(this.editorialClicked))
                .compose(bindToLifecycle())
                .subscribe(this.editorial)
        }

        override fun configureWith(editorial: Editorial) = this.editorialInput.onNext(editorial)

        override fun editorialClicked() = this.editorialClicked.onNext(null)

        override fun backgroundColor(): Observable<Int> = this.backgroundColor

        override fun ctaDescription(): Observable<Int> = this.ctaDescription

        override fun ctaTitle(): Observable<Int> = this.ctaTitle

        override fun editorial(): Observable<Editorial> = this.editorial

        override fun graphic(): Observable<Int> = this.graphic
    }
}
