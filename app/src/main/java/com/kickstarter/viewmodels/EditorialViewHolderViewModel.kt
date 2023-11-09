package com.kickstarter.viewmodels

import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.ui.data.Editorial
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

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

    class ViewModel : Inputs, Outputs {
        private val editorialInput = PublishSubject.create<Editorial>()
        private val editorialClicked = PublishSubject.create<Unit>()

        private val backgroundColor = BehaviorSubject.create<Int>()
        private val ctaDescription = BehaviorSubject.create<Int>()
        private val ctaTitle = BehaviorSubject.create<Int>()
        private val editorial = PublishSubject.create<Editorial>()
        private val graphic = BehaviorSubject.create<Int>()

        private val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.editorialInput
                .map { it.backgroundColor }
                .subscribe { this.backgroundColor.onNext(it) }
                .addToDisposable(disposables)

            this.editorialInput
                .map { it.ctaTitle }
                .subscribe { this.ctaTitle.onNext(it) }
                .addToDisposable(disposables)

            this.editorialInput
                .map { it.ctaDescription }
                .subscribe { this.ctaDescription.onNext(it) }
                .addToDisposable(disposables)

            this.editorialInput
                .map { it.graphic }
                .filter { it.isNotNull() }
                .subscribe { this.graphic.onNext(it) }
                .addToDisposable(disposables)

            this.editorialInput
                .compose(takeWhenV2(this.editorialClicked))
                .subscribe { this.editorial.onNext(it) }
                .addToDisposable(disposables)
        }

        override fun configureWith(editorial: Editorial) = this.editorialInput.onNext(editorial)

        override fun editorialClicked() = this.editorialClicked.onNext(Unit)

        override fun backgroundColor(): Observable<Int> = this.backgroundColor

        override fun ctaDescription(): Observable<Int> = this.ctaDescription

        override fun ctaTitle(): Observable<Int> = this.ctaTitle

        override fun editorial(): Observable<Editorial> = this.editorial

        override fun graphic(): Observable<Int> = this.graphic

        fun clear() {
            disposables.clear()
        }
    }
}
