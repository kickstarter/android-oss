package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.viewholders.PaginationErrorViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject

interface PaginationViewMoreViewHolderViewModel {
    interface Inputs {
        fun configureWith(configureCellWith: Boolean)
    }

    interface Outputs {
        fun isViewMoreRepliesPaginationVisible(): Observable<Boolean>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<PaginationErrorViewHolder>(environment), Inputs, Outputs {
        private val isViewMoreRepliesPaginationVisible = BehaviorSubject.create<Boolean>()
        private val initCellConfig = BehaviorSubject.create<Boolean>()

        val inputs = this
        val outputs = this

        init {
            this.initCellConfig
                .compose(bindToLifecycle())
                .subscribe {
                    this.isViewMoreRepliesPaginationVisible.onNext(it)
                }
        }

        // - Inputs
        override fun configureWith(shouldShowErrorCell: Boolean) = this.initCellConfig.onNext(shouldShowErrorCell)

        // - Outputs
        override fun isViewMoreRepliesPaginationVisible(): Observable<Boolean> = this.isViewMoreRepliesPaginationVisible
    }
}
