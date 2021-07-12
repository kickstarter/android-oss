package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.viewholders.RepliesStatusCellType
import com.kickstarter.ui.viewholders.RepliesStatusCellViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject

interface RepliesStatusCellViewHolderViewModel {
    interface Inputs {
        fun configureWith(configureCellWith: RepliesStatusCellType)
    }

    interface Outputs {
        fun isViewMoreRepliesPaginationVisible(): Observable<Boolean>
        fun isErrorPaginationVisible(): Observable<Boolean>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<RepliesStatusCellViewHolder>(environment), Inputs, Outputs {
        private val isViewMoreRepliesPaginationVisible = BehaviorSubject.create<Boolean>()
        private val isErrorPaginationVisible = BehaviorSubject.create<Boolean>()
        private val initCellConfig = BehaviorSubject.create<RepliesStatusCellType>()

        val inputs = this
        val outputs = this

        init {
            this.initCellConfig
                .compose(bindToLifecycle())
                .subscribe {
                    when (it) {
                        RepliesStatusCellType.VIEW_MORE -> {
                            this.isErrorPaginationVisible.onNext(false)
                            this.isViewMoreRepliesPaginationVisible.onNext(true)
                        }
                        RepliesStatusCellType.PAGINATION_ERROR -> {
                            this.isErrorPaginationVisible.onNext(true)
                            this.isViewMoreRepliesPaginationVisible.onNext(false)
                        }
                    }
                }
        }

        // - Inputs
        override fun configureWith(shouldShowErrorCell: RepliesStatusCellType) = this.initCellConfig.onNext(shouldShowErrorCell)

        // - Outputs
        override fun isViewMoreRepliesPaginationVisible(): Observable<Boolean> = this.isViewMoreRepliesPaginationVisible
        override fun isErrorPaginationVisible(): Observable<Boolean> = this.isErrorPaginationVisible
    }
}
