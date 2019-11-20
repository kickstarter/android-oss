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
        fun backgroundTint(): Observable<Int>

        /** Emits the @StringRes ID of the CTA description of the [Editorial]. */
        fun ctaDescription(): Observable<Int>

        /** Emits the @StringRes ID of the CTA title of the [Editorial]. */
        fun ctaTitle(): Observable<Int>

        /** Emits the @DrawableRes ID of the graphic of the [Editorial]. */
        fun graphic(): Observable<Int>

        /** Emits the tagId of the [Editorial]. */
        fun tagId(): Observable<Int>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<EditorialViewHolder>(environment), Inputs, Outputs {
        private val editorial = PublishSubject.create<Editorial>()
        private val editorialClicked = PublishSubject.create<Void>()

        private val backgroundTint = BehaviorSubject.create<Int>()
        private val ctaDescription = BehaviorSubject.create<Int>()
        private val ctaTitle = BehaviorSubject.create<Int>()
        private val graphic = BehaviorSubject.create<Int>()
        private val tagId = BehaviorSubject.create<Int>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.editorial
                    .map { it.backgroundTint }
                    .compose(bindToLifecycle())
                    .subscribe(this.backgroundTint)

            this.editorial
                    .map { it.ctaTitle }
                    .compose(bindToLifecycle())
                    .subscribe(this.ctaTitle)

            this.editorial
                    .map { it.ctaDescription }
                    .compose(bindToLifecycle())
                    .subscribe(this.ctaDescription)

            this.editorial
                    .map { it.graphic }
                    .filter { ObjectUtils.isNotNull(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.graphic)

            this.editorial
                    .compose<Editorial>(takeWhen(this.editorialClicked))
                    .map { it.tagId }
                    .compose(bindToLifecycle())
                    .subscribe(this.tagId)
        }

        override fun configureWith(editorial: Editorial) = this.editorial.onNext(editorial)

        override fun editorialClicked() = this.editorialClicked.onNext(null)

        override fun backgroundTint(): Observable<Int> = this.backgroundTint

        override fun ctaDescription(): Observable<Int> = this.ctaDescription

        override fun ctaTitle(): Observable<Int> = this.ctaTitle

        override fun graphic(): Observable<Int> = this.graphic

        override fun tagId(): Observable<Int> = this.tagId
    }
}
