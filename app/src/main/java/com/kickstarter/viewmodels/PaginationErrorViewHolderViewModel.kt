package com.kickstarter.viewmodels

import com.kickstarter.libs.utils.extensions.addToDisposable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

interface PaginationErrorViewHolderViewModel {
    interface Inputs {
        /** Pair with the configuration for the feedback cell UI
         * @param configureCellWith
         * - true: Show Error Cell configuration
         * - false: Hide Error Cell configuration
         */
        fun configureWith(configureCellWith: Boolean)
    }

    interface Outputs {
        fun isErrorPaginationVisible(): Observable<Boolean>
    }

    class ViewModel : Inputs, Outputs {
        private val isErrorPaginationVisible = BehaviorSubject.create<Boolean>()
        private val initCellConfig = BehaviorSubject.create<Boolean>()

        val inputs = this
        val outputs = this
        private val disposables = CompositeDisposable()

        init {
            this.initCellConfig
                .subscribe {
                    this.isErrorPaginationVisible.onNext(it)
                }.addToDisposable(disposables)
        }

        // - Inputs
        override fun configureWith(shouldShowErrorCell: Boolean) = this.initCellConfig.onNext(shouldShowErrorCell)

        // - Outputs
        override fun isErrorPaginationVisible(): Observable<Boolean> = this.isErrorPaginationVisible

        fun clear() {
            disposables.clear()
        }
    }
}
