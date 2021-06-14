package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.viewholders.PaginationErrorViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject

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

    class ViewModel(environment: Environment) : ActivityViewModel<PaginationErrorViewHolder>(environment), Inputs, Outputs {
        private val isErrorPaginationVisible = BehaviorSubject.create<Boolean>()
        private val initCellConfig = BehaviorSubject.create<Boolean>()

        val inputs = this
        val outputs = this

        init {
            this.initCellConfig
                .compose(bindToLifecycle())
                .subscribe {
                    this.isErrorPaginationVisible.onNext(it)
                }
        }

        // - Inputs
        override fun configureWith(shouldShowErrorCell: Boolean) = this.initCellConfig.onNext(shouldShowErrorCell)

        // - Outputs
        override fun isErrorPaginationVisible(): Observable<Boolean> = this.isErrorPaginationVisible
    }
}
